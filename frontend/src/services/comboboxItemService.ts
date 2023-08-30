import httpClient, { ServiceResponse } from "./httpClient"
import { CitationStyle } from "@/domain/citationStyle"
import { Court, Procedure } from "@/domain/documentUnit"
import { FieldOfLawNode } from "@/domain/fieldOfLaw"
import { NormAbbreviation } from "@/domain/normAbbreviation"
import {
  ComboboxInputModelType,
  ComboboxItem,
} from "@/shared/components/input/types"
import errorMessages from "@/shared/i18n/errors.json"

enum Endpoint {
  documentTypes = "lookuptable/documentTypes",
  courts = "lookuptable/courts",
  citationStyles = "lookuptable/zitart",
  fieldOfLawSearchByIdentifier = "fieldsoflaw/search-by-identifier",
  risAbbreviations = `normabbreviation?pg=0&sz=30`,
  risAbbreviationsAwesome = `normabbreviation/search?pg=0&sz=30`,
  procedures = `procedure`,
}

type DocumentType = {
  id: number
  jurisShortcut: string
  label: string
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
      return (responseData as FieldOfLawNode[]).map((item) => ({
        label: item.identifier,
        value: item,
        additionalInformation: item.text,
      }))
    }
    case Endpoint.risAbbreviations:
    case Endpoint.risAbbreviationsAwesome: {
      return (responseData as NormAbbreviation[]).map((item) => ({
        label: item.abbreviation,
        value: item,
        additionalInformation: item.officialLongTitle,
      }))
    }
    case Endpoint.citationStyles: {
      return (responseData as CitationStyle[]).map((item) => ({
        label: item.label,
        value: item,
        additionalInformation: item.jurisShortcut,
      }))
    }
    case Endpoint.procedures: {
      return (responseData as Procedure[]).map((item) => ({
        label: item.label,
        value: item,
        additionalInformation: `${
          item.documentUnitCount ?? 0
        } Dokumentationseinheiten`,
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
  getRisAbbreviationsAwesome: async (filter?: string) =>
    await fetchFromEndpoint(Endpoint.risAbbreviationsAwesome, filter),
  getCitationStyles: async (filter?: string) =>
    await fetchFromEndpoint(Endpoint.citationStyles, filter),
  getProcedures: async (filter?: string) =>
    await fetchFromEndpoint(Endpoint.procedures, filter, 10),
}

export default service
