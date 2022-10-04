<script lang="ts" setup>
import { ref } from "vue"
import DocumentUnitFiles from "@/components/DocumentUnitFiles.vue"
import DocumentUnit from "@/domain/documentUnit"
import documentUnitService from "@/services/documentUnitService"

const props = defineProps<{ documentNumber: string }>()

async function loadDocumentUnit() {
  const response = await documentUnitService.getByDocumentNumber(
    props.documentNumber
  )
  return {
    documentUnit: ref(response.data),
    error: response.error,
  }
}
const { documentUnit, error } = await loadDocumentUnit()
</script>

<template>
  <DocumentUnitFiles
    v-if="documentUnit"
    :document-unit="documentUnit"
    @update-document-unit="Object.assign(documentUnit as DocumentUnit, $event)"
  />
  <div v-else>
    <h2>{{ error?.title }}</h2>
    <p>{{ error?.description }}</p>
  </div>
</template>
