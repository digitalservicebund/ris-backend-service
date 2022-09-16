<script lang="ts" setup>
import { ref } from "vue"
import ErrorModal from "@/components/ErrorModal.vue"
import FileInputButton from "@/components/FileInputButton.vue"
import DocumentUnit from "@/domain/documentUnit"
import { UploadStatus, UploadErrorStatus } from "@/domain/uploadStatus"
import fileService from "@/services/fileService"

const props = defineProps<{ documentUnitUuid: string }>()
const emits = defineEmits<{
  (e: "updateDocumentUnit", updatedDocumentUnit: DocumentUnit): void
}>()

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

const reset = () => {
  status.value = emptyStatus
}

const upload = async (file: File) => {
  const extension = file.name?.split(".").pop()
  if (!extension || extension.toLowerCase() !== "docx") {
    status.value.uploadStatus = UploadStatus.WRONG_FILE_FORMAT
    return
  }
  status.value.file = file
  status.value.uploadStatus = UploadStatus.UPLOADING
  const response = await fileService.uploadFile(props.documentUnitUuid, file)
  status.value.uploadStatus = response.status
  if (response.status == UploadStatus.SUCCESSED && !!response.documentUnit) {
    emits("updateDocumentUnit", response.documentUnit)
  }
}

const dragover = (e: DragEvent) => {
  e.preventDefault()
  status.value.inDragError = checkForInDragError(e)
  status.value.inDrag = true
}

const checkForInDragError = (e: DragEvent): string => {
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

function onFileSelect(event: Event): void {
  const files = (event.target as HTMLInputElement).files

  if (files) {
    reset()
    upload(files[0])
  }
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
            'upload-drop-area__in-drag-error':
              status.inDragError ||
              UploadErrorStatus.includes(status.uploadStatus),
          }"
          @dragover="dragover"
          @dragleave="dragleave"
          @drop="drop"
        >
          <span v-if="status.inDragError">
            <span class="file-upload material-icons"> upload_file </span>
            <!-- if still in drag move -->
            <span v-if="status.uploadStatus !== UploadStatus.WRONG_FILE_FORMAT">
              <div class="upload-status">Datei wird nicht unterstützt.</div>
              <div>
                Versuchen Sie eine .docx-Version dieser Datei hochzuladen.
              </div>
            </span>
            <!-- if file dropped and failed to upload -->
            <span v-else>
              <div class="upload-status">Datei in diesen Bereich ziehen</div>
              <div>oder</div>
              <div>
                <FileInputButton
                  id="file-upload-after-fail"
                  aria-label="Upload File"
                  @input="onFileSelect"
                >
                  <span class="material-icons">search</span>
                  Festplatte durchsuchen
                </FileInputButton>
              </div>
            </span>
          </span>
          <span v-else>
            <span v-if="status.uploadStatus === UploadStatus.UPLOADING">
              <span class="file-upload material-icons"> refresh </span>
              <div class="upload-status">Upload läuft</div>
              <div>{{ status.file ? status.file.name : "" }}</div>
            </span>
            <span v-else-if="status.uploadStatus === UploadStatus.SUCCESSED">
              <span class="file-upload material-icons"> upload_file </span>
              <div class="upload-status">
                Die Datei {{ status.file ? status.file.name : "" }} wurde
                erfolgreich hochgeladen
              </div>
            </span>
            <span v-else>
              <span class="file-upload material-icons"> upload_file </span>
              <div class="upload-status">Datei in diesen Bereich ziehen</div>
              <div>oder</div>
              <FileInputButton
                id="file-upload"
                aria-label="Upload File"
                @input="onFileSelect"
              >
                <span class="material-icons">search</span>
                Festplatte durchsuchen
              </FileInputButton>
            </span>
          </span>
        </v-container>
      </v-col>
    </v-row>
    <v-row v-if="status.uploadStatus === UploadStatus.WRONG_FILE_FORMAT">
      <v-col md="8" sm="12">
        <ErrorModal
          title="Das ausgewählte Dateiformat ist nicht korrekt."
          description="Versuchen Sie eine .docx-Version dieser Datei hochzuladen."
        >
        </ErrorModal>
      </v-col>
    </v-row>
    <v-row v-if="status.uploadStatus === UploadStatus.FILE_TOO_LARGE">
      <v-col md="8" sm="12">
        <ErrorModal
          title="Die Datei darf max. 20 MB groß sein."
          description="Bitte laden Sie eine kleinere Datei hoch."
        >
        </ErrorModal>
      </v-col>
    </v-row>
    <v-row v-if="status.uploadStatus === UploadStatus.FAILED">
      <v-col md="8" sm="12">
        <ErrorModal
          title="Leider ist ein Fehler aufgetreten."
          description="Bitte versuchen Sie es zu einem späteren Zeitpunkt erneut."
        >
        </ErrorModal>
      </v-col>
    </v-row>
  </v-container>
</template>

<style lang="scss" scoped>
@import "@/styles/variables";

.upload-drop-area {
  border-radius: $border-radius;
  border: $border-style-inactive;
  background: $white;
  padding: 44px;

  &:hover {
    border: $border-style-active;
  }

  &__in-drag-error {
    border: $border-style-error;

    &:hover {
      border: $border-style-error;
    }
  }

  &__in-drag {
    border: $border-style-active;
  }
}

.file-upload {
  margin-top: 10px;
  margin-left: -5px;
  color: $blue800;
  font-size: 50px;
}

.upload-status {
  font-size: 24px;
  margin: 16px 0 10px;
}
</style>
