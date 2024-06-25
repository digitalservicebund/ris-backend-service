<script setup lang="ts">
import DocumentUnitCategories from "@/components/DocumentUnitCategories.vue"
import { ValidationError } from "@/components/input/types"
import DocumentUnit from "@/domain/documentUnit"

defineProps<{
  documentUnit: DocumentUnit
  validationErrors: ValidationError[]
}>()

const emits = defineEmits<{
  documentUnitUpdatedLocally: [DocumentUnit]
  saveDocumentUnitToServer: []
}>()

function documentUnitUpdatedLocally(updatedDocumentUnit: DocumentUnit) {
  emits("documentUnitUpdatedLocally", updatedDocumentUnit)
}

function saveDocumentUnitToServer() {
  emits("saveDocumentUnitToServer")
}
</script>

<template>
  <DocumentUnitCategories
    v-if="documentUnit"
    :document-unit="documentUnit"
    :validation-errors="validationErrors"
    @document-unit-updated-locally="documentUnitUpdatedLocally"
    @save-document-unit-to-server="saveDocumentUnitToServer"
  />
</template>
