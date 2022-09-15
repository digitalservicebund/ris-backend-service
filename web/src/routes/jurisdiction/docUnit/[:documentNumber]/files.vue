<script lang="ts" setup>
import { ref } from "vue"
import DocumentUnitFiles from "@/components/DocumentUnitFiles.vue"
import documentUnitService from "@/services/documentUnitService"
import fileService from "@/services/fileService"

const props = defineProps<{ documentNumber: string }>()

const { data: documentUnit, error } = await (async () => {
  const response = await documentUnitService.getByDocumentNumber(
    props.documentNumber
  )
  return {
    data: ref(response.data),
    error: response.error,
  }
})()

const handleDeleteFile = async () => {
  await fileService.deleteFile(documentUnit.value.uuid)
  documentUnit.value = (
    await documentUnitService.getByDocumentNumber(props.documentNumber)
  ).data
}
</script>

<template>
  <DocumentUnitFiles
    v-if="documentUnit"
    :document-unit="documentUnit"
    @delete-file="handleDeleteFile"
    @update-doc-unit="Object.assign(documentUnit, $event)"
  />
  <div v-else>
    <h2>{{ error?.title }}</h2>
    <p>{{ error?.description }}</p>
  </div>
</template>
