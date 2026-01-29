import { createTestingPinia } from "@pinia/testing"
import { setActivePinia, storeToRefs } from "pinia"
import { beforeEach, describe, expect, it, vi } from "vitest"
import { useAttachments } from "@/composables/useAttachments"
import { Attachment } from "@/domain/attachment"
import { Decision } from "@/domain/decision"
import errorMessages from "@/i18n/errors.json"
import attachmentService from "@/services/attachmentService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

describe("useAttachments", () => {
  let store: ReturnType<typeof useDocumentUnitStore>
  const updateDocumentUnitMock = vi.fn()

  beforeEach(() => {
    setActivePinia(createTestingPinia())
    store = createDocumentUnitStore(decision)
    vi.spyOn(store, "updateDocumentUnit").mockImplementation(
      updateDocumentUnitMock,
    )
    vi.resetAllMocks()
  })

  describe("computed properties", () => {
    it("attachments returns correct list", () => {
      const events = {}
      const options = {
        getList: (d: Decision) => d.attachments,
        setList: vi.fn(),
        uploadFn: vi.fn(),
      }

      const { attachments } = useAttachments(events, options)

      expect(attachments.value).toEqual([mockAttachment])
    })

    it("hasAttachments with attachments returns true", () => {
      const events = {}
      const options = {
        getList: (d: Decision) => d.attachments,
        setList: vi.fn(),
        uploadFn: vi.fn(),
      }

      const { hasAttachments } = useAttachments(events, options)

      expect(hasAttachments.value).toBe(true)
    })

    it("hasAttachments without attachments returns false", () => {
      const decisionWithoutAttachment = new Decision("test-uuid", {
        attachments: [],
      })
      createDocumentUnitStore(decisionWithoutAttachment)
      const events = {}
      const options = {
        getList: (d: Decision) => d.attachments,
        setList: vi.fn(),
        uploadFn: vi.fn(),
      }

      const { hasAttachments } = useAttachments(events, options)

      expect(hasAttachments.value).toBe(false)
    })
  })

  describe("handleOnDelete", () => {
    it("does nothing without decision", async () => {
      createDocumentUnitStore(undefined)
      const events = {}
      const options = { getList: vi.fn(), setList: vi.fn(), uploadFn: vi.fn() }
      const { handleOnDelete } = useAttachments(events, options)

      await handleOnDelete({ id: "1" } as Attachment)

      expect(deleteAttachmentMock).not.toHaveBeenCalled()
    })

    it("emits event and reloads store on success", async () => {
      const mockEvents = {
        attachmentIndexDeleted: vi.fn(),
      }
      const options = {
        getList: (d: Decision) => d.attachments,
        setList: vi.fn(),
        uploadFn: vi.fn(),
      }
      deleteAttachmentMock.mockResolvedValue({ status: 200, data: {} })
      const { handleOnDelete } = useAttachments(mockEvents, options)

      await handleOnDelete(mockAttachment)

      expect(deleteAttachmentMock).toHaveBeenCalledWith("test-uuid", "123")
      expect(mockEvents.attachmentIndexDeleted).toHaveBeenCalledWith(0)
      expect(store.loadDocumentUnit).toHaveBeenCalledWith(
        decision.documentNumber,
      )
    })

    it("sets error on delete failure", async () => {
      const events = {}
      const options = { getList: vi.fn(), setList: vi.fn(), uploadFn: vi.fn() }
      deleteAttachmentMock.mockResolvedValue({
        status: 400,
        error: errorMessages.FILE_DELETE_FAILED,
      })
      const { handleOnDelete, errors } = useAttachments(events, options)

      await handleOnDelete(mockAttachment)

      expect(errors.value).toEqual([errorMessages.FILE_DELETE_FAILED.title])
    })
  })

  describe("handleOnDownload", () => {
    it("does nothing without decision", async () => {
      createDocumentUnitStore(undefined)
      const events = {}
      const options = { getList: vi.fn(), setList: vi.fn(), uploadFn: vi.fn() }
      downloadAttachmentMock.mockResolvedValue({ status: 200, data: {} })
      const { handleOnDownload } = useAttachments(events, options)

      await handleOnDownload(mockAttachment)

      expect(downloadAttachmentMock).not.toHaveBeenCalled()
    })

    it("removes attachment from active downloads", async () => {
      const events = {}
      const options = { getList: vi.fn(), setList: vi.fn(), uploadFn: vi.fn() }
      downloadAttachmentMock.mockResolvedValue({ status: 200, data: {} })
      const { handleOnDownload, attachmentIdsWithActiveDownload } =
        useAttachments(events, options)

      await handleOnDownload(mockAttachment)

      expect(attachmentIdsWithActiveDownload.value).not.toContain("1")
      expect(downloadAttachmentMock).toHaveBeenCalledWith(
        "test-uuid",
        mockAttachment,
      )
    })

    it("sets FILE_DOWNLOAD_FAILED error on download failure", async () => {
      const events = {}
      const options = { getList: vi.fn(), setList: vi.fn(), uploadFn: vi.fn() }

      downloadAttachmentMock.mockResolvedValue({
        status: 400,
        error: errorMessages.FILE_DOWNLOAD_FAILED,
      })
      const { handleOnDownload, errors } = useAttachments(events, options)

      await handleOnDownload(mockAttachment)

      expect(errors.value).toEqual([errorMessages.FILE_DOWNLOAD_FAILED.title])
    })
  })

  describe("upload", () => {
    it("returns false if no decision", async () => {
      const events = {}
      const options = {
        getList: vi.fn(),
        setList: vi.fn(),
        uploadFn: uploadAttachmentMock,
      }
      createDocumentUnitStore(undefined)
      const fileList = {
        length: 1,
        0: new File(["test"], "test.pdf"),
        item: (i: number) => [new File(["test"], "test.pdf")][i],
        [Symbol.iterator]: function* () {
          yield new File(["test"], "test.pdf")
        },
      } as unknown as FileList
      const { upload } = useAttachments(events, options)

      const result = await upload(fileList)

      expect(result).toBeUndefined()
      expect(uploadAttachmentMock).not.toHaveBeenCalled()
    })

    it("sets isLoading and emits success on successful upload", async () => {
      const mockEvents = { attachmentsUploaded: vi.fn() }
      const mockUploadFn = vi.fn().mockResolvedValue({ status: 200, data: {} })
      const options = {
        getList: vi.fn(),
        setList: vi.fn(),
        uploadFn: mockUploadFn,
      }

      const file = new File(["foo"], "test.pdf")
      const files = {
        length: 1,
        [Symbol.iterator]: function* () {
          yield file
        },
      }

      const { upload, isLoading } = useAttachments(mockEvents, options)

      const result = await upload(files as FileList)

      expect(isLoading.value).toBe(false)
      expect(mockUploadFn).toHaveBeenCalledWith("test-uuid", file)
      expect(mockEvents.attachmentsUploaded).toHaveBeenCalledWith(true)
      expect(store.loadDocumentUnit).toHaveBeenCalledWith(
        decision.documentNumber,
      )
      expect(result).toBe(true)
    })

    it("adds upload error to errors array", async () => {
      const mockEvents = { attachmentsUploaded: vi.fn() }
      const mockUploadFn = vi.fn().mockResolvedValue({
        status: 400,
        error: errorMessages.FILE_UPLOAD_FAILED,
      })
      const options = {
        getList: vi.fn(),
        setList: vi.fn(),
        uploadFn: mockUploadFn,
      }
      const file = new File(["foo"], "test.pdf")
      const files = {
        length: 1,
        [Symbol.iterator]: function* () {
          yield file
        },
      }
      const { upload, errors } = useAttachments(mockEvents, options)

      const result = await upload(files as FileList)

      expect(mockEvents.attachmentsUploaded).toHaveBeenCalledWith(false)
      expect(store.loadDocumentUnit).toHaveBeenCalledWith(
        decision.documentNumber,
      )
      expect(result).toBe(false)
      expect(errors.value).toEqual([
        "'test.pdf' " + errorMessages.FILE_UPLOAD_FAILED.title,
      ])
    })
  })
})
const mockAttachment: Attachment = {
  id: "123",
  name: "test-file.docx",
  format: "docx",
  uploadTimestamp: "11.04.2024",
}
const decision = new Decision("test-uuid", {
  documentNumber: "documentNumber",
  attachments: [mockAttachment],
  otherAttachments: [],
})
const deleteAttachmentMock = vi.spyOn(attachmentService, "delete")
const downloadAttachmentMock = vi.spyOn(attachmentService, "download")
const uploadAttachmentMock = vi.spyOn(
  attachmentService,
  "uploadOriginalDocument",
)
const createDocumentUnitStore = (decision?: Decision) => {
  const store = useDocumentUnitStore()
  const { documentUnit } = storeToRefs(store)
  documentUnit.value = decision
  return store
}
