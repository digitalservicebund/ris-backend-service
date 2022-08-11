import api from "./api"
import DocUnit from "@/domain/docUnit"
import { UploadStatus } from "@/domain/uploadStatus"

export default {
  async uploadFile(
    docUnitUuid: string,
    file: File
  ): Promise<{ docUnit?: DocUnit; status: UploadStatus }> {
    return await api()
      .put(`docunits/${docUnitUuid}/file`, file, {
        headers: {
          "Content-Type":
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
          "X-Filename": file.name,
        },
      })
      .then((response) => {
        if (response.status === 201) {
          return { docUnit: response.data, status: UploadStatus.SUCCESSED }
        }

        return { status: UploadStatus.FAILED }
      })
      .catch((error) => {
        if (error.response.status === 413) {
          return { status: UploadStatus.FILE_TOO_LARGE }
        }

        return { status: UploadStatus.FAILED }
      })
  },
  async getDocxFileAsHtml(fileName: string) {
    try {
      const response = await api().get(`docunitdocx/${fileName}`)
      return response.data.content
    } catch (error) {
      throw new Error(`Could not get docx: ${error}`)
    }
  },
  async deleteFile(docUnitUuid: string) {
    try {
      await api().delete(`docunits/${docUnitUuid}/file`)
    } catch (error) {
      throw new Error(`Could not delete file: ${error}`)
    }
  },
  async getAllDocxFiles() {
    try {
      const response = await api().get("docunitdocx")
      return response.data
    } catch (error) {
      throw new Error(`Could not get all docx files: ${error}`)
    }
  },
  async getEmailInfos(docUnitUuid: string) {
    try {
      const response = await api().put(`docunits/${docUnitUuid}/publish`)
      return response.data
    } catch (error) {
      throw new Error(`Could not get email from server: ${error}`)
    }
  },
}
