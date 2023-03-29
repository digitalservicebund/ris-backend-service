<script lang="ts" setup>
import { ref } from "vue"
import InfoModal from "@/shared/components/InfoModal.vue"
import FileInput from "@/shared/components/input/FileInput.vue"

const props = defineProps<{
  error?: { title: string; description?: string }
  isLoading?: boolean
  accept?: string
}>()
const emits = defineEmits<{
  (e: "fileSelected", file: File): void
}>()

interface Status {
  file: File | null
  inDrag: boolean
  inDragError: string
}

const emptyStatus: Status = {
  file: null,
  inDrag: false,
  inDragError: "",
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
    emits("fileSelected", e.dataTransfer.files[0])
  }
}

function onFileSelect(event: Event) {
  const files = (event.target as HTMLInputElement).files

  if (files) {
    reset()
    emits("fileSelected", files[0])
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
    <span v-if="isLoading">
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
        :accept="accept"
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
