<script lang="ts" setup>
import { ref } from "vue"
import TextButton from "./TextButton.vue"
import DocUnit from "@/domain/docUnit"
import fileService from "@/services/fileService"

const props = defineProps<{ docUnitUuid: string }>()
const emits = defineEmits<{
  (e: "updateDocUnit", updatedDocUnit: DocUnit): void
}>()

interface Status {
  file: File | null
  inDrag: boolean
  inDragError: string
  uploadStatus: "none" | "uploading" | "succeeded" | "failed"
}

const emptyStatus: Status = {
  file: null,
  inDrag: false,
  inDragError: "",
  uploadStatus: "none",
}

const status = ref<Status>(emptyStatus)

const reset = () => {
  status.value = emptyStatus
}

const upload = async (file: File) => {
  const extension = file.name.split(".").pop()
  if (!extension || extension.toLowerCase() !== "docx") {
    status.value.uploadStatus = "failed"
    return
  }
  status.value.file = file
  status.value.uploadStatus = "uploading"
  const docUnit = await fileService.uploadFile(props.docUnitUuid, file)
  status.value.uploadStatus = "succeeded" // error handling TODO
  emits("updateDocUnit", docUnit)
}

const dragover = (e: DragEvent) => {
  e.preventDefault()
  status.value.inDragError = checkForInDragError(e)
  status.value.inDrag = true
}

const checkForInDragError = (e: DragEvent): string => {
  //  this doesn't work on Windows, the type is not included TODO
  if (!e.dataTransfer) return ""
  const items = e.dataTransfer.items
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
    const files = (e.target as HTMLInputElement).files
    if (!files) return
    reset()
    upload(files[0])
  })
  // inputEl.click()
}
</script>

<template>
  <v-container>
    <v-row>
      <v-col md="8" sm="12">
        Aktuell ist keine Datei hinterlegt. Wählen Sie die Datei des
        Originaldokumentes aus
      </v-col>
    </v-row>
    <v-row>
      <v-col md="8" sm="12">
        <v-container
          id="upload-drop-area"
          class="upload-drop-area"
          :class="{
            'upload-drop-area__in-drag': status.inDrag,
            'upload-drop-area__in-drag-error': status.inDragError,
          }"
          @dragover="dragover"
          @dragleave="dragleave"
          @drop="drop"
        >
          <span v-if="status.inDragError">
            <v-icon class="icon_upload" size="50px"> upload_file </v-icon>
            <span v-if="status.uploadStatus !== 'failed'">
              <div class="upload_status">Datei wird nicht unterstützt.</div>
              <div>
                Versuchen Sie eine .docx-Version dieser Datei hochzuladen.
              </div>
            </span>
            <span v-else>
              <div class="upload_status">Datei in diesen Bereich ziehen</div>
              <div>oder</div>
              <div>
                <TextButton
                  class="button_upload"
                  icon="search"
                  label="Festplatte durchsuchen"
                  @click="openFileDialog"
                />
              </div>
            </span>
          </span>
          <span v-else>
            <v-icon class="icon_upload" size="50px"> upload_file </v-icon>
            <span v-if="status.uploadStatus === 'uploading'">
              <div class="upload_status">
                Die Datei {{ status.file ? status.file.name : "" }} wird
                hochgeladen ...
              </div>
              <div>
                <TextButton
                  class="button_upload"
                  icon="refresh"
                  label="Upload läuft"
                  @click="openFileDialog"
                />
              </div>
            </span>
            <span v-else-if="status.uploadStatus === 'succeeded'">
              <div class="upload_status">
                Die Datei {{ status.file ? status.file.name : "" }} wurde
                erfolgreich hochgeladen
              </div>
            </span>
            <span v-else>
              <div class="upload_status">Datei in diesen Bereich ziehen</div>
              <div>oder</div>
              <div>
                <TextButton
                  class="button_upload"
                  icon="search"
                  label="Festplatte durchsuchen"
                  @click="openFileDialog"
                />
              </div>
            </span>
          </span>
        </v-container>
      </v-col>
    </v-row>
    <v-row v-if="status.uploadStatus === 'failed'">
      <v-col md="8" sm="12">
        <v-container class="upload_error">
          <v-row>
            <v-col class="upload_error_icon">
              <v-icon color="#B0243F" size="20px"> error outline </v-icon>
            </v-col>
            <v-col align-self="stretch">
              <div class="upload_error_title">
                Das ausgewählte Dateiformat ist nicht korrekt.
              </div>
              <div>
                Versuchen Sie eine .docx-Version dieser Datei hochzuladen.
              </div>
            </v-col>
          </v-row>
        </v-container>
      </v-col>
    </v-row>
  </v-container>
</template>

<style lang="scss">
.upload-drop-area {
  border-radius: $border-radius;
  border: $border-style-inactive;
  background: $white;
  padding: 44px;

  &:hover {
    border: $border-style-active;
  }

  &__in-drag-error {
    &:hover {
      border: $border-style-error;
    }
  }

  &__in-drag-error {
    border: $border-style-error;
  }

  &__in-drag {
    border: $border-style-active;
  }
}

.icon_upload {
  margin-top: 10px;
  margin-left: -5px;
  color: $blue800;
}

.upload_status {
  font-size: 24px;
  margin: 24px 0px 16px 0;
}

.button_upload {
  margin-top: 16px;
  margin-bottom: 10px;
}

.upload_error {
  background-color: #f9e5ec;
  border-left: 8px solid #b0243f;
  margin-top: 23px;
}

.upload_error_title {
  font-size: 16px;
  font-weight: 700;
}

.upload_error_icon {
  max-width: 20px;
  margin-right: 10px;
}
</style>
