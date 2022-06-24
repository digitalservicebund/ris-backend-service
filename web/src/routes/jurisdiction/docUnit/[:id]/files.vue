<script lang="ts" setup>
import { ref } from "vue"
import DocUnitDetail from "./index.vue"
import FileUpload from "@/components/FileUpload.vue"
import FileViewer from "@/components/FileViewer.vue"
import docUnitService from "@/services/docUnitService"
import fileService from "@/services/fileService"

const props = defineProps<{ id: string }>()
const docUnit = ref(await docUnitService.getById(String(props.id)))

const handleDeleteFile = async () => {
  await fileService.deleteFile(props.id)
  docUnit.value = await docUnitService.getById(String(props.id))
}
</script>

<template>
  <DocUnitDetail :doc-unit="docUnit">
    <v-container>
      <v-row>
        <v-col><h2>Dokumente</h2></v-col>
      </v-row>
    </v-container>
    <FileViewer
      v-if="docUnit.hasFile"
      :s3-path="(docUnit.s3path as string)"
      :file-name="docUnit.filename || ' - '"
      :upload-time-stamp="docUnit.fileuploadtimestamp || ' - '"
      :file-type="docUnit.filetype || ' - '"
      @delete-file="handleDeleteFile"
    />
    <FileUpload v-else :doc-unit-id="id" />
  </DocUnitDetail>
</template>
