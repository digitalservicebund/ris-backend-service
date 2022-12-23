<script lang="ts" setup>
import { ref } from "vue"
import FileInput from "@/components/FileInput.vue"
import InfoModal from "@/components/InfoModal.vue"
import DocumentUnit from "@/domain/documentUnit"
import fileService from "@/services/fileService"
import { ResponseError } from "@/services/httpClient"

const props = defineProps<{ documentUnitUuid: string }>()
const emits = defineEmits<{
  (e: "updateDocumentUnit", updatedDocumentUnit: DocumentUnit): void
}>()

enum UploadStatus {
  UNKNOWN,
  UPLOADING,
  SUCCESSED,
  FAILED,
  FILE_TOO_LARGE,
  WRONG_FILE_FORMAT,
}

interface Status {
  file: File | null
  inDrag: boolean
  inDragError: string
  uploadStatus: UploadStatus
}

const emptyStatus: Status = {
  file: null,
  inDrag: false,
  inDragError: "",
  uploadStatus: UploadStatus.UNKNOWN,
}

const status = ref(emptyStatus)
const error = ref<ResponseError>()

function reset() {
  status.value = emptyStatus
}

async function upload(file: File) {
  status.value.file = file
  status.value.uploadStatus = UploadStatus.UPLOADING
  const response = await fileService.upload(props.documentUnitUuid, file)
  if (response.status === 201 && response.data) {
    status.value.uploadStatus = UploadStatus.SUCCESSED
    emits("updateDocumentUnit", response.data)
  } else {
    status.value.uploadStatus =
      response.status === 413
        ? UploadStatus.FILE_TOO_LARGE
        : response.status === 415
        ? UploadStatus.WRONG_FILE_FORMAT
        : UploadStatus.FAILED
  }
  error.value = response.error
}

function dragover(e: DragEvent) {
  e.preventDefault()
  status.value.inDrag = true
}

function dragleave() {
  status.value.inDrag = false
}

function drop(e: DragEvent) {
  e.preventDefault()
  reset()
  if (e.dataTransfer) {
    upload(e.dataTransfer.files[0])
  }
}

function onFileSelect(event: Event) {
  const files = (event.target as HTMLInputElement).files

  if (files) {
    reset()
    upload(files[0])
  }
}
</script>

<template>
  <div class="flex flex-col items-start w-[40rem]">
    <div class="mb-14">
      Aktuell ist keine Datei hinterlegt. Wählen Sie die Datei des
      Originaldokumentes aus
    </div>

    <div
      id="upload-drop-area"
      class="bg-white border-3 border-blue-300 border-dashed flex flex-col hover:border-3 items-center p-[3.125rem] rounded-lg text-center upload-drop-area w-full"
      :class="{
        'upload-drop-area__in-drag': status.inDrag,
      }"
      @dragleave="dragleave"
      @dragover="dragover"
      @drop="drop"
    >
      <span v-if="status.uploadStatus === UploadStatus.UPLOADING">
        <span class="material-icons text-72 text-blue-800"> refresh </span>
        <div class="heading-03-regular mt-[0.5rem]">Upload läuft</div>
        <div>{{ status.file ? status.file.name : "" }}</div>
      </span>
      <span v-else>
        <span class="material-icons text-72 text-blue-800"> upload_file </span>

        <div class="heading-03-regular mt-[0.5rem]">
          Datei in diesen Bereich ziehen
        </div>
        <FileInput
          id="file-upload"
          aria-label="Upload File"
          @input="onFileSelect"
        >
          <span class="hover:underline link-03-bold mt-[0.438rem]"
            >oder Datei auswählen</span
          >
        </FileInput>
      </span>
    </div>

    <InfoModal v-if="error" v-bind="error" class="mt-8" />
  </div>
</template>

<style lang="scss" scoped>
.upload-drop-area {
  &__in-drag-error {
    @apply border-3 border-dashed border-red-200;

    &:hover {
      @apply border-3 border-dashed border-red-800;
    }
  }

  &__in-drag {
    @apply border-3 border-dashed border-blue-500;
  }
}
</style>
