<script lang="ts" setup>
import DocumentUnitPublication from "@/components/DocumentUnitPublication.vue"
import DocumentUnit from "@/domain/documentUnit"

defineProps<{
  documentUnit: DocumentUnit
  showNavigationPanel?: boolean
}>()
const emit = defineEmits<{
  updateDocumentUnit: [void]
}>()

async function loadDocumentUnit() {
  emit("updateDocumentUnit")
}
</script>

<template>
  <DocumentUnitPublication
    v-if="documentUnit"
    :document-unit="documentUnit as DocumentUnit"
    @update-document-unit="loadDocumentUnit"
  />
</template>
