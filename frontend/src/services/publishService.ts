import httpClient, { ServiceResponse } from "./httpClient"
import PublicationHistoryRecord from "@/domain/xmlMail"
import errorMessages from "@/shared/i18n/errors.json"

interface PublishService {
  publishDocument(
    documentUnitUuid: string,
  ): Promise<ServiceResponse<PublicationHistoryRecord>>
  getPublicationLog(
    documentUnitUuid: string,
  ): Promise<ServiceResponse<PublicationHistoryRecord[]>>
  getPreview(
    documentUnitUuid: string,
  ): Promise<ServiceResponse<PublicationHistoryRecord>>
}

const service: PublishService = {
  async publishDocument(documentUnitUuid: string) {
    const response = await httpClient.put<string, PublicationHistoryRecord>(
      `caselaw/documentunits/${documentUnitUuid}/publish`,
      {
        headers: { "Content-Type": "text/plain" },
      },
    )

    if (response.status >= 300 || Number(response.data?.statusCode) >= 300) {
      response.status = response.data?.statusCode
        ? Number(response.data.statusCode)
        : response.status

      let description =
        "Die Dokumentationseinheit kann nicht veröffentlicht werden."
      if (
        response.data?.statusMessages &&
        response.data.statusMessages.length > 0
      ) {
        description += '<ul class="list-disc">'
      }
      response.data?.statusMessages?.forEach(
        (value) =>
          (description +=
            '<li class="ds-body-02-reg font-bold ml-[1rem] list-item">' +
            value +
            "</li>"),
      )
      if (
        response.data?.statusMessages &&
        response.data.statusMessages.length > 0
      ) {
        description += "</ul>"
      }

      response.error = {
        title: errorMessages.DOCUMENT_UNIT_PUBLISH_FAILED.title,
        description,
      }
    }

    return response
  },

  async getPublicationLog(documentUnitUuid: string) {
    const response = await httpClient.get<PublicationHistoryRecord[]>(
      `caselaw/documentunits/${documentUnitUuid}/publish`,
    )

    response.error =
      response.status >= 300
        ? {
            title: errorMessages.DOCUMENT_UNIT_LOADING_PUBLICATION_FAILED.title,
            description:
              errorMessages.DOCUMENT_UNIT_LOADING_PUBLICATION_FAILED
                .description,
          }
        : undefined

    return response
  },

  async getPreview(documentUnitUuid: string) {
    const response = await httpClient.get<PublicationHistoryRecord>(
      `caselaw/documentunits/${documentUnitUuid}/preview-publication-xml`,
    )

    response.error =
      response.status >= 300
        ? {
            title: "Can't load publication preview",
            description: "TODO error",
          }
        : undefined

    return response
  },
}

export default service
