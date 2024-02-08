<script lang="ts" setup>
import { onMounted, ref } from "vue"
import DocumentUnitWrapper from "@/components/DocumentUnitWrapper.vue"
import FileUpload from "@/components/FileUpload.vue"
import FileViewer from "@/components/FileViewer.vue"
import LoadingSpinner from "@/components/LoadingSpinner.vue"
import DocumentUnit from "@/domain/documentUnit"
import documentUnitService from "@/services/documentUnitService"
import fileService from "@/services/fileService"
import { ResponseError } from "@/services/httpClient"

const props = defineProps<{ documentUnit: DocumentUnit }>()
const emit = defineEmits<{
  updateDocumentUnit: [updatedDocumentUnit: DocumentUnit]
}>()

const error = ref<ResponseError>()
const html = ref<string>()
const isLoading = ref(false)

async function handleDeleteFile() {
  if ((await fileService.delete(props.documentUnit.uuid)).status < 300) {
    const updateResponse = await documentUnitService.getByDocumentNumber(
      props.documentUnit.documentNumber as string,
    )
    if (updateResponse.error) {
      console.error(updateResponse.error)
    } else {
      emit("updateDocumentUnit", updateResponse.data)
      html.value = undefined
    }
  }
}

async function upload(file: File) {
  isLoading.value = true

  try {
    const response = await fileService.upload(props.documentUnit.uuid, file)
    if (response.status === 200 && response.data) {
      html.value = response.data.html
    } else {
      error.value = response.error
    }
  } finally {
    isLoading.value = false
    emit("updateDocumentUnit", props.documentUnit)
  }
}

onMounted(async () => {
  isLoading.value = true
  try {
    const fileResponse = await fileService.getDocxFileAsHtml(
      props.documentUnit.uuid,
    )

    if (fileResponse.error) {
      console.error(JSON.stringify(fileResponse.error))
    } else {
      html.value = fileResponse.data.html
    }
  } finally {
    isLoading.value = false
  }
})
</script>

<template>
  <DocumentUnitWrapper :document-unit="documentUnit">
    <template #default="{ classes }">
      <div class="flex flex-col" :class="classes">
        <h1 class="ds-heading-02-reg mb-[1rem]">Dokumente</h1>

        <span v-if="isLoading" class="flex flex-col items-center">
          <LoadingSpinner />
        </span>

        <div v-else>
          <FileViewer
            v-if="html"
            :file-name="documentUnit.filename"
            :file-type="documentUnit.filetype"
            :html="html"
            :upload-time-stamp="documentUnit.fileuploadtimestamp"
            :uuid="documentUnit.uuid"
            @delete-file="handleDeleteFile"
          />

          <div v-else class="flex w-[40rem] flex-col items-start">
            <div class="mb-14">
              Aktuell ist keine Datei hinterlegt. Wählen Sie die Datei des
              Originaldokumentes aus
            </div>

            <FileUpload
              :error="error"
              :is-loading="false"
              @file-selected="(file) => upload(file)"
            />
          </div>
        </div>
      </div>
    </template>
  </DocumentUnitWrapper>
</template>
