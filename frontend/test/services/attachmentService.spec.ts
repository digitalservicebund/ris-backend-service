import { Attachment } from "@/domain/attachment"
import errorMessages from "@/i18n/errors.json"
import service from "@/services/attachmentService"
import HttpClient, { ServiceResponse } from "@/services/httpClient"

const httpGetMock = vi.spyOn(HttpClient, "get")
const httpPutMock = vi.spyOn(HttpClient, "put")
const httpDeleteMock = vi.spyOn(HttpClient, "delete")

describe("attachmentService", () => {
  describe("uploadOriginalDocument", () => {
    it("should return 415 if not docx file", async () => {
      const testFile = new File(["foo"], "test.pdf")
      const result = await service.uploadOriginalDocument("123", testFile)
      expect(result.status).toBe(415)
      expect(result.error).toEqual(errorMessages.WRONG_MEDIA_TYPE_DOCX_REQUIRED)
    })

    it("should succeed", async () => {
      const testFile = new File(["foo"], "test.docx")
      const mockResponse = { status: 200, data: {} }
      httpPutMock.mockResolvedValue(mockResponse)
      const expectedHeaders = {
        headers: {
          "Content-Type":
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
          "X-Filename": "test.docx",
          "X-Filesize": "3",
        },
      }

      await service.uploadOriginalDocument("123", testFile)

      expect(httpPutMock).toHaveBeenCalledWith(
        "caselaw/documentunits/123/original-file",
        expectedHeaders,
        expect.any(FormData),
      )
    })

    it.each([
      { status: 413, expectedError: "ORIGINAL_DOCUMENT_TOO_LARGE_CASELAW" },
      { status: 415, expectedError: "WRONG_MEDIA_TYPE_DOCX_REQUIRED" },
      { status: 422, expectedError: "DOCX_PARSING_ERROR" },
      { status: 403, expectedError: "NOT_ALLOWED" },
      { status: 500, expectedError: "SERVER_ERROR" },
    ])(
      "should map HTTP $status to correct error message",
      async ({ status, expectedError }) => {
        const testFile = new File(["foo"], "test.docx")
        httpPutMock.mockResolvedValue({ status } as ServiceResponse<unknown>)

        const result = await service.uploadOriginalDocument("123", testFile)

        expect(result.status).toBe(status)
        expect(result.error).toEqual(
          errorMessages[expectedError as keyof typeof errorMessages],
        )
      },
    )
  })

  describe("uploadOtherAttachment", () => {
    it("should return 413 if file is larger than 100 MB", async () => {
      const fileBits_101mb = [new Uint8Array(105472512)] // 101 * 1024 * 1024 Bytes
      const largeFile = new File(fileBits_101mb, "test.pdf", {})

      const result = await service.uploadOtherAttachment("123", largeFile)

      expect(result.status).toBe(413)
      expect(result.error).toEqual(errorMessages.OTHER_FILE_TOO_LARGE_CASELAW)
    })

    it("should upload file smaller than 100 MB", async () => {
      const smallFile = new File(["foo"], "test.pdf")
      httpPutMock.mockResolvedValue({ status: 200 } as ServiceResponse<unknown>)
      const expectedHeaders = {
        headers: {
          "X-Filename": "test.pdf",
          "X-Filesize": "3",
        },
      }

      await service.uploadOtherAttachment("123", smallFile)

      expect(httpPutMock).toHaveBeenCalledWith(
        "caselaw/documentunits/123/other-file",
        expectedHeaders,
        expect.any(FormData),
      )
    })

    it("should map HTTP 413 to correct error message", async () => {
      const largeFile = new File(["foo"], "test.pdf")
      httpPutMock.mockResolvedValue({ status: 413 } as ServiceResponse<unknown>)

      const result = await service.uploadOtherAttachment("123", largeFile)

      expect(result.status).toBe(413)
      expect(result.error).toEqual(errorMessages.OTHER_FILE_TOO_LARGE_CASELAW)
    })

    it("should map HTTP 403 to correct error message", async () => {
      const file = new File(["foo"], "test.pdf")
      httpPutMock.mockResolvedValue({ status: 403 } as ServiceResponse<unknown>)

      const result = await service.uploadOtherAttachment("123", file)

      expect(result.status).toBe(403)
      expect(result.error).toEqual(errorMessages.NOT_ALLOWED)
    })

    it("should map HTTP >= 300 to server error", async () => {
      const file = new File(["foo"], "test.pdf")
      httpPutMock.mockResolvedValue({ status: 500 } as ServiceResponse<unknown>)

      const result = await service.uploadOtherAttachment("123", file)

      expect(result.status).toBe(500)
      expect(result.error).toEqual(errorMessages.SERVER_ERROR)
    })

    it("should set error to undefined on success (HTTP 200)", async () => {
      const file = new File(["foo"], "test.pdf")
      httpPutMock.mockResolvedValue({
        status: 200,
        data: {},
        error: undefined,
      } as ServiceResponse<unknown>)

      const result = await service.uploadOtherAttachment("123", file)

      expect(result.status).toBe(200)
      expect(result.error).toBeUndefined()
    })
  })

  describe("download", () => {
    it("should trigger browser download on success", async () => {
      const mockAttachment: Attachment = {
        id: "123",
        name: "test.pdf",
      } as Attachment

      const mockBlobData = new Uint8Array([1, 2, 3])
      const mockResponse = {
        status: 200,
        data: mockBlobData,
        headers: { "content-type": "application/pdf" },
      }
      httpGetMock.mockResolvedValue(mockResponse)

      const createObjectURLSpy = vi.spyOn(URL, "createObjectURL")
      const revokeObjectURLSpy = vi.spyOn(URL, "revokeObjectURL")

      const response = await service.download("123", mockAttachment)

      expect(createObjectURLSpy).toHaveBeenCalledOnce()
      expect(revokeObjectURLSpy).toHaveBeenCalledOnce()
      expect(response.data).toHaveLength(3)
      expect(response.status).toBe(200)
      expect(response.error).toBeUndefined()
      expect(response.headers).toBe(mockResponse.headers)
    })

    it("should return error on HTTP failure", async () => {
      httpGetMock.mockResolvedValue({ status: 404 } as ServiceResponse<unknown>)

      const mockAttachment: Attachment = {
        id: "123",
        name: "test.pdf",
      } as Attachment

      const result = await service.download("123", mockAttachment)

      expect(result.status).toBe(500)
      expect(result.error).toEqual(errorMessages.FILE_DOWNLOAD_FAILED)
    })
  })

  describe("delete", () => {
    it("should succeed", async () => {
      httpDeleteMock.mockResolvedValue({
        status: 200,
      } as ServiceResponse<unknown>)

      const result = await service.delete("123", "fileToDeleteId")

      expect(httpDeleteMock).toHaveBeenCalledWith(
        "caselaw/documentunits/123/file/fileToDeleteId",
      )
      expect(result.error).toBeUndefined()
    })

    it("should set FILE_DELETE_FAILED error on HTTP >= 300", async () => {
      httpDeleteMock.mockResolvedValue({
        status: 400,
      } as ServiceResponse<unknown>)
      const result = await service.delete("123", "fileToDeleteId")

      expect(result.error).toEqual(errorMessages.FILE_DELETE_FAILED)
    })
  })

  describe("getAttachmentAsHtml", () => {
    it("should call GET with correct params", async () => {
      httpGetMock.mockResolvedValue({ status: 200 } as ServiceResponse<unknown>)

      await service.getAttachmentAsHtml("123", "456")

      expect(httpGetMock).toHaveBeenCalledWith(
        "caselaw/documentunits/123/file/456/html",
        expect.objectContaining({
          headers: {
            Accept: "application/json",
            "Content-Type": "application/json",
          },
        }),
      )
    })

    it("should set DOCX_COULD_NOT_BE_LOADED error on HTTP >= 300", async () => {
      httpGetMock.mockResolvedValue({
        status: 400,
      } as ServiceResponse<unknown>)
      const result = await service.getAttachmentAsHtml("123", "456")

      expect(result.error).toEqual(errorMessages.DOCX_COULD_NOT_BE_LOADED)
    })
  })
})
