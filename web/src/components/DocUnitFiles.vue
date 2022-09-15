<script lang="ts" setup>
import DocUnitWrapper from "@/components/DocUnitWrapper.vue"
import FileUpload from "@/components/FileUpload.vue"
import FileViewer from "@/components/FileViewer.vue"
import DocUnit from "@/domain/docUnit"

defineProps<{ docUnit: DocUnit }>()

const emits = defineEmits<{
  (e: "updateDocUnit", updatedDocUnit: DocUnit): void
  (e: "deleteFile"): void
}>()
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
      @delete-file="emits('deleteFile')"
    />
    <FileUpload
      v-else
      :doc-unit-uuid="docUnit.uuid"
      @update-doc-unit="emits('updateDocUnit', $event)"
    />
  </DocUnitWrapper>
</template>
