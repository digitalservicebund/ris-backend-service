<script lang="ts" setup>
import { ref } from "vue"
import DocumentUnitFiles from "@/components/DocumentUnitFiles.vue"
import RouteErrorDisplay from "@/components/RouteErrorDisplay.vue"
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
    :document-unit="(documentUnit as DocumentUnit)"
    @update-document-unit="Object.assign(documentUnit as DocumentUnit, $event)"
  />
  <RouteErrorDisplay v-else :error="error" />
</template>
