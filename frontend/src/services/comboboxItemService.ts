import { capitalize } from "vue"
import httpClient, { ServiceResponse } from "./httpClient"
import { ComboboxInputModelType, ComboboxItem } from "@/components/input/types"
import { Page } from "@/components/Pagination.vue"
import { CitationType } from "@/domain/citationType"
import { Court, Procedure, DocumentType } from "@/domain/documentUnit"
import { FieldOfLaw } from "@/domain/fieldOfLaw"
import { LegalForceType, LegalForceRegion } from "@/domain/legalForce"
import { NormAbbreviation } from "@/domain/normAbbreviation"
import errorMessages from "@/i18n/errors.json"

enum Endpoint {
  documentTypes = "documenttypes",
  courts = "courts",
  citationTypes = "citationtypes",
  fieldOfLawSearchByIdentifier = "fieldsoflaw/search-by-identifier",
  risAbbreviations = `normabbreviation/search?pg=0&sz=30`,
  procedures = `procedure`,
  legalForceRegions = `region/applicable`,
  legalForceTypes = `legalforcetype`,
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
            item.documentUnitCount ?? 0
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
  }
}

async function fetchFromEndpoint(
  endpoint: Endpoint,
  filter?: string,
  size?: number,
) {
  const requestParams: { q?: string; sz?: string } = {
    ...(filter ? { q: filter } : {}),
    ...(size != undefined ? { sz: size.toString() } : {}),
  }
  const response = await httpClient.get<ComboboxInputModelType[]>(
    `caselaw/${endpoint}`,
    { params: requestParams },
  )
  if (response.data) {
    return {
      status: response.status,
      data: formatDropdownItems(response.data, endpoint),
    }
  } else {
    return {
      status: response.status,
      error: {
        title: errorMessages.SERVER_ERROR_DROPDOWN.title,
        description: errorMessages.SERVER_ERROR_DROPDOWN.description,
      },
    }
  }
}

type ComboboxItemService = {
  [key in keyof typeof Endpoint as `get${Capitalize<key>}`]: (
    filter?: string,
  ) => Promise<ServiceResponse<ComboboxItem[]>>
} & {
  filterItems: (
    items: ComboboxItem[],
  ) => (filter?: string) => Promise<ServiceResponse<ComboboxItem[]>>
}

const service: ComboboxItemService = {
  filterItems: (items: ComboboxItem[]) => (filter?: string) => {
    const filteredItems = filter
      ? items.filter((item) => item.label.includes(filter))
      : items
    return Promise.resolve({ status: 200, data: filteredItems })
  },
  getCourts: async (filter?: string) =>
    await fetchFromEndpoint(Endpoint.courts, filter),
  getDocumentTypes: async (filter?: string) =>
    await fetchFromEndpoint(Endpoint.documentTypes, filter),
  getFieldOfLawSearchByIdentifier: async (filter?: string) =>
    await fetchFromEndpoint(Endpoint.fieldOfLawSearchByIdentifier, filter),
  getRisAbbreviations: async (filter?: string) =>
    await fetchFromEndpoint(Endpoint.risAbbreviations, filter),
  getCitationTypes: async (filter?: string) =>
    await fetchFromEndpoint(Endpoint.citationTypes, filter),
  getProcedures: async (filter?: string) =>
    await fetchFromEndpoint(Endpoint.procedures, filter, 10),
  getLegalForceTypes: async (filter?: string) =>
    await fetchFromEndpoint(Endpoint.legalForceTypes, filter),
  getLegalForceRegions: async (filter?: string) =>
    await fetchFromEndpoint(Endpoint.legalForceRegions, filter),
}

export default service
