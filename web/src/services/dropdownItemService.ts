import httpClient from "./httpClient"
import { DropdownItem, LookupTableEndpoint } from "@/domain"
import { Court } from "@/domain/documentUnit"
import { DocumentType } from "@/domain/lookupTables"

// TODO this should be wrapped in a ServiceResponse
interface DropdownItemService {
  fetch(
    endpoint: LookupTableEndpoint,
    searchStr?: string
  ): Promise<DropdownItem[]>
}

const service: DropdownItemService = {
  async fetch(endpoint: LookupTableEndpoint, searchStr?: string) {
    let response
    switch (endpoint) {
      case LookupTableEndpoint.documentTypes: {
        response = await httpClient.get<DocumentType[]>(
          `caselaw/${endpoint}`,
          searchStr ? { params: { searchStr } } : undefined
        )
        if (response.status >= 300 || !response.data) {
          return []
        }
        return response.data.map((item) => ({
          text: item.jurisShortcut + " - " + item.label,
          value: item.label,
        }))
      }
      case LookupTableEndpoint.courts: {
        response = await httpClient.get<Court[]>(
          `caselaw/${endpoint}`,
          searchStr ? { params: { searchStr } } : undefined
        )
        if (response.status >= 300 || !response.data) {
          return []
        }
        return response.data.map((item) => ({
          text: item.label,
          value: item,
        }))
      }
      default: {
        return []
      }
    }
  },
}

export default service
