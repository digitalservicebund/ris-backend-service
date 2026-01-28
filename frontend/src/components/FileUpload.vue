<script lang="ts" setup>
import { ref } from "vue"
import FileInput from "@/components/input/FileInput.vue"
import LoadingSpinner from "@/components/LoadingSpinner.vue"
import IconUpload from "~icons/ic/baseline-upload-file"

defineProps<{
  isLoading?: boolean
  accept?: string
  ariaLabel: string
  id: string
}>()

const emits = defineEmits<{
  filesSelected: [files: FileList]
}>()

interface Status {
  file: File | null
  inDrag: boolean
}

const emptyStatus: Status = {
  file: null,
  inDrag: false,
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
  status.value.inDrag = false
  if (e.dataTransfer) {
    emits("filesSelected", e.dataTransfer.files)
  }
}

function onFileSelect(event: Event) {
  const files = (event.target as HTMLInputElement).files

  if (files) {
    reset()
    emits("filesSelected", files)
  }
}
</script>

<template>
  <!-- eslint-disable-next-line vuejs-accessibility/no-static-element-interactions -->
  <div
    id="upload-drop-area"
    class="upload-drop-area flex w-full flex-grow flex-col items-center justify-center border-1 border-dashed border-blue-300 bg-white text-center"
    :class="{
      'bg-blue-200': status.inDrag,
    }"
    @dragleave="dragleave"
    @dragover="dragover"
    @drop="drop"
  >
    <span v-if="isLoading" class="flex flex-col items-center">
      <LoadingSpinner />
      <div class="ris-heading3-regular mt-[0.5rem]">Upload läuft</div>
      <div>{{ status.file ? status.file.name : "" }}</div>
    </span>
    <span v-else class="flex flex-col items-center p-24">
      <span class="rounded-full bg-blue-200 p-12 text-blue-800">
        <IconUpload class="text-24 text-blue-800" height="42" width="42" />
      </span>

      <div class="ris-label1-bold mt-[0.5rem] pt-6">
        Ziehen Sie Ihre Dateien in diesen Bereich.
      </div>
      <FileInput
        :id="id"
        :accept="accept"
        :aria-label="ariaLabel"
        @input="onFileSelect"
      >
        <span class="ris-link3-bold mt-[0.438rem] hover:underline"
          >Oder hier auswählen</span
        >
      </FileInput>
    </span>
  </div>
</template>
