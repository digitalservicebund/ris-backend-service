<script lang="ts" setup>
import { ref } from "vue"
import { uploadFile } from "../../api"
import { useDocUnitsStore } from "../../store"
import RisButton from "../ris-button/RisButton.vue"

const docUnitsStore = useDocUnitsStore()

interface Status {
  file: File | null
  inDrag: boolean
  inDragError: string
  uploadStatus: "none" | "uploading" | "succeeded" | "failed"
  docUnitId: number | null
}

const emptyStatus: Status = {
  file: null,
  inDrag: false,
  inDragError: "",
  uploadStatus: "none",
  docUnitId: null,
}

const status = ref<Status>(emptyStatus)

const reset = () => {
  status.value = emptyStatus
}

const upload = async (file: File) => {
  let extension = file.name.split(".").pop()
  if (!extension || extension.toLowerCase() !== "docx") {
    alert("Aktuell werden nur DOCX Dateien unterstützt")
    return
  }
  status.value.file = file
  status.value.uploadStatus = "uploading"
  let docUnit = await uploadFile(file)
  docUnitsStore.add(docUnit)
  status.value.docUnitId = docUnit.id
  status.value.uploadStatus = "succeeded" // error handling TODO
  console.log("file uploaded, response:", docUnit)
}

const dragover = (e: DragEvent) => {
  e.preventDefault()
  status.value.inDragError = checkForInDragError(e)
  status.value.inDrag = true
}

const checkForInDragError = (e: DragEvent): string => {
  //  this doesn't work on Windows, the type is not included TODO
  if (!e.dataTransfer) return ""
  let items = e.dataTransfer.items
  if (items.length > 1) return "Nur eine Datei auf einmal ist möglich"
  if (items[0].kind !== "file") return "Dies scheint keine Datei zu sein"
  if (
    items[0].type &&
    items[0].type !==
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
  )
    return "Aktuell werden nur DOCX Dateien unterstützt"
  return ""
}

const dragleave = () => {
  status.value.inDragError = ""
  status.value.inDrag = false
}

const drop = (e: DragEvent) => {
  e.preventDefault()
  reset()
  if (e.dataTransfer) {
    upload(e.dataTransfer.files[0])
  }
}

const openFileDialog = () => {
  const inputEl = document.createElement("input")
  inputEl.setAttribute("type", "file")
  inputEl.addEventListener("change", (e: Event) => {
    let files = (e.target as HTMLInputElement).files
    if (!files) return
    reset()
    upload(files[0])
  })
  inputEl.click()
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
            'upload-drop-area__in-drag': status.inDrag,
            'upload-drop-area__in-drag-error': status.inDragError,
          }"
          @dragover="dragover"
          @dragleave="dragleave"
          @drop="drop"
        >
          <span v-if="!status.inDragError">
            <v-row align="center">
              <v-col cols="1" />
              <v-col cols="2">
                <v-icon class="icon_upload" size="50px">
                  drive_folder_upload
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
                <ris-button
                  color="blue800"
                  label="Festplatte durchsuchen"
                  @click="openFileDialog"
                />
              </v-col>
            </v-row>
          </span>
          <span v-else>
            {{ status.inDragError }}
          </span>
        </v-container>
      </v-col>
      <v-col cols="4"></v-col>
    </v-row>
    <v-row>
      <v-col cols="10">
        <span v-if="status.uploadStatus === 'uploading'">
          Die Datei {{ status.file ? status.file.name : "" }} wird
          hochgeladen...
        </span>
        <span v-if="status.uploadStatus === 'succeeded'">
          Die Datei {{ status.file ? status.file.name : "" }} wurde erfolgreich
          hochgeladen,
          <router-link
            :to="{ name: 'Stammdaten', params: { id: status.docUnitId } }"
          >
            zur Stammdaten-Ansicht</router-link
          >
        </span>
      </v-col>
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
.icon_upload {
  color: $blue800;
}
</style>
