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
  <DocumentUnitWrapper :document-unit="documentUnit">
    <v-container>
      <v-row>
        <v-col><h2>Dokumente</h2></v-col>
      </v-row>
    </v-container>
    <FileViewer
      v-if="documentUnit.hasFile"
      :s3-path="(documentUnit.s3path as string)"
      :file-name="documentUnit.filename"
      :upload-time-stamp="documentUnit.fileuploadtimestamp"
      :file-type="documentUnit.filetype"
      @delete-file="emits('deleteFile')"
    />
    <FileUpload
      v-else
      :document-unit-uuid="documentUnit.uuid"
      @update-document-unit="emits('updateDocumentUnit', $event)"
    />
  </DocumentUnitWrapper>
</template>
