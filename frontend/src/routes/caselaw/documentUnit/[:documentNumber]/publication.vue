<script lang="ts" setup>
import { ref } from "vue"
import DocumentUnitPublication from "@/components/DocumentUnitPublication.vue"
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
  <DocumentUnitPublication
    v-if="documentUnit"
    :document-unit="(documentUnit as DocumentUnit)"
  />
  <div v-else>
    <h2>{{ error?.title }}</h2>
    <p>{{ error?.description }}</p>
  </div>
</template>
