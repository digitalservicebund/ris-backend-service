import httpClient, { ServiceResponse } from "./httpClient"

interface PublishDocumentationUnitService {
  publishDocument(documentUnitUuid: string): Promise<ServiceResponse<void>>
}

const service: PublishDocumentationUnitService = {
  async publishDocument(documentUnitUuid: string) {
    const response = await httpClient.put<string, void>(
      `caselaw/documentunits/${documentUnitUuid}/publish`,
    )

    if (response.status >= 400) {
      response.error = {
        title: "Fehler beim Veröffentlichen der Dokumentationseinheit",
        description:
          "Die Dokumentationseinheit konnte nicht veröffentlicht werden. Bitte versuchen Sie es erneut oder wenden Sie sich an den Support.",
      }
    }

    return response
  },
}

export default service
