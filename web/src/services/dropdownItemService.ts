import httpClient, { ServiceResponse } from "./httpClient"
import { DropdownItem, LookupTableEndpoint } from "@/domain"
import { Court } from "@/domain/documentUnit"

function createDropdownItems(
  responseData: DropdownType,
  endpoint: LookupTableEndpoint
): DropdownItem[] {
  switch (endpoint) {
    case LookupTableEndpoint.documentTypes: {
      return (responseData as DocumentType[]).map((item) => ({
        text: item.jurisShortcut + " - " + item.label,
        value: item.label,
      }))
    }
    case LookupTableEndpoint.courts: {
      return (responseData as Court[]).map((item) => ({
        text: item.label,
        value: item,
      }))
    }
  }
}

interface DropdownItemService {
  fetch(
    endpoint: LookupTableEndpoint,
    searchStr?: string
  ): Promise<ServiceResponse<DropdownItem[]>>
}

const service: DropdownItemService = {
  async fetch(endpoint: LookupTableEndpoint, searchStr?: string) {
    const response = await httpClient.get<DropdownType>(
      `caselaw/${endpoint}`,
      searchStr ? { params: { searchStr } } : undefined
    )
    if (response.data) {
      return {
        status: response.status,
        data: createDropdownItems(response.data, endpoint),
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
  },
}

export type DocumentType = {
  id: number
  jurisShortcut: string
  label: string
}

export type DropdownType = DocumentType[] | Court[]

export default service
