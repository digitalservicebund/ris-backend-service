import httpClient, { ServiceResponse } from "./httpClient"
import { Court } from "@/domain/documentUnit"
import { FieldOfLawNode } from "@/domain/fieldOfLaw"
import { NormAbbreviation } from "@/domain/normAbbreviation"
import { ComboboxItem } from "@/shared/components/input/types"

enum Endpoint {
  documentTypes = "lookuptable/documentTypes",
  courts = "lookuptable/courts",
  fieldOfLawSearchByIdentifier = "fieldsoflaw/search-by-identifier",
  risAbbreviations = `normabbreviation?pg=0&sz=30`,
  risAbbreviationsAwesome = `normabbreviation/search?pg=0&sz=30`,
}

type DocumentType = {
  id: number
  jurisShortcut: string
  label: string
}

type DropdownType =
  | DocumentType[]
  | Court[]
  | FieldOfLawNode[]
  | NormAbbreviation[]

function formatDropdownItems(
  responseData: DropdownType,
  endpoint: Endpoint
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
    case Endpoint.risAbbreviations: {
      return (responseData as NormAbbreviation[]).map((item) => ({
        label: item.abbreviation,
        value: item,
        additionalInformation: item.officialLongTitle,
      }))
    }
    case Endpoint.risAbbreviationsAwesome: {
      return (responseData as NormAbbreviation[]).map((item) => ({
        label: item.abbreviation,
        value: item,
        additionalInformation: item.officialLongTitle,
      }))
    }
  }
}

async function fetchFromEndpoint(endpoint: Endpoint, filter?: string) {
  const response = await httpClient.get<DropdownType>(
    `caselaw/${endpoint}`,
    filter ? { params: { q: filter } } : undefined
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
        title: "Serverfehler.",
        description: "Dropdown Items konnten nicht geladen werden.",
      },
    }
  }
}

type ComboboxItemService = {
  [key in keyof typeof Endpoint as `get${Capitalize<key>}`]: (
    filter?: string
  ) => Promise<ServiceResponse<ComboboxItem[]>>
} & {
  filterItems: (
    items: ComboboxItem[]
  ) => (filter?: string) => Promise<ServiceResponse<ComboboxItem[]>>
}

const service: ComboboxItemService = {
  filterItems: (items: ComboboxItem[]) => (filter?: string) => {
    const filteredItems = filter
      ? items.filter((item) => item.label.includes(filter))
      : items
    return Promise.resolve({ status: 200, data: filteredItems })
  },
  getCourts: (filter?: string) => fetchFromEndpoint(Endpoint.courts, filter),
  getDocumentTypes: (filter?: string) =>
    fetchFromEndpoint(Endpoint.documentTypes, filter),
  getFieldOfLawSearchByIdentifier: (filter?: string) =>
    fetchFromEndpoint(Endpoint.fieldOfLawSearchByIdentifier, filter),
  getRisAbbreviations: (filter?: string) =>
    fetchFromEndpoint(Endpoint.risAbbreviations, filter),
  getRisAbbreviationsAwesome: (filter?: string) =>
    fetchFromEndpoint(Endpoint.risAbbreviationsAwesome, filter),
}

export default service
