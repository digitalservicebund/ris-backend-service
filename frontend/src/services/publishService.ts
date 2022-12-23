import httpClient, { ServiceResponse } from "./httpClient"
import XmlMail from "@/domain/xmlMail"

interface publishService {
  publishDocument(
    documentUnitUuid: string,
    receiverEmail: string
  ): Promise<ServiceResponse<XmlMail>>
  getLastPublishedXML(
    documentUnitUuid: string
  ): Promise<ServiceResponse<XmlMail>>
}

const service: publishService = {
  async publishDocument(documentUnitUuid: string, receiverEmail: string) {
    const response = await httpClient.put<string, XmlMail>(
      `caselaw/documentunits/${documentUnitUuid}/publish`,
      {
        headers: { "Content-Type": "text/plain" },
      },
      receiverEmail
    )

    if (response.status >= 300 || Number(response.data?.statusCode) >= 300) {
      response.status = response.data?.statusCode
        ? Number(response.data.statusCode)
        : response.status

      response.error = {
        title: "Leider ist ein Fehler aufgetreten.",
        description:
          "Die Dokumentationseinheit kann nicht veröffentlicht werden.",
      }
    }

    return response
  },

  async getLastPublishedXML(documentUnitUuid: string) {
    const response = await httpClient.get<XmlMail>(
      `caselaw/documentunits/${documentUnitUuid}/publish`
    )

    response.error =
      response.status >= 300
        ? {
            title: "Fehler beim Laden der letzten Veröffentlichung",
            description:
              "Die Daten der letzten Veröffentlichung konnten nicht geladen werden.",
          }
        : undefined

    return response
  },
}

export default service
