import httpClient, { ServiceResponse } from "./httpClient"
import errorMessages from "@/i18n/errors.json"

interface PublishDocumentationUnitService {
  publishDocument(documentUnitUuid: string): Promise<ServiceResponse<void>>
  withdrawDocument(documentUnitUuid: string): Promise<ServiceResponse<void>>
}

const service: PublishDocumentationUnitService = {
  async publishDocument(documentUnitUuid: string) {
    const response = await httpClient.put<string, void>(
      `caselaw/documentunits/${documentUnitUuid}/publish`,
    )

    if (response.status >= 300) {
      const description =
        "Die Dokumentationseinheit konnte nicht veröffentlicht werden."

      response.error = {
        title: errorMessages.DOCUMENT_UNIT_HANDOVER_FAILED.title,
        description,
      }
    }

    return response
  },
  async withdrawDocument(documentUnitUuid: string) {
    const response = await httpClient.put<string, void>(
      `caselaw/documentunits/${documentUnitUuid}/withdraw`,
    )

    if (response.status >= 300) {
      const description =
        "Die Dokumentationseinheit konnte nicht zurückgezogen werden."

      response.error = {
        title: errorMessages.SERVER_ERROR.title,
        description,
      }
    }

    return response
  },
}

export default service
