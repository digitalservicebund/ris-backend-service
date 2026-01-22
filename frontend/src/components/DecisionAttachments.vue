<script lang="ts" setup>
import { storeToRefs } from "pinia"
import Message from "primevue/message"
import { computed, Ref } from "vue"
import AttachmentList from "@/components/AttachmentList.vue"
import FileUpload from "@/components/FileUpload.vue"
import FlexItem from "@/components/FlexItem.vue"
import TitleElement from "@/components/TitleElement.vue"
import { useAttachments } from "@/composables/useAttachments"
import { Decision } from "@/domain/decision"
import attachmentService from "@/services/attachmentService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const emit = defineEmits<{
  attachmentsUploaded: [boolean]
  attachmentIndexSelected: [number]
  attachmentIndexDeleted: [number]
}>()

const {
  errors,
  isLoading,
  attachments,
  hasAttachments,
  attachmentIdsWithActiveDownload,
  handleOnDelete,
  handleOnDownload,
  upload,
} = useAttachments(
  {
    attachmentsUploaded: (success) => emit("attachmentsUploaded", success),
    attachmentIndexDeleted: (index) => emit("attachmentIndexDeleted", index),
  },
  {
    getList: (decision) => decision.attachments,
    setList: (decision, newValues) => (decision.attachments = newValues),
    uploadFn: (uuid, file) =>
      attachmentService.uploadOriginalDocument(uuid, file),
  },
)

const store = useDocumentUnitStore()
const { documentUnit: decision } = storeToRefs(store) as {
  documentUnit: Ref<Decision | undefined>
}
const acceptedFileFormats = [".docx"]

const errorTitle = computed(() => {
  if (errors.value.length === 1) {
    return "1 Datei konnte nicht hochgeladen werden."
  } else if (errors.value.length > 1) {
    return `${errors.value.length} Dateien konnten nicht hochgeladen werden.`
  } else return ""
})

const handleOnSelect = (index: number) => {
  if (index >= 0 && index < attachments.value.length) {
    emit("attachmentIndexSelected", index)
  }
}
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
