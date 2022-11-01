import httpClient from "./httpClient"
import { DropdownItem, LookupTableEndpoint } from "@/domain"
import { DocumentType } from "@/domain/lookupTables"

interface LookupTableService {
  getAll(endpoint: LookupTableEndpoint): Promise<DropdownItem[]>
}

const service: LookupTableService = {
  async getAll(endpoint: LookupTableEndpoint) {
    const response = await httpClient.get<DocumentType[]>(endpoint)
    if (response.status >= 300 || !response.data) {
      return []
    }
    return response.data.map((item) => ({
      text: item.jurisShortcut + " - " + item.label,
      value: item.label,
    }))
  },
}

export default service
