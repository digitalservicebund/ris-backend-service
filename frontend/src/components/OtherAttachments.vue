<script lang="ts" setup>
import { storeToRefs } from "pinia"
import Message from "primevue/message"
import { computed, ref, Ref } from "vue"
import AttachmentList from "@/components/AttachmentList.vue"
import FileUpload from "@/components/FileUpload.vue"
import TitleElement from "@/components/TitleElement.vue"
import { Attachment } from "@/domain/attachment"
import { Decision } from "@/domain/decision"
import attachmentService from "@/services/attachmentService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const store = useDocumentUnitStore()
const { documentUnit: decision } = storeToRefs(store) as {
  documentUnit: Ref<Decision | undefined>
}
const errors = ref<string[]>([])
const isUploading = ref(false)

const handleOnDelete = async (attachment: Attachment) => {
  errors.value = []
  if (attachment.s3path == undefined) {
    console.error("file path is undefined", attachment)
    return
  }

  const { status } = await attachmentService.delete(
    decision.value!.uuid,
    attachment.s3path,
  )
  if (status < 300) {
    await store.loadDocumentUnit(store.documentUnit!.documentNumber!)
  } else {
    errors.value = [
      "Datei konnte nicht gelöscht werden. Bitte versuchen Sie es erneut oder wenden Sie sich an den Support.",
    ]
  }
}

async function upload(files: FileList) {
  errors.value = []
  for (const file of Array.from(files)) {
    isUploading.value = true
    const response = await attachmentService.uploadOtherAttachment(
      decision.value!.uuid,
      file,
    )
    if (response.error?.title) {
      errors.value.push(
        file.name +
          " " +
          response.error?.title +
          " " +
          response.error?.description,
      )
    }
  }
  isUploading.value = false
  await store.loadDocumentUnit(store.documentUnit!.documentNumber!)
}

const attachmentIdsWithActiveDownload = ref<string[]>([])
async function handleOnDownload(attachment: Attachment) {
  errors.value = []
  attachmentIdsWithActiveDownload.value = [
    ...attachmentIdsWithActiveDownload.value,
    attachment.id,
  ]
  const response = await attachmentService.download(
    decision.value!.uuid,
    attachment,
  )
  if (response.error) {
    errors.value = [response.error.title]
  }
  attachmentIdsWithActiveDownload.value =
    attachmentIdsWithActiveDownload.value.filter((id) => id !== attachment.id)
}

const hasAttachments = computed<boolean>(() => {
  return decision.value!.otherAttachments.length > 0
})

const attachments = computed({
  get: () => decision.value!.otherAttachments,
  set: (newValues) => {
    decision.value!.otherAttachments = newValues
  },
})
</script>

<template>
  <TitleElement class="mb-24">Weitere Anhänge</TitleElement>
  <Message
    v-if="errors.length > 0 && !isUploading"
    class="mt-8"
    severity="error"
  >
    <p class="ris-body1-bold">Es sind Fehler aufgetreten</p>
    <ul class="m-0 list-disc ps-20">
      <li v-for="(error, index) in errors" :key="index">{{ error }}</li>
    </ul>
  </Message>
  <FileUpload
    id="other-attachments-upload"
    aria-label="Weitere Dateien hochladen"
    :is-loading="isUploading"
    @files-selected="(files) => upload(files)"
  />
  <div class="mt-8 mb-24 flex flex-row justify-end">
    <div class="ris-label2-regular text-gray-900">
      Maximale Dateigröße: 100 MB
    </div>
  </div>
  <div class="max-h-[40vh] overflow-y-auto">
    <AttachmentList
      v-if="hasAttachments"
      :attachment-ids-with-active-download="attachmentIdsWithActiveDownload"
      :enable-select="false"
      :files="attachments"
      @delete="handleOnDelete"
      @download="handleOnDownload"
    />
  </div>
</template>
