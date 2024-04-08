<script lang="ts" setup>
import { onMounted, ref } from "vue"
import DocumentUnitWrapper from "@/components/DocumentUnitWrapper.vue"
import DocumentUnitFileList from "@/components/FileList.vue"
import FileUpload from "@/components/FileUpload.vue"
import DocumentUnit from "@/domain/documentUnit"
import FileItem from "@/domain/file"
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
const acceptedFileFormats = [".docx"]

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

const deleteFile = (file: FileItem) => {
  console.log(file)
  handleDeleteFile()
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
      <div class="flex flex-col space-y-20" :class="classes">
        <h1 class="ds-heading-02-reg mb-[1rem]">Dokumente</h1>
        <DocumentUnitFileList
          v-if="documentUnit.filename != null"
          id="file-table"
          :files="[
            {
              name: documentUnit.filename,
              format: documentUnit.filetype,
              uploadedDate: documentUnit.fileuploadtimestamp,
            },
          ]"
          @delete-event="deleteFile"
        ></DocumentUnitFileList>
        <div>
          <div class="flex flex-col items-start">
            <FileUpload
              :accept="acceptedFileFormats.toString()"
              :error="error"
              :is-loading="false"
              @file-selected="(file) => upload(file)"
            />
          </div>
        </div>
        <div>
          Zulässige Dateiformate:
          {{ acceptedFileFormats.toString().replace(/\./g, " ") }}
        </div>
      </div>
    </template>
  </DocumentUnitWrapper>
</template>
