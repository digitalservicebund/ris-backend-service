<script lang="ts" setup>
import { ref } from "vue"
import { uploadDocUnit } from "../../api"
import { useDocUnitsStore } from "../../store"
import RisButton from "../ris-button/RisButton.vue"

const docUnitsStore = useDocUnitsStore()
const inDrag = ref(false)

const upload = async (file: File) => {
  let docUnit = await uploadDocUnit(file)
  console.log("file uploaded, response:", docUnit)
  docUnitsStore.add(docUnit)
}

const dragover = (e: Event) => {
  e.preventDefault()
  inDrag.value = true
}

const dragleave = () => {
  inDrag.value = false
}

const drop = (e: DragEvent) => {
  e.preventDefault()
  inDrag.value = false
  if (e.dataTransfer) {
    upload(e.dataTransfer.files[0])
  }
}
</script>

<template>
  Upload
  <div
    class="upload-drop-area"
    :class="{ 'upload-drop-area__in-drag': inDrag }"
    @dragover="dragover"
    @dragleave="dragleave"
    @drop="drop"
  >
    Datei in diesen Bereich ziehen
    <br />
    oder
    <br />
    <ris-button color="blue800" label="Festplatte durchsuchen" />
  </div>
</template>

<style lang="scss">
.upload-drop-area {
  border-radius: $border-radius;
  border: $border-style-inactive;
  background: $white;
  max-width: 640px;
  margin: 20px; // define globally
  padding: 20px; // define globally

  &:hover {
    border: $border-style-active;
  }

  &__in-drag {
    border: $border-style-active;
  }
}
</style>
