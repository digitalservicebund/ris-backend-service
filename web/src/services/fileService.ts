import httpClient, { ServiceResponse } from "./httpClient"
import DocumentUnit from "@/domain/documentUnit"

interface fileService {
  upload(
    documentUnitUuid: string,
    file: File
  ): Promise<ServiceResponse<DocumentUnit>>
  delete(documentUnitUuid: string): Promise<ServiceResponse<unknown>>
  getDocxFileAsHtml(fileName: string): Promise<ServiceResponse<string>>
}

const service: fileService = {
  async upload(documentUnitUuid: string, file: File) {
    const extension = file.name?.split(".").pop()
    if (!extension || extension.toLowerCase() !== "docx") {
      return {
        status: 415,
        error: {
          title: "Das ausgewählte Dateiformat ist nicht korrekt.",
          description:
            "Versuchen Sie eine .docx-Version dieser Datei hochzuladen.",
        },
      }
    }

    const response = await httpClient.put<File, DocumentUnit>(
      `documentunits/${documentUnitUuid}/file`,
      {
        headers: {
          "Content-Type":
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
          "X-Filename": file.name,
        },
      },
      file
    )
    response.error =
      response.status === 413
        ? {
            title: "Die Datei darf max. 20 MB groß sein.",
            description: "Bitte laden Sie eine kleinere Datei hoch.",
          }
        : response.status === 415
        ? {
            title: "Das ausgewählte Dateiformat ist nicht korrekt.",
            description:
              "Versuchen Sie eine .docx-Version dieser Datei hochzuladen.",
          }
        : response.status >= 300
        ? {
            title: "Leider ist ein Fehler aufgetreten.",
            description:
              "Bitte versuchen Sie es zu einem späteren Zeitpunkt erneut.",
          }
        : undefined

    return response
  },

  async delete(documentUnitUuid: string) {
    const response = await httpClient.delete(
      `documentunits/${documentUnitUuid}/file`
    )
    response.error =
      response.status >= 300
        ? { title: "Datei konnte nicht gelöscht werden." }
        : undefined

    return response
  },

  async getDocxFileAsHtml(fileName: string) {
    const response = await httpClient.get<string>(
      `documentunitdocx/${fileName}`
    )
    response.error =
      response.status >= 300
        ? { title: "Docx konnte nicht als html geladen werden." }
        : undefined

    return response
  },
}

export default service
