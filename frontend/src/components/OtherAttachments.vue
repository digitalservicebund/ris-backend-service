<script lang="ts" setup>
import Message from "primevue/message"
import AttachmentList from "@/components/AttachmentList.vue"
import FileUpload from "@/components/FileUpload.vue"
import TitleElement from "@/components/TitleElement.vue"
import { useAttachments } from "@/composables/useAttachments"
import attachmentService from "@/services/attachmentService"

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
  {},
  {
    getList: (decision) => decision.otherAttachments,
    setList: (decision, newValues) => (decision.otherAttachments = newValues),
    uploadFn: (uuid, file) =>
      attachmentService.uploadOtherAttachment(uuid, file),
  },
)
</script>

<template>
  <TitleElement class="mb-24">Weitere Anhänge</TitleElement>
  <Message v-if="errors.length > 0 && !isLoading" class="mt-8" severity="error">
    <p class="ris-body1-bold">Es sind Fehler aufgetreten</p>
    <ul class="m-0 list-disc ps-20">
      <li v-for="(error, index) in errors" :key="index">{{ error }}</li>
    </ul>
  </Message>
  <FileUpload
    id="other-attachments-upload"
    aria-label="Weitere Dateien hochladen"
    :is-loading="isLoading"
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
