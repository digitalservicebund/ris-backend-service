<script lang="ts" setup>
import { ref } from "vue"
import DocUnitFiles from "@/components/DocumentUnitFiles.vue"
import docUnitService from "@/services/documentUnitService"
import fileService from "@/services/fileService"

const props = defineProps<{ documentNumber: string }>()

const { data: docUnit, error } = await (async () => {
  const response = await docUnitService.getByDocumentNumber(
    props.documentNumber
  )
  return {
    data: ref(response.data),
    error: response.error,
  }
})()

const handleDeleteFile = async () => {
  await fileService.deleteFile(docUnit.value.uuid)
  docUnit.value = (
    await docUnitService.getByDocumentNumber(props.documentNumber)
  ).data
}
</script>

<template>
  <DocUnitFiles
    v-if="docUnit"
    :doc-unit="docUnit"
    @delete-file="handleDeleteFile"
    @update-doc-unit="Object.assign(docUnit, $event)"
  />
  <div v-else>
    <h2>{{ error?.title }}</h2>
    <p>{{ error?.description }}</p>
  </div>
</template>
