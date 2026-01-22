import httpClient, { ServiceResponse } from "./httpClient"
import { Attachment } from "@/domain/attachment"
import { Docx2HTML } from "@/domain/docx2html"
import errorMessages from "@/i18n/errors.json"

interface AttachmentService {
  uploadOriginalDocument(
    documentUnitUuid: string,
    file: File,
  ): Promise<ServiceResponse<Docx2HTML>>

  uploadOtherAttachment(
    documentUnitUuid: string,
    file: File,
  ): Promise<ServiceResponse<unknown>>

  download(
    documentUnitUuid: string,
    attachment: Attachment,
  ): Promise<ServiceResponse<unknown>>

  delete(
    documentUnitUuid: string,
    s3path: string,
  ): Promise<ServiceResponse<unknown>>

  getAttachmentAsHtml(
    uuid: string,
    s3path: string | undefined,
    format: string,
  ): Promise<ServiceResponse<Docx2HTML>>
}

const service: AttachmentService = {
  async uploadOriginalDocument(documentUnitUuid: string, file: File) {
    const extension = file.name?.split(".").pop()
    if (!extension || extension.toLowerCase() !== "docx") {
      return {
        status: 415,
        error: {
          title: errorMessages.WRONG_MEDIA_TYPE_DOCX_REQUIRED.title,
          description: errorMessages.WRONG_MEDIA_TYPE_DOCX_REQUIRED.description,
        },
      }
    }
    if (file.size > 20 * 1024 * 1024)
      return {
        status: 413,
        error: {
          title: errorMessages.ORIGINAL_DOCUMENT_TOO_LARGE_CASELAW.title,
          description:
            errorMessages.ORIGINAL_DOCUMENT_TOO_LARGE_CASELAW.description,
        },
      }

    const form = new FormData()
    form.append("file", file)

    const response = await httpClient.put<FormData, Docx2HTML>(
      `caselaw/documentunits/${documentUnitUuid}/original-file`,
      {
        headers: {
          "Content-Type":
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
          "X-Filename": file.name,
          "X-Filesize": `${file.size}`,
        },
      },
      form,
    )
    if (response.status === 413) {
      response.error = {
        title: errorMessages.ORIGINAL_DOCUMENT_TOO_LARGE_CASELAW.title,
        description:
          errorMessages.ORIGINAL_DOCUMENT_TOO_LARGE_CASELAW.description,
      }
    } else if (response.status === 415) {
      response.error = {
        title: errorMessages.WRONG_MEDIA_TYPE_DOCX_REQUIRED.title,
        description: errorMessages.WRONG_MEDIA_TYPE_DOCX_REQUIRED.description,
      }
    } else if (response.status === 422) {
      response.error = {
        title: errorMessages.DOCX_PARSING_ERROR.title,
        description: errorMessages.DOCX_PARSING_ERROR.description,
      }
    } else if (response.status === 403) {
      response.error = {
        title: errorMessages.NOT_ALLOWED.title,
        description: errorMessages.NOT_ALLOWED.description,
      }
    } else if (response.status >= 300) {
      response.error = {
        title: errorMessages.SERVER_ERROR.title,
        description: errorMessages.SERVER_ERROR.description,
      }
    } else {
      response.error = undefined
    }

    return response
  },

  async uploadOtherAttachment(documentUnitUuid: string, file: File) {
    if (file.size > 100 * 1024 * 1024)
      return {
        status: 413,
        error: {
          title: errorMessages.OTHER_FILE_TOO_LARGE_CASELAW.title,
          description: errorMessages.OTHER_FILE_TOO_LARGE_CASELAW.description,
        },
      } as ServiceResponse<unknown>

    const form = new FormData()
    form.append("file", file)

    const response = await httpClient.put<FormData, unknown>(
      `caselaw/documentunits/${documentUnitUuid}/other-file`,
      {
        headers: {
          "X-Filename": file.name,
          "X-Filesize": `${file.size}`,
        },
      },
      form,
    )
    if (response.status === 413) {
      response.error = {
        title: errorMessages.OTHER_FILE_TOO_LARGE_CASELAW.title,
        description: errorMessages.OTHER_FILE_TOO_LARGE_CASELAW.description,
      }
    } else if (response.status === 403) {
      response.error = {
        title: errorMessages.NOT_ALLOWED.title,
        description: errorMessages.NOT_ALLOWED.description,
      }
    } else if (response.status >= 300) {
      response.error = {
        title: errorMessages.SERVER_ERROR.title,
        description: errorMessages.SERVER_ERROR.description,
      }
    } else {
      response.error = undefined
    }

    return response
  },

  async download(documentUnitUuid: string, attachment: Attachment) {
    const response = await httpClient.get<BlobPart>(
      `caselaw/documentunits/${documentUnitUuid}/file/${attachment.id}`,
      { responseType: "blob" },
    )
    if (response.error || response.status > 300)
      return {
        status: 500,
        error: {
          title:
            "Datei konnte nicht heruntergeladen werden. Versuchen sie es erneut oder wenden Sie sich an den Support.",
        },
      }

    const blob = new Blob([response.data], {
      type: response?.headers?.["content-type"],
    })
    const url = URL.createObjectURL(blob)
    const link = document.createElement("a")
    link.href = url
    link.download = attachment.name
    document.body.appendChild(link)
    link.click()
    link.remove()
    URL.revokeObjectURL(url)
    return response
  },

  async delete(documentUnitUuid: string, s3path: string) {
    const response = await httpClient.delete(
      `caselaw/documentunits/${documentUnitUuid}/file/${s3path}`,
    )
    response.error =
      response.status >= 300
        ? { title: errorMessages.FILE_DELETE_FAILED.title }
        : undefined

    return response
  },

  async getAttachmentAsHtml(uuid: string, s3path: string, format: string) {
    const response = await httpClient.get<Docx2HTML>(
      `caselaw/documentunits/${uuid}/file`,
      {
        params: {
          s3Path: s3path ?? "",
          format: format ?? "",
        },
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
      },
    )
    response.error =
      response.status >= 300
        ? { title: errorMessages.DOCX_COULD_NOT_BE_LOADED.title }
        : undefined

    return response
  },
}

export default service
