import httpClient from "./httpClient"
import DocumentUnit from "@/domain/documentUnit"
import { UploadStatus } from "@/domain/uploadStatus"

export default {
  async uploadFile(
    documentUnitUuid: string,
    file: File
  ): Promise<{ documentUnit?: DocumentUnit; status: UploadStatus }> {
    try {
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
      console.log("response", response)

      if (response.status === 201) {
        return { documentUnit: response.data, status: UploadStatus.SUCCESSED }
      }
      return { status: UploadStatus.FAILED }
    } catch (error: any) {
      console.log("landed in catch", error)
      if (error.response.status === 413) {
        return { status: UploadStatus.FILE_TOO_LARGE }
      }
      if (error.response.status === 415) {
        return { status: UploadStatus.WRONG_FILE_FORMAT }
      }
      return { status: UploadStatus.FAILED }
    }
  },
  async getDocxFileAsHtml(fileName: string) {
    try {
      const response = await httpClient.get<{ content: string }>(
        `documentunitdocx/${fileName}`
      )
      return response.data.content
    } catch (error) {
      throw new Error(`Could not get docx: ${error}`)
    }
  },
  async deleteFile(documentUnitUuid: string) {
    try {
      await httpClient.delete(`documentunits/${documentUnitUuid}/file`)
    } catch (error) {
      throw new Error(`Could not delete file: ${error}`)
    }
  },
}
