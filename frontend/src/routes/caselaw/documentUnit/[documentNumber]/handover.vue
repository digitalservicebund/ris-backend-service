<script lang="ts" setup>
import DocumentUnitHandover from "@/components/DocumentUnitHandover.vue"
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
  <DocumentUnitHandover
    v-if="documentUnit"
    :document-unit="documentUnit as DocumentUnit"
    @request-document-unit-from-server="requestDocumentUnitFromServer"
  />
</template>
