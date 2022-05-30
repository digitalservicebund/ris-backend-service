<script lang="ts" setup>
import { ref } from "vue"
import { uploadDocUnit } from "../../api"
import { useDocUnitsStore } from "../../store"
import RisButton from "../ris-button/RisButton.vue"

const docUnitsStore = useDocUnitsStore()
const inDrag = ref(false)
const inDragError = ref("")

const upload = async (file: File) => {
  let docUnit = await uploadDocUnit(file)
  console.log("file uploaded, response:", docUnit)
  docUnitsStore.add(docUnit)
}

const dragover = (e: DragEvent) => {
  e.preventDefault()
  inDragError.value = checkForInDragError(e)
  inDrag.value = true
}

const checkForInDragError = (e: DragEvent): string => {
  if (!e.dataTransfer) return ""
  let items = e.dataTransfer.items
  if (items.length !== 1) return "Nur eine Datei auf einmal ist möglich"
  if (items[0].kind !== "file") return "Dies scheint keine Datei zu sein"
  if (
    items[0].type !==
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
  )
    return "Aktuell werden nur DOCX Dateien unterstützt"
  return ""
}

const dragleave = () => {
  resetDrag()
}

const resetDrag = () => {
  inDrag.value = false
  inDragError.value = ""
}

const drop = (e: DragEvent) => {
  e.preventDefault()
  resetDrag()
  if (e.dataTransfer) {
    upload(e.dataTransfer.files[0])
  }
}
</script>

<template>
  <v-container>
    <v-row>
      <v-col cols="10"><h3>Original Dokument</h3></v-col>
    </v-row>
    <v-row>
      <v-col cols="10">
        Aktuell ist keine Datei hinterlegt. Wählen Sie die Datei des
        Originaldokumentes aus
      </v-col>
    </v-row>
    <v-row><v-col></v-col></v-row>
    <v-row>
      <v-col cols="10"><h3>Upload</h3></v-col>
    </v-row>
    <v-row>
      <v-col cols="6">
        <v-container
          class="upload-drop-area"
          :class="{
            'upload-drop-area__in-drag': inDrag,
            'upload-drop-area__in-drag-error': inDragError,
          }"
          @dragover="dragover"
          @dragleave="dragleave"
          @drop="drop"
        >
          <span v-if="!inDragError">
            <v-row align="center">
              <v-col cols="1" />
              <v-col cols="2">
                <v-icon class="icon" size="50px">
                  mdi-folder-upload-outline
                </v-icon>
              </v-col>
              <v-col cols="7"> Datei in diesen Bereich ziehen </v-col>
            </v-row>
            <v-row>
              <v-col cols="1" />
              <v-col cols="9"> oder </v-col>
            </v-row>
            <v-row>
              <v-col cols="1" />
              <v-col cols="9">
                <ris-button color="blue800" label="Festplatte durchsuchen" />
              </v-col>
            </v-row>
          </span>
          <span v-else>
            {{ inDragError }}
          </span>
        </v-container>
      </v-col>
      <v-col cols="4"></v-col>
    </v-row>
  </v-container>
</template>

<style lang="scss">
.upload-drop-area {
  border-radius: $border-radius;
  border: $border-style-inactive;
  background: $white;

  &:hover {
    border: $border-style-active;
  }

  &__in-drag {
    border: $border-style-active;
  }

  &__in-drag-error {
    color: red;
    height: 184px;
    text-align: center;
  }
}
.icon {
  color: $blue800;
}
</style>
