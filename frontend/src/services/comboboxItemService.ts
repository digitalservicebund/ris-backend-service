import { useFetch, UseFetchReturn } from "@vueuse/core"
import { capitalize, computed, Ref } from "vue"
import { API_PREFIX } from "./httpClient"
import { ComboboxItem } from "@/components/input/types"
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

type Endpoint<T> = {
  path: string
  format: (responseData: T) => ComboboxItem<T>
}

const endpoints = {
  documentTypes: {
    path: "documenttypes",
    format: (item: DocumentType) => ({
      label: item.label,
      value: item,
      additionalInformation: item.jurisShortcut,
    }),
  },
  courts: {
    path: "courts",
    format: (item: Court) => ({
      label: item.label,
      value: item,
      additionalInformation: item.revoked,
    }),
  },
  citationTypes: {
    path: "citationtypes",
    format: (item: CitationType) => ({
      label: item.label,
      value: item,
    }),
  },
  fieldOfLawSearchByIdentifier: {
    path: "fieldsoflaw/search-by-identifier",
    format: (item: FieldOfLaw) => ({
      label: item.identifier,
      value: item,
      additionalInformation: item.text,
    }),
  },
  risAbbreviations: {
    path: "normabbreviation/search",
    format: (item: NormAbbreviation) => ({
      label: item.abbreviation,
      value: item,
      additionalInformation: `${item.officialLongTitle ?? ""} ${item.officialLongTitle && item.documentNumber ? " | " : ""} ${item.documentNumber ?? ""}`,
    }),
  },
  procedures: {
    path: "procedure",
    format: (item: Procedure) => ({
      label: item.label,
      value: item,
      additionalInformation: `${
        item.documentationUnitCount ?? 0
      } Dokumentationseinheiten`,
    }),
  },
  legalForceRegions: {
    path: "region/applicable",
    format: (item: LegalForceRegion) => ({
      label: item.longText,
      value: item,
    }),
  },
  legalForceTypes: {
    path: "legalforcetype",
    format: (item: LegalForceType) => ({
      label: capitalize(item.abbreviation),
      value: item,
    }),
  },
  legalPeriodicals: {
    path: "legalperiodicals",
    format: (item: LegalPeriodical) => ({
      label: `${item.abbreviation} | ${item.title}`,
      value: item,
      additionalInformation: item.subtitle,
      sideInformation: item.primaryReference ? "amtlich" : "nicht amtlich",
    }),
  },
  documentationOffices: {
    path: "documentationoffices",
    format: (item: DocumentationOffice) => ({
      label: item.abbreviation,
      value: item,
    }),
  },
  languageCodes: {
    path: "languagecodes",
    format: (item: LanguageCode) => ({
      label: item.label,
      value: item,
    }),
  },
  usersForDocOffice: {
    path: "users",
    format: (item: User) => ({
      label: item.name ?? item.email,
      value: item,
    }),
  },
  collectiveAgreementIndustries: {
    path: "collective-agreement-industries",
    format: (item: CollectiveAgreementIndustry) => ({
      label: item.label,
      value: item,
    }),
  },
  currencyCodes: {
    path: "currencycodes",
    format: (item: CurrencyCode) => ({
      label: item.label,
      value: item,
    }),
  },
} satisfies {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  [key: string]: Endpoint<any>
}

function fetchFromEndpoint<T>(
  endpoint: Endpoint<T>,
  filter: Ref<string | undefined>,
  size?: number,
  category?: DocumentTypeCategory,
): UseFetchReturn<T[]> {
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
    return `${API_PREFIX}caselaw/${endpoint.path}?${queryParams}`
  })

  return useFetch<T[]>(url, {
    afterFetch: (ctx) => {
      // endpoints.procedures returns a paginated response we need to unpack
      if ("content" in ctx.data) {
        ctx.data = ctx.data.content
      }

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

function toComboboxItemService<T>(
  endpoint: Endpoint<T>,
  size?: number,
  category?: DocumentTypeCategory,
): ComboboxItemService<T> {
  return (filter) => ({
    format: endpoint.format,
    useFetch: fetchFromEndpoint(endpoint, filter, size, category),
  })
}

const services = {
  getCollectiveAgreementIndustries: toComboboxItemService(
    endpoints.collectiveAgreementIndustries,
  ),
  getCourts: toComboboxItemService(endpoints.courts, 200),
  getCaselawDocumentTypes: toComboboxItemService(endpoints.documentTypes),
  getCaselawAndPendingProceedingDocumentTypes: toComboboxItemService(
    endpoints.documentTypes,
    undefined,
    DocumentTypeCategory.CASELAW_PENDING_PROCEEDING,
  ),
  getDependentLiteratureDocumentTypes: toComboboxItemService(
    endpoints.documentTypes,
    undefined,
    DocumentTypeCategory.DEPENDENT_LITERATURE,
  ),
  getFieldOfLawSearchByIdentifier: toComboboxItemService(
    endpoints.fieldOfLawSearchByIdentifier,
    200,
  ),
  getCountryFieldOfLawSearchByIdentifier: (filter: Ref<string | undefined>) =>
    toComboboxItemService(
      endpoints.fieldOfLawSearchByIdentifier,
      1000,
    )(computed(() => `RE-07-${filter.value?.replace("RE-07-", "") ?? ""}`)),
  getRisAbbreviations: toComboboxItemService(endpoints.risAbbreviations, 30),
  getCitationTypes: toComboboxItemService(endpoints.citationTypes),
  getProcedures: toComboboxItemService(endpoints.procedures, 10),
  getLegalForceTypes: toComboboxItemService(endpoints.legalForceTypes),
  getLegalForceRegions: toComboboxItemService(endpoints.legalForceRegions),
  getLegalPeriodicals: toComboboxItemService(endpoints.legalPeriodicals),
  getDocumentationOffices: toComboboxItemService(
    endpoints.documentationOffices,
  ),
  getLanguageCodes: toComboboxItemService(endpoints.languageCodes),
  getUsersForDocOffice: toComboboxItemService(endpoints.usersForDocOffice),
  getCurrencyCodes: toComboboxItemService(endpoints.currencyCodes),
}

export type ComboboxItemService<T> = (filter: Ref<string | undefined>) => {
  useFetch: UseFetchReturn<T[]>
  format: (item: T) => ComboboxItem<T>
}

export default services
