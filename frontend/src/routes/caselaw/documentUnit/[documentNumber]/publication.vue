<script lang="ts" setup>
import DocumentUnitPublication from "@/components/DocumentUnitPublication.vue"
import DocumentUnit from "@/domain/documentUnit"

defineProps<{
  documentUnit: DocumentUnit
  showNavigationPanel?: boolean
}>()
const emit = defineEmits<{
  requestDocumentUnitFromServer: [void]
}>()

async function requestDocumentUnitFromServer() {
  emit("requestDocumentUnitFromServer")
}
</script>

<template>
  <DocumentUnitPublication
    v-if="documentUnit"
    :document-unit="documentUnit as DocumentUnit"
    @request-document-unit-from-server="requestDocumentUnitFromServer"
  />
</template>
