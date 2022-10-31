import httpClient, { ServiceResponse } from "./httpClient"
import { DocumentType } from "@/domain/lookupTables"

interface LookupTableService {
  getAllDocumentTypes(): Promise<ServiceResponse<DocumentType[]>>
}

const service: LookupTableService = {
  async getAllDocumentTypes() {
    const response = await httpClient.get<DocumentType[]>(
      "lookuptable/documentTypes"
    )
    if (response.status >= 300) {
      response.error = {
        title: "Dokumenttypen konnten nicht geladen werden.",
      }
    }
    return response
  },
}

export default service
