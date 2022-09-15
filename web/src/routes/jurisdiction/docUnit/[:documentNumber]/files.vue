<script lang="ts" setup>
import { ref } from "vue"
import DocUnitWrapper from "@/components/DocUnitWrapper.vue"
import FileUpload from "@/components/FileUpload.vue"
import FileViewer from "@/components/FileViewer.vue"
import docUnitService from "@/services/docUnitService"
import fileService from "@/services/fileService"

const props = defineProps<{ documentNumber: string }>()
const docUnit = ref(
  (await docUnitService.getByDocumentNumber(props.documentNumber)).data
)

const handleDeleteFile = async () => {
  await fileService.deleteFile(docUnit.value.uuid)
  docUnit.value = (
    await docUnitService.getByDocumentNumber(props.documentNumber)
  ).data
}
</script>

<template>
  <DocUnitWrapper :doc-unit="docUnit">
    <v-container>
      <v-row>
        <v-col><h2>Dokumente</h2></v-col>
      </v-row>
    </v-container>
    <FileViewer
      v-if="docUnit.hasFile"
      :s3-path="(docUnit.s3path as string)"
      :file-name="docUnit.filename"
      :upload-time-stamp="docUnit.fileuploadtimestamp"
      :file-type="docUnit.filetype"
      @delete-file="handleDeleteFile"
    />
    <FileUpload
      v-else
      :doc-unit-uuid="docUnit.uuid"
      @update-doc-unit="Object.assign(docUnit, $event)"
    />
  </DocUnitWrapper>
</template>
