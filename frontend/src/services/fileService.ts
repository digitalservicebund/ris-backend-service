import httpClient, { ServiceResponse } from "./httpClient"
import DocumentUnit from "@/domain/documentUnit"

interface FileService {
  upload(
    documentUnitUuid: string,
    file: File
  ): Promise<ServiceResponse<DocumentUnit>>
  delete(documentUnitUuid: string): Promise<ServiceResponse<unknown>>
  getDocxFileAsHtml(uuid: string): Promise<ServiceResponse<string>>
}

const service: FileService = {
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
      `caselaw/documentunits/${documentUnitUuid}/file`,
      {
        headers: {
          "Content-Type":
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
          "X-Filename": file.name,
        },
      },
      file
    )
    if (response.status === 413) {
      response.error = {
        title: "Die Datei darf max. 20 MB groß sein.",
        description: "Bitte laden Sie eine kleinere Datei hoch.",
      }
    } else if (response.status === 415) {
      response.error = {
        title: "Das ausgewählte Dateiformat ist nicht korrekt.",
        description:
          "Versuchen Sie eine .docx-Version dieser Datei hochzuladen.",
      }
    } else if (response.status >= 300) {
      response.error = {
        title: "Leider ist ein Fehler aufgetreten.",
        description:
          "Bitte versuchen Sie es zu einem späteren Zeitpunkt erneut.",
      }
    } else {
      response.error = undefined
    }

    return response
  },

  async delete(documentUnitUuid: string) {
    const response = await httpClient.delete(
      `caselaw/documentunits/${documentUnitUuid}/file`
    )
    response.error =
      response.status >= 300
        ? { title: "Datei konnte nicht gelöscht werden." }
        : undefined

    return response
  },

  async getDocxFileAsHtml(uuid: string) {
    const response = await httpClient.get<string>(
      `caselaw/documentunits/${uuid}/docx`
    )
    response.error =
      response.status >= 300
        ? { title: "Docx konnte nicht als html geladen werden." }
        : undefined

    return response
  },
}

export default service
