<script lang="ts" setup>
import { storeToRefs } from "pinia"
import Message from "primevue/message"
import { computed, Ref, ref } from "vue"
import AttachmentList from "@/components/AttachmentList.vue"
import FileUpload from "@/components/FileUpload.vue"
import FlexItem from "@/components/FlexItem.vue"
import TitleElement from "@/components/TitleElement.vue"
import { Attachment } from "@/domain/attachment"
import { Decision } from "@/domain/decision"
import attachmentService from "@/services/attachmentService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const emit = defineEmits<{
  attachmentsUploaded: [boolean]
  attachmentIndexSelected: [number]
  attachmentIndexDeleted: [number]
}>()

const store = useDocumentUnitStore()
const { documentUnit: decision } = storeToRefs(store) as {
  documentUnit: Ref<Decision | undefined>
}
const errors = ref<string[]>([])
const isLoading = ref(false)
const acceptedFileFormats = [".docx"]

const errorTitle = computed(() => {
  if (errors.value.length === 1) {
    return "1 Datei konnte nicht hochgeladen werden."
  } else if (errors.value.length > 1) {
    return `${errors.value.length} Dateien konnten nicht hochgeladen werden.`
  } else return ""
})

const handleOnDelete = async (fileToDelete: Attachment) => {
  errors.value = []
  if (fileToDelete?.s3path == undefined) {
    console.error("file path is undefined", fileToDelete)
    return
  }

  if (
    (await attachmentService.delete(decision.value!.uuid, fileToDelete.s3path))
      .status < 300
  ) {
    emit("attachmentIndexDeleted", attachments.value.indexOf(fileToDelete))
    await store.loadDocumentUnit(store.documentUnit!.documentNumber!)
  }
}

const handleOnSelect = (index: number) => {
  if (index >= 0 && index < attachments.value.length) {
    emit("attachmentIndexSelected", index)
  }
}

async function upload(files: FileList) {
  errors.value = []
  let anySuccessful = false
  try {
    for (const file of Array.from(files)) {
      isLoading.value = true
      const response = await attachmentService.uploadOriginalDocument(
        decision.value!.uuid,
        file,
      )
      if (response.status === 200 && response.data) {
        anySuccessful = true
      } else if (response.error?.title) {
        errors.value.push(
          file.name +
            " " +
            response.error?.title +
            " " +
            response.error?.description,
        )
      }
    }
  } finally {
    isLoading.value = false
    emit("attachmentsUploaded", anySuccessful)
    await store.loadDocumentUnit(store.documentUnit!.documentNumber!)
  }
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
  return decision.value!.attachments.length > 0
})

const attachments = computed({
  get: () => decision.value!.attachments,
  set: (newValues) => {
    decision.value!.attachments = newValues
  },
})
</script>

<template>
  <FlexItem
    v-if="decision"
    class="w-full flex-1 grow p-24"
    data-testid="document-unit-attachments"
  >
    <div class="flex w-full flex-1 grow flex-col gap-24">
      <TitleElement>Originaldokument</TitleElement>
      <AttachmentList
        v-if="hasAttachments"
        :attachment-ids-with-active-download="attachmentIdsWithActiveDownload"
        enable-select
        :files="attachments"
        @delete="handleOnDelete"
        @download="handleOnDownload"
        @select="handleOnSelect"
      />
      <Message
        v-if="errors.length > 0 && !isLoading"
        class="mt-8"
        severity="error"
      >
        <p class="ris-body1-bold">{{ errorTitle }}</p>
        <ul class="m-0 list-disc ps-20">
          <li v-for="(error, index) in errors" :key="index">{{ error }}</li>
        </ul>
      </Message>
      <div class="flex-grow">
        <div class="flex h-full flex-col items-start">
          <FileUpload
            id="file-upload"
            :accept="acceptedFileFormats.toString()"
            aria-label="Originaldokument hochladen"
            :is-loading="isLoading"
            @files-selected="(files) => upload(files)"
          />
        </div>
      </div>
      <div class="flex flex-row justify-between">
        <div class="ris-label2-regular text-gray-900">
          Zulässige Dateiformate:
          {{ acceptedFileFormats.toString().replace(/\./g, " ") }}
        </div>
        <div class="ris-label2-regular text-gray-900">
          Maximale Dateigröße: 20 MB
        </div>
      </div>
    </div>
  </FlexItem>
</template>
