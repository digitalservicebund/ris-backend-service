import httpClient, { ServiceResponse } from "./httpClient"
import errorMessages from "@/i18n/errors.json"

interface PortalService {
  deleteFromPortal(documentNumber: string): Promise<ServiceResponse<unknown>>
}

const service: PortalService = {
  async deleteFromPortal(documentNumber: string) {
    const response = await httpClient.delete(`caselaw/portal/${documentNumber}`)
    if (response.status >= 300) {
      response.error = {
        title: errorMessages.DOCUMENT_UNIT_DELETE_FAILED.title,
      }
    }
    return response
  },
}

export default service
