<script lang="ts" setup>
import { ref } from "vue"
import FileInput from "@/components/FileInput.vue"
import InfoModal from "@/components/InfoModal.vue"
import { ResponseError } from "@/services/httpClient"

const props = defineProps<{ error?: ResponseError }>()
const emits = defineEmits<{
  (e: "file-selected", file: File): void
}>()

enum UploadStatus {
  UNKNOWN,
  UPLOADING,
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

function reset() {
  status.value = emptyStatus
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
    emits("file-selected", e.dataTransfer.files[0])
  }
}

function onFileSelect(event: Event) {
  const files = (event.target as HTMLInputElement).files

  if (files) {
    reset()
    emits("file-selected", files[0])
  }
}
</script>

<template>
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

  <InfoModal v-if="props.error" v-bind="props.error" class="mt-8" />
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
