import { useFetch, UseFetchReturn } from "@vueuse/core"
import { capitalize, computed, Ref } from "vue"
import { API_PREFIX } from "./httpClient"
import { ComboboxInputModelType, ComboboxItem } from "@/components/input/types"
import { Page } from "@/components/Pagination.vue"
import { CitationType } from "@/domain/citationType"
import DocumentationOffice from "@/domain/documentationOffice"
import { Court, DocumentType, Procedure } from "@/domain/documentUnit"
import { FieldOfLaw } from "@/domain/fieldOfLaw"
import { LegalForceRegion, LegalForceType } from "@/domain/legalForce"
import LegalPeriodical from "@/domain/legalPeriodical"
import { NormAbbreviation } from "@/domain/normAbbreviation"
import errorMessages from "@/i18n/errors.json"

enum Endpoint {
  documentTypes = "documenttypes",
  dependentLiteratureDocumentTypes = "documenttypes/dependent-literature",
  courts = "courts",
  citationTypes = "citationtypes",
  fieldOfLawSearchByIdentifier = "fieldsoflaw/search-by-identifier",
  risAbbreviations = `normabbreviation/search`,
  procedures = `procedure`,
  legalForceRegions = `region/applicable`,
  legalForceTypes = `legalforcetype`,
  legalPeriodicals = `legalperiodicals`,
  documentationOffices = `documentationoffices`,
}

function formatDropdownItems(
  responseData: ComboboxInputModelType[],
  endpoint: Endpoint,
): ComboboxItem[] {
  switch (endpoint) {
    case Endpoint.documentTypes:
    case Endpoint.dependentLiteratureDocumentTypes: {
      return (responseData as DocumentType[]).map((item) => ({
        label: item.label,
        value: item,
        additionalInformation: item.jurisShortcut,
      }))
    }
    case Endpoint.courts: {
      return (responseData as Court[]).map((item) => ({
        label: item.label,
        value: item,
        additionalInformation: item.revoked,
      }))
    }
    case Endpoint.fieldOfLawSearchByIdentifier: {
      return (responseData as FieldOfLaw[]).map((item) => ({
        label: item.identifier,
        value: item,
        additionalInformation: item.text,
      }))
    }
    case Endpoint.risAbbreviations: {
      return (responseData as NormAbbreviation[]).map((item) => ({
        label: item.abbreviation,
        value: item,
        additionalInformation: `${item.officialLongTitle ?? ""} ${item.officialLongTitle && item.documentNumber ? " | " : ""} ${item.documentNumber ?? ""}`,
      }))
    }
    case Endpoint.citationTypes: {
      return (responseData as CitationType[]).map((item) => ({
        label: item.label,
        value: item,
      }))
    }
    case Endpoint.procedures: {
      return (responseData as unknown as Page<Procedure>).content.map(
        (item) => ({
          label: item.label,
          value: item,
          additionalInformation: `${
            item.documentationUnitCount ?? 0
          } Dokumentationseinheiten`,
        }),
      )
    }
    case Endpoint.legalForceTypes: {
      return (responseData as LegalForceType[]).map((item) => ({
        label: capitalize(item.abbreviation),
        value: item,
      }))
    }
    case Endpoint.legalForceRegions: {
      return (responseData as LegalForceRegion[]).map((item) => ({
        label: item.longText,
        value: item,
      }))
    }
    case Endpoint.legalPeriodicals: {
      return (responseData as LegalPeriodical[]).map((item) => ({
        label: `${item.abbreviation} | ${item.title}`,
        value: item,
        additionalInformation: item.subtitle,
        sideInformation: item.primaryReference ? "amtlich" : "nicht amtlich",
      }))
    }
    case Endpoint.documentationOffices: {
      return (responseData as DocumentationOffice[]).map((item) => ({
        label: item.abbreviation,
        value: item,
      }))
    }
  }
}

function fetchFromEndpoint(
  endpoint: Endpoint,
  filter: Ref<string | undefined>,
  size?: number,
) {
  const requestParams = computed<{ q?: string; sz?: string }>(() => ({
    ...(filter.value ? { q: filter.value } : {}),
    ...(size != undefined ? { sz: size.toString(), pg: "0" } : {}),
  }))
  const url = computed(() => {
    const queryParams = new URLSearchParams(requestParams.value).toString()
    return `${API_PREFIX}caselaw/${endpoint}?${queryParams}`
  })

  return useFetch<ComboboxItem[]>(url, {
    afterFetch: (ctx) => {
      ctx.data = formatDropdownItems(ctx.data, endpoint)
      return ctx
    },
    onFetchError: ({ response }) => ({
      status: response?.status,
      error: {
        title: errorMessages.SERVER_ERROR_DROPDOWN.title,
        description: errorMessages.SERVER_ERROR_DROPDOWN.description,
      },
    }),
  }).json()
}

type ComboboxItemService = {
  [key in keyof typeof Endpoint as `get${Capitalize<key>}`]: (
    filter: Ref<string | undefined>,
  ) => UseFetchReturn<ComboboxItem[]>
}

const service: ComboboxItemService = {
  getCourts: (filter: Ref<string | undefined>) =>
    fetchFromEndpoint(Endpoint.courts, filter, 200),
  getDocumentTypes: (filter: Ref<string | undefined>) =>
    fetchFromEndpoint(Endpoint.documentTypes, filter),
  getDependentLiteratureDocumentTypes: (filter: Ref<string | undefined>) =>
    fetchFromEndpoint(Endpoint.dependentLiteratureDocumentTypes, filter),
  getFieldOfLawSearchByIdentifier: (filter: Ref<string | undefined>) =>
    fetchFromEndpoint(Endpoint.fieldOfLawSearchByIdentifier, filter),
  getRisAbbreviations: (filter: Ref<string | undefined>) =>
    fetchFromEndpoint(Endpoint.risAbbreviations, filter, 30),
  getCitationTypes: (filter: Ref<string | undefined>) =>
    fetchFromEndpoint(Endpoint.citationTypes, filter),
  getProcedures: (filter: Ref<string | undefined>) =>
    fetchFromEndpoint(Endpoint.procedures, filter, 10),
  getLegalForceTypes: (filter: Ref<string | undefined>) =>
    fetchFromEndpoint(Endpoint.legalForceTypes, filter),
  getLegalForceRegions: (filter: Ref<string | undefined>) =>
    fetchFromEndpoint(Endpoint.legalForceRegions, filter),
  getLegalPeriodicals: (filter: Ref<string | undefined>) =>
    fetchFromEndpoint(Endpoint.legalPeriodicals, filter),
  getDocumentationOffices: (filter: Ref<string | undefined>) =>
    fetchFromEndpoint(Endpoint.documentationOffices, filter),
}

export default service
