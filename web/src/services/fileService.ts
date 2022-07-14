import api from "./api"
import DocUnit from "@/domain/docUnit"

export default {
  async uploadFile(docUnitUuid: string, file: File): Promise<DocUnit> {
    try {
      const response = await api().put(`docunits/${docUnitUuid}/file`, file, {
        headers: {
          "Content-Type":
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
          "X-Filename": file.name,
        },
      })
      return response.data
    } catch (error) {
      throw new Error(`Could not upload file: ${error}`)
    }
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
}
