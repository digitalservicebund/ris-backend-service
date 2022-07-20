<script lang="ts" setup>
import dayjs from "dayjs"
import { onMounted, ref } from "vue"
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

const fileAsHtml = ref<string>("Loading data ....")
onMounted(async () => {
  fileAsHtml.value = await fileService.getDocxFileAsHtml(props.s3Path)
})
</script>

<template>
  <v-container class="fileviewer-info-panel">
    <v-row>
      <v-col sm="3" md="2">
        Hochgeladen am
        <div class="fileviewer-info-panel-value">
          {{ dayjs(uploadTimeStamp).format("DD.MM.YYYY") || " - " }}
        </div>
      </v-col>
      <v-col sm="3" md="2">
        Format
        <div class="fileviewer-info-panel-value">
          {{ fileType || " - " }}
        </div>
      </v-col>
      <v-col sm="3" md="2">
        Von
        <div class="fileviewer-info-panel-value">USER NAME</div>
      </v-col>
      <v-col sm="6">
        Dateiname
        <div class="fileviewer-info-panel-value">
          {{ fileName || " - " }}
        </div>
      </v-col>
      <v-col cols="4" />
    </v-row>
    <v-row class="fileviewer-info-panel">
      <v-col cols="12">
        <TextButton
          icon="delete"
          label="Datei löschen"
          @click="$emit('deleteFile')"
        />
      </v-col>
    </v-row>
  </v-container>
  <v-container>
    <v-row>
      <v-col cols="12">
        <TextEditor :value="fileAsHtml" field-size="max" :editable="false" />
      </v-col>
    </v-row>
  </v-container>
</template>

<style lang="scss">
.fileviewer-info-panel {
  background-color: $white;
}

.fileviewer-info-panel-value {
  color: $gray900;
  font-weight: bold;
}
</style>
