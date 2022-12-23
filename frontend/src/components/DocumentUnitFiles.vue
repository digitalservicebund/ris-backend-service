<script lang="ts" setup>
import DocumentUnitWrapper from "@/components/DocumentUnitWrapper.vue"
import FileUpload from "@/components/FileUpload.vue"
import FileViewer from "@/components/FileViewer.vue"
import DocumentUnit from "@/domain/documentUnit"
import documentUnitService from "@/services/documentUnitService"
import fileService from "@/services/fileService"

const props = defineProps<{ documentUnit: DocumentUnit }>()
const emit = defineEmits<{
  (e: "updateDocumentUnit", updatedDocumentUnit: DocumentUnit): void
}>()

async function handleDeleteFile() {
  if ((await fileService.delete(props.documentUnit.uuid)).status < 300) {
    const updateResponse = await documentUnitService.getByDocumentNumber(
      props.documentUnit.documentNumber as string
    )
    if (updateResponse.error) {
      console.error(updateResponse.error)
    } else {
      emit("updateDocumentUnit", updateResponse.data)
    }
  }
}
</script>

<template>
  <DocumentUnitWrapper v-slot="{ classes }" :document-unit="documentUnit">
    <div class="flex flex-col" :class="classes">
      <h1 class="heading-02-regular mb-[1rem]">Dokumente</h1>

      <FileViewer
        v-if="documentUnit.hasFile"
        :file-name="documentUnit.filename"
        :file-type="documentUnit.filetype"
        :s3-path="(documentUnit.s3path as string)"
        :upload-time-stamp="documentUnit.fileuploadtimestamp"
        @delete-file="handleDeleteFile"
      />

      <FileUpload
        v-else
        :document-unit-uuid="documentUnit.uuid"
        @update-document-unit="emit('updateDocumentUnit', $event)"
      />
    </div>
  </DocumentUnitWrapper>
</template>
