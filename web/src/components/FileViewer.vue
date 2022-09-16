<script lang="ts" setup>
import dayjs from "dayjs"
import { onMounted, ref } from "vue"
import PopupModal from "./PopupModal.vue"
import TextButton from "./TextButton.vue"
import TextEditor from "./TextEditor.vue"
import fileService from "@/services/fileService"

const props = defineProps<{
  s3Path: string
  fileName?: string
  fileType?: string
  uploadTimeStamp?: string
}>()

defineEmits<{ (e: "deleteFile"): void }>()

const showModal = ref(false)
const popupModalText = ref(
  ` Möchten Sie die ausgewählte Datei ${props.fileName} wirklich löschen?`
)
const confirmText = ref("Löschen")
const modalCancelButtonType = "ghost"
const modalConfirmButtonType = "secondary"
const toggleModal = () => {
  showModal.value = !showModal.value
  if (showModal.value) {
    const scrollLeft = document.documentElement.scrollLeft
    const scrollTop = document.documentElement.scrollTop
    window.onscroll = () => {
      window.scrollTo(scrollLeft, scrollTop)
    }
  } else {
    window.onscroll = () => {
      return
    }
  }
}

const fileAsHtml = ref("Dokument wird geladen.")
onMounted(async () => {
  fileAsHtml.value = await fileService.getDocxFileAsHtml(props.s3Path)
})
</script>

<template>
  <div>
    <v-container class="bg-white">
      <PopupModal
        v-if="showModal"
        :cancel-button-type="modalCancelButtonType"
        :confirm-button-type="modalConfirmButtonType"
        :confirm-text="confirmText"
        :content-text="popupModalText"
        @close-modal="toggleModal"
        @confirm-action="toggleModal(), $emit('deleteFile')"
      />
      <v-row>
        <v-col md="2" sm="3">
          Hochgeladen am
          <div class="fileviewer-info-panel-value text-gray-900">
            {{ dayjs(uploadTimeStamp).format("DD.MM.YYYY") || " - " }}
          </div>
        </v-col>
        <v-col md="2" sm="3">
          Format
          <div class="fileviewer-info-panel-value text-gray-900">
            {{ fileType || " - " }}
          </div>
        </v-col>
        <v-col md="2" sm="3">
          Von
          <div class="fileviewer-info-panel-value text-gray-900">USER NAME</div>
        </v-col>
        <v-col sm="6">
          Dateiname
          <div class="fileviewer-info-panel-value text-gray-900">
            {{ fileName || " - " }}
          </div>
        </v-col>
        <v-col cols="4" />
      </v-row>
      <v-row class="bg-white">
        <v-col cols="12">
          <TextButton
            icon="delete"
            label="Datei löschen"
            @click="toggleModal"
          />
        </v-col>
      </v-row>
    </v-container>
    <v-container>
      <v-row>
        <v-col cols="12">
          <TextEditor field-size="max" :value="fileAsHtml" />
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<style lang="scss" scoped>
.fileviewer-info-panel-value {
  font-weight: bold;
}
</style>
