<script lang="ts" setup>
import { ref } from "vue"
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
  status.value.uploadStatus = "succeeded"
  emits("updateDocUnit", docUnit)
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

const onFileSelect = (e: Event) => {
  const files = (e.target as HTMLInputElement).files
  if (!files) return
  reset()
  upload(files[0])
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
            <!-- if still in drag move -->
            <span v-if="status.uploadStatus !== 'failed'">
              <div class="upload_status">Datei wird nicht unterstützt.</div>
              <div>
                Versuchen Sie eine .docx-Version dieser Datei hochzuladen.
              </div>
            </span>
            <!-- if file dropped and failed to upload -->
            <span v-else>
              <div class="upload_status">Datei in diesen Bereich ziehen</div>
              <div>oder</div>
              <div>
                <v-btn
                  class="ris-btn"
                  :rounded="0"
                  :ripple="false"
                  :flat="true"
                  color="blue800"
                >
                  <v-icon> search </v-icon>
                  <label class="custom-file-label" for="file-upload-after-fail">
                    Festplatte durchsuchen
                    <input
                      id="file-upload-after-fail"
                      class="custom-file-input"
                      type="file"
                      name="file-upload-after-fail"
                      aria-label="file-upload-after-fail"
                      @change="onFileSelect"
                    />
                  </label>
                </v-btn>
              </div>
            </span>
          </span>
          <span v-else>
            <span v-if="status.uploadStatus === 'uploading'">
              <v-icon class="icon_upload" size="50px"> refresh </v-icon>
              <div class="upload_status">Upload läuft</div>
              <div>{{ status.file ? status.file.name : "" }}</div>
            </span>
            <span v-else-if="status.uploadStatus === 'succeeded'">
              <v-icon class="icon_upload" size="50px"> upload_file </v-icon>
              <div class="upload_status">
                Die Datei {{ status.file ? status.file.name : "" }} wurde
                erfolgreich hochgeladen
              </div>
            </span>
            <span v-else>
              <v-icon class="icon_upload" size="50px"> upload_file </v-icon>
              <div class="upload_status">Datei in diesen Bereich ziehen</div>
              <div>oder</div>
              <div>
                <v-btn
                  class="ris-btn"
                  :rounded="0"
                  :ripple="false"
                  :flat="true"
                  color="blue800"
                >
                  <v-icon> search </v-icon>
                  <label class="custom-file-label" for="file-upload">
                    Festplatte durchsuchen
                    <input
                      id="file-upload"
                      class="custom-file-input"
                      type="file"
                      name="file-upload"
                      aria-label="file-upload"
                      @change="onFileSelect"
                    />
                  </label>
                </v-btn>
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
    border: $border-style-error;
    &:hover {
      border: $border-style-error;
    }
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
  margin: 16px 0px 10px 0;
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

.custom-file-input::-webkit-file-upload-button {
  visibility: hidden;
}

.custom-file-input {
  opacity: 0;
  position: absolute;
  z-index: -1;
}

.custom-file-label {
  cursor: pointer;
}

.ris-btn {
  font-family: $font-bold;
  margin-top: 16px;
  margin-bottom: 10px;

  &.v-btn {
    text-transform: none;
    font-size: var(--v-btn-size);

    &--size-default {
      --v-btn-height: 48px;
      --v-btn-size: 1rem;
      padding: rem(13px) rem(24px);
    }

    &--size-small {
      --v-btn-height: 40px;
      --v-btn-size: 1rem;
      padding: rem(9px) rem(24px);
    }

    &--size-large,
    &--size-x-large {
      --v-btn-height: 64px;
      --v-btn-size: 1.125rem;
      padding: rem(19px) rem(24px);
    }

    &:not(.v-btn--icon) {
      .v-icon--start {
        margin-inline-start: 0;
      }

      .v-icon--end {
        margin-inline-end: 0;
      }
    }

    &:hover {
      background-color: $blue700 !important;

      .v-btn__overlay {
        opacity: 0;
      }
    }

    &:active {
      background-color: $blue500 !important;

      .v-btn__overlay {
        opacity: 0;
      }
    }

    &:focus-visible {
      outline: 2px solid $blue800;
      outline-offset: 2px;

      .v-btn__overlay {
        opacity: 0;
      }
    }
  }
}
</style>
