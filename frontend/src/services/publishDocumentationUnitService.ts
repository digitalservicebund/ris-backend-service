import httpClient, { ServiceResponse } from "./httpClient"
import { LdmlPreview } from "@/components/input/types"
import errorMessages from "@/i18n/errors.json"

interface PublishDocumentationUnitService {
  publishDocument(
    documentUnitUuid: string,
  ): Promise<ServiceResponse<PublicationResult>>
  withdrawDocument(documentUnitUuid: string): Promise<ServiceResponse<void>>
  getPreview(documentUnitUuid: string): Promise<ServiceResponse<LdmlPreview>>
}

const service: PublishDocumentationUnitService = {
  async publishDocument(documentUnitUuid: string) {
    const response = await httpClient.put<string, PublicationResult>(
      `caselaw/documentunits/${documentUnitUuid}/publish`,
    )

    if (response.status >= 400) {
      response.error = {
        title: "Fehler beim Veröffentlichen der Dokumentationseinheit.",
        description:
          "Die Dokumentationseinheit konnte nicht veröffentlicht werden.",
      }
    }

    return response
  },
  async withdrawDocument(documentUnitUuid: string) {
    const response = await httpClient.put<string, void>(
      `caselaw/documentunits/${documentUnitUuid}/withdraw`,
    )

    if (response.status >= 400) {
      response.error = {
        title: "Fehler beim Zurückziehen der Dokumentationseinheit.",
        description:
          "Die Dokumentationseinheit konnte nicht zurückgezogen werden",
      }
    }

    return response
  },
  async getPreview(documentUnitUuid: string) {
    const response = await httpClient.get<LdmlPreview>(
      `caselaw/documentunits/${documentUnitUuid}/preview-ldml`,
    )

    if (
      response.status >= 400 &&
      response.status < 500 &&
      response.status !== 422
    ) {
      response.error = {
        title: errorMessages.DOCUMENT_UNIT_LOADING_LDML_PREVIEW.title,
        description:
          errorMessages.DOCUMENT_UNIT_LOADING_LDML_PREVIEW.description +
          ": " +
          errorMessages.DOCUMENT_UNIT_NOT_ALLOWED.title,
      }
    } else if (response.status >= 300 || !response.data?.success) {
      response.error = {
        title: errorMessages.DOCUMENT_UNIT_LOADING_LDML_PREVIEW.title,
        description:
          response.data?.statusMessages &&
          response.data.statusMessages.length > 0
            ? errorMessages.DOCUMENT_UNIT_LOADING_LDML_PREVIEW.description +
              ": " +
              response.data?.statusMessages
            : errorMessages.DOCUMENT_UNIT_LOADING_LDML_PREVIEW.description +
              ".",
      }
    }

    return response
  },
}

export type PublicationResult = {
  relatedPendingProceedingsPublicationResult: "SUCCESS" | "ERROR" | "NO_ACTION"
}
export default service
