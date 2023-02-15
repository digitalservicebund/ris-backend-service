<script lang="ts" setup>
import { ref } from "vue"
import DocumentUnitWrapper from "@/components/DocumentUnitWrapper.vue"
import FileUpload from "@/components/FileUpload.vue"
import FileViewer from "@/components/FileViewer.vue"
import DocumentUnit from "@/domain/documentUnit"
import documentUnitService from "@/services/documentUnitService"
import fileService from "@/services/fileService"
import { ResponseError } from "@/services/httpClient"

const props = defineProps<{ documentUnit: DocumentUnit }>()
const emit = defineEmits<{
  (e: "updateDocumentUnit", updatedDocumentUnit: DocumentUnit): void
}>()
const error = ref<ResponseError>()
const isUploading = ref(false)

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

async function upload(file: File) {
  isUploading.value = true

  try {
    const response = await fileService.upload(props.documentUnit.uuid, file)
    if (response.status === 201 && response.data) {
      emit("updateDocumentUnit", response.data)
    } else {
      error.value = response.error
    }
  } finally {
    isUploading.value = false
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

      <div v-else class="flex flex-col items-start w-[40rem]">
        <div class="mb-14">
          Aktuell ist keine Datei hinterlegt. Wählen Sie die Datei des
          Originaldokumentes aus
        </div>

        <FileUpload
          :error="error"
          :is-loading="isUploading"
          @file-selected="(file) => upload(file)"
        />
      </div>
    </div>
  </DocumentUnitWrapper>
</template>
