<script lang="ts" setup>
import { useHead } from "@unhead/vue"
import DocumentUnitPublication from "@/components/DocumentUnitPublication.vue"
import DocumentUnit from "@/domain/documentUnit"

const props = defineProps<{
  documentUnit: DocumentUnit
  showNavigationPanel?: boolean
}>()
const emit = defineEmits<{
  updateDocumentUnit: [void]
}>()
useHead({
  title:
    props.documentUnit.documentNumber + " · NeuRIS Rechtsinformationssystem",
})

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
