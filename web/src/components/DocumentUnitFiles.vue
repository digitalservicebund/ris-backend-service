<script lang="ts" setup>
import DocumentUnitWrapper from "@/components/DocumentUnitWrapper.vue"
import FileUpload from "@/components/FileUpload.vue"
import FileViewer from "@/components/FileViewer.vue"
import DocumentUnit from "@/domain/documentUnit"

defineProps<{ documentUnit: DocumentUnit }>()

const emits = defineEmits<{
  (e: "updateDocumentUnit", updatedDocumentUnit: DocumentUnit): void
  (e: "deleteFile"): void
}>()
</script>

<template>
  <DocumentUnitWrapper v-slot="{ classes }" :document-unit="documentUnit">
    <div class="flex flex-col" :class="classes">
      <h1 class="heading-03-bold mb-8">Dokumente</h1>

      <FileViewer
        v-if="documentUnit.hasFile"
        :file-name="documentUnit.filename"
        :file-type="documentUnit.filetype"
        :s3-path="(documentUnit.s3path as string)"
        :upload-time-stamp="documentUnit.fileuploadtimestamp"
        @delete-file="emits('deleteFile')"
      />

      <FileUpload
        v-else
        :document-unit-uuid="documentUnit.uuid"
        @update-document-unit="emits('updateDocumentUnit', $event)"
      />
    </div>
  </DocumentUnitWrapper>
</template>
