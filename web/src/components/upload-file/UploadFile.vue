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
    :class="{ 'in-drag': inDrag }"
    @dragover="dragover"
    @dragleave="dragleave"
    @drop="drop"
  >
    Datei in diesen Bereich ziehen
    <br />
    oder
    <br />
    <ris-button label="Festplatte durchsuchen" />
  </div>
</template>

<style lang="scss">
.upload-drop-area {
  border-radius: 10px;
  border: 3px solid $blue300;
  background: white;
  width: 640px;
  height: 313px;
  margin: 20px;
  padding: 20px;

  &:hover {
    border: 3px solid $blue500 !important;
  }
}
.in-drag {
  border: 3px solid $blue500 !important;
}
</style>
