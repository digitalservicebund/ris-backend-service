import httpClient, { ServiceResponse } from "./httpClient"
import { CitationType } from "@/domain/citationType"
import { Court, Procedure, DocumentType } from "@/domain/documentUnit"
import { FieldOfLawNode } from "@/domain/fieldOfLaw"
import { NormAbbreviation } from "@/domain/normAbbreviation"
import {
  ComboboxInputModelType,
  ComboboxItem,
} from "@/shared/components/input/types"
import { Page } from "@/shared/components/Pagination.vue"
import errorMessages from "@/shared/i18n/errors.json"

enum Endpoint {
  documentTypes = "documenttypes",
  courts = "courts",
  citationTypes = "citationtypes",
  fieldOfLawSearchByIdentifier = "fieldsoflaw/search-by-identifier",
  risAbbreviations = `normabbreviation?pg=0&sz=30`,
  risAbbreviationsAwesome = `normabbreviation/search?pg=0&sz=30`,
  procedures = `procedure`,
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

// Function to debounce the actual HTTP request
function debounce(
  func: (
    endpoint: Endpoint,
    filter?: string,
    size?: number,
  ) => Promise<ServiceResponse<ComboboxItem[]>>,
  delay: number,
) {
  let timeoutId: NodeJS.Timeout

  return async function (
    endpoint: Endpoint,
    filter?: string,
    size?: number,
  ): Promise<ServiceResponse<ComboboxItem[]>> {
    clearTimeout(timeoutId)

    return new Promise<ServiceResponse<ComboboxItem[]>>(async (resolve) => {
      timeoutId = setTimeout(async () => {
        const result = await func(endpoint, filter, size)
        resolve(result)
      }, delay)
    })
  }
}

// Debounced version of the fetchFromEndpoint function
const debouncedFetch = debounce(fetchFromEndpoint, 400) // Set your desired delay in milliseconds

async function fetchFromEndpointDebounced(
  endpoint: Endpoint,
  filter?: string,
  size?: number,
) {
  return debouncedFetch(endpoint, filter, size)
}

// Modify your service to use the debounced function
const service: ComboboxItemService = {
  filterItems: (items: ComboboxItem[]) => (filter?: string) => {
    const filteredItems = filter
      ? items.filter((item) => item.label.includes(filter))
      : items
    return Promise.resolve({ status: 200, data: filteredItems })
  },
  getCourts: async (filter?: string) =>
    await fetchFromEndpointDebounced(Endpoint.courts, filter),
  getDocumentTypes: async (filter?: string) =>
    await fetchFromEndpointDebounced(Endpoint.documentTypes, filter),
  getFieldOfLawSearchByIdentifier: async (filter?: string) =>
    await fetchFromEndpointDebounced(
      Endpoint.fieldOfLawSearchByIdentifier,
      filter,
    ),
  getRisAbbreviations: async (filter?: string) =>
    await fetchFromEndpointDebounced(Endpoint.risAbbreviations, filter),
  getRisAbbreviationsAwesome: async (filter?: string) =>
    await fetchFromEndpointDebounced(Endpoint.risAbbreviationsAwesome, filter),
  getCitationTypes: async (filter?: string) =>
    await fetchFromEndpointDebounced(Endpoint.citationTypes, filter),
  getProcedures: async (filter?: string) =>
    await fetchFromEndpointDebounced(Endpoint.procedures, filter, 10),
}

export default service
