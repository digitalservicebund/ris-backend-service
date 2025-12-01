import { useFetch, UseFetchReturn } from "@vueuse/core"
import { capitalize, computed, Ref } from "vue"
import { API_PREFIX } from "./httpClient"
import { ComboboxInputModelType, ComboboxItem } from "@/components/input/types"
import { Page } from "@/components/Pagination.vue"
import { CitationType } from "@/domain/citationType"
import { CollectiveAgreementIndustry } from "@/domain/collectiveAgreementIndustry"
import { Court } from "@/domain/court"
import DocumentationOffice from "@/domain/documentationOffice"
import { DocumentType } from "@/domain/documentType"
import { DocumentTypeCategory } from "@/domain/documentTypeCategory"
import { FieldOfLaw } from "@/domain/fieldOfLaw"
import { LanguageCode } from "@/domain/foreignLanguageVersion"
import { LegalForceRegion, LegalForceType } from "@/domain/legalForce"
import LegalPeriodical from "@/domain/legalPeriodical"
import { NormAbbreviation } from "@/domain/normAbbreviation"
import { CurrencyCode } from "@/domain/objectValue"
import { Procedure } from "@/domain/procedure"
import { User } from "@/domain/user"
import errorMessages from "@/i18n/errors.json"

enum Endpoint {
  documentTypes = "documenttypes",
  courts = "courts",
  citationTypes = "citationtypes",
  fieldOfLawSearchByIdentifier = "fieldsoflaw/search-by-identifier",
  risAbbreviations = `normabbreviation/search`,
  procedures = `procedure`,
  legalForceRegions = `region/applicable`,
  legalForceTypes = `legalforcetype`,
  legalPeriodicals = `legalperiodicals`,
  documentationOffices = `documentationoffices`,
  languageCodes = `languagecodes`,
  usersForDocOffice = "users",
  collectiveAgreementIndustries = "collective-agreement-industries",
  currencyCodes = `currencycodes`,
}

function formatDropdownItems(
  responseData: ComboboxInputModelType[],
  endpoint: Endpoint,
): ComboboxItem[] {
  switch (endpoint) {
    case Endpoint.documentTypes: {
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
    case Endpoint.languageCodes: {
      return (responseData as LanguageCode[]).map((item) => ({
        label: item.label,
        value: item,
      }))
    }
    case Endpoint.usersForDocOffice: {
      return (responseData as User[]).map((item) => ({
        label: item.name ?? item.email,
        value: item,
      }))
    }
    case Endpoint.collectiveAgreementIndustries: {
      return (responseData as CollectiveAgreementIndustry[]).map((item) => ({
        label: item.label,
        value: item,
      }))
    }
    case Endpoint.currencyCodes: {
      return (responseData as CurrencyCode[]).map((item) => ({
        label: item.label,
        value: item,
      }))
    }
  }
}

function fetchFromEndpoint(
  endpoint: Endpoint,
  filter: Ref<string | undefined>,
  size?: number,
  category?: DocumentTypeCategory,
) {
  const requestParams = computed<{
    q?: string
    sz?: string
    category?: string
  }>(() => ({
    ...(filter.value ? { q: filter.value } : {}),
    ...(size != undefined ? { sz: size.toString(), pg: "0" } : {}),
    ...(category ? { category: category.toString() } : {}),
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
  // Generic signature for most methods (excluding 'documentTypes')
  [key in Exclude<
    keyof typeof Endpoint,
    "documentTypes"
  > as `get${Capitalize<key>}`]: (
    filter: Ref<string | undefined>,
  ) => UseFetchReturn<ComboboxItem[]>
} & {
  // --- Convenience methods for document types ---
  getCaselawDocumentTypes: (
    filter: Ref<string | undefined>,
  ) => UseFetchReturn<ComboboxItem[]>
  getCaselawAndPendingProceedingDocumentTypes: (
    filter: Ref<string | undefined>,
  ) => UseFetchReturn<ComboboxItem[]>
  getDependentLiteratureDocumentTypes: (
    filter: Ref<string | undefined>,
  ) => UseFetchReturn<ComboboxItem[]>
}

const service: ComboboxItemService = {
  getCollectiveAgreementIndustries: (filter: Ref<string | undefined>) =>
    fetchFromEndpoint(Endpoint.collectiveAgreementIndustries, filter),
  getCourts: (filter: Ref<string | undefined>) =>
    fetchFromEndpoint(Endpoint.courts, filter, 200),
  getCaselawDocumentTypes: (filter: Ref<string | undefined>) =>
    fetchFromEndpoint(Endpoint.documentTypes, filter),
  getCaselawAndPendingProceedingDocumentTypes: (
    filter: Ref<string | undefined>,
  ) =>
    fetchFromEndpoint(
      Endpoint.documentTypes,
      filter,
      undefined,
      DocumentTypeCategory.CASELAW_PENDING_PROCEEDING,
    ),
  getDependentLiteratureDocumentTypes: (filter: Ref<string | undefined>) =>
    fetchFromEndpoint(
      Endpoint.documentTypes,
      filter,
      undefined,
      DocumentTypeCategory.DEPENDENT_LITERATURE,
    ),
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
  getLanguageCodes: (filter: Ref<string | undefined>) =>
    fetchFromEndpoint(Endpoint.languageCodes, filter),
  getUsersForDocOffice: (filter: Ref<string | undefined>) =>
    fetchFromEndpoint(Endpoint.usersForDocOffice, filter),
  getCurrencyCodes: (filter: Ref<string | undefined>) =>
    fetchFromEndpoint(Endpoint.currencyCodes, filter),
}

export default service
