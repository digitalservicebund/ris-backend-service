import { storeToRefs } from "pinia"
import { ref, computed, Ref } from "vue"
import { Attachment } from "@/domain/attachment"
import { Decision } from "@/domain/decision"
import attachmentService from "@/services/attachmentService"
import { ServiceResponse } from "@/services/httpClient"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

export function useAttachments(
  emit:
    | (((event: "attachmentsUploaded", anySuccessful: boolean) => void) &
        ((event: "attachmentIndexSelected", index: number) => void) &
        ((event: "attachmentIndexDeleted", index: number) => void))
    | undefined,
  options: {
    getList: (decision: Decision) => Attachment[]
    setList: (decision: Decision, newValues: Attachment[]) => void
    uploadFn: (uuid: string, file: File) => Promise<unknown>
  },
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
      fileToDelete.s3path,
    )
    if (status < 300) {
      if (emit) {
        emit("attachmentIndexDeleted", attachments.value.indexOf(fileToDelete))
      }
      await store.loadDocumentUnit(store.documentUnit!.documentNumber)
    } else {
      errors.value = [
        "Datei konnte nicht gelöscht werden. Bitte versuchen Sie es erneut oder wenden Sie sich an den Support.",
      ]
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
        const response = (await options.uploadFn(
          decision.value.uuid,
          file,
        )) as ServiceResponse<unknown>
        if (response.status === 200 && response.data) {
          anySuccessful = true
        } else if (response.error?.title) {
          errors.value.push(
            `${file.name} ${response.error.title} ${response.error.description}`,
          )
        }
      }
    } finally {
      isLoading.value = false
      if (emit) {
        emit("attachmentsUploaded", anySuccessful)
      }
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
