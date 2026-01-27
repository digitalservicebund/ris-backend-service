import { storeToRefs } from "pinia"
import { ref, computed, Ref } from "vue"
import { Attachment } from "@/domain/attachment"
import { Decision } from "@/domain/decision"
import errorMessages from "@/i18n/errors.json"
import attachmentService from "@/services/attachmentService"
import { ServiceResponse } from "@/services/httpClient"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

interface AttachmentHandlers {
  getList: (decision: Decision) => Attachment[]
  setList: (decision: Decision, newValues: Attachment[]) => void
  uploadFn: (uuid: string, file: File) => Promise<ServiceResponse<unknown>>
}

interface AttachmentEvents {
  attachmentsUploaded?: (anySuccessful: boolean) => void
  attachmentIndexDeleted?: (index: number) => void
  attachmentIndexSelected?: (index: number) => void
}

export function useAttachments(
  events: AttachmentEvents,
  options: AttachmentHandlers,
) {
  const store = useDocumentUnitStore()
  const { documentUnit: decision } = storeToRefs(store) as {
    documentUnit: Ref<Decision | undefined>
  }

  const errors = ref<string[]>([])
  const isLoading = ref(false)
  const attachmentIdsWithActiveDownload = ref<string[]>([])

  const attachments = computed({
    get: () => (decision.value ? options.getList(decision.value) : []),
    set: (newValues) => {
      if (decision.value) options.setList(decision.value, newValues)
    },
  })

  const hasAttachments = computed<boolean>(() => attachments.value.length > 0)

  async function handleOnDelete(fileToDelete: Attachment) {
    errors.value = []
    if (!decision.value) return
    if (!fileToDelete.s3path) {
      console.error("file path is undefined", fileToDelete)
      return
    }
    const { status } = await attachmentService.delete(
      decision.value.uuid,
      fileToDelete.id,
    )
    if (status < 300) {
      events.attachmentIndexDeleted?.(attachments.value.indexOf(fileToDelete))
      await store.loadDocumentUnit(store.documentUnit!.documentNumber)
    } else {
      errors.value = [errorMessages.FILE_DELETE_FAILED.title]
    }
  }

  async function handleOnDownload(attachment: Attachment) {
    errors.value = []
    if (!decision.value) return
    attachmentIdsWithActiveDownload.value = [
      ...attachmentIdsWithActiveDownload.value,
      attachment.id,
    ]
    const response = await attachmentService.download(
      decision.value.uuid,
      attachment,
    )
    if (response.error) errors.value = [response.error.title]
    attachmentIdsWithActiveDownload.value =
      attachmentIdsWithActiveDownload.value.filter((id) => id !== attachment.id)
  }

  async function upload(files: FileList) {
    errors.value = []
    if (!decision.value) return
    let anySuccessful = false
    try {
      for (const file of Array.from(files)) {
        isLoading.value = true
        const response = await options.uploadFn(decision.value.uuid, file)
        if (response.status === 200 && response.data) {
          anySuccessful = true
        } else if (response.error?.title) {
          errors.value.push(
            [
              `'${file.name}'`,
              response.error?.title,
              response.error?.description,
            ]
              .filter(Boolean)
              .join(" "),
          )
        }
      }
    } finally {
      isLoading.value = false
      events.attachmentsUploaded?.(anySuccessful)
      await store.loadDocumentUnit(store.documentUnit!.documentNumber)
    }
    return anySuccessful
  }

  return {
    errors,
    isLoading,
    attachments,
    hasAttachments,
    attachmentIdsWithActiveDownload,
    handleOnDelete,
    handleOnDownload,
    upload,
  }
}
