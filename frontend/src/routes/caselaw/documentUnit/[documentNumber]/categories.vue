<script setup lang="ts">
import DocumentUnitCategories from "@/components/DocumentUnitCategories.vue"
import { ValidationError } from "@/components/input/types"
import DocumentUnit from "@/domain/documentUnit"

defineProps<{
  documentUnit: DocumentUnit
  validationErrors: ValidationError[]
}>()

const emits = defineEmits<{
  documentUnitUpdate: [DocumentUnit]
  documentUnitSave: []
}>()

function updateDocumentUnit(updatedDocumentUnit: DocumentUnit) {
  emits("documentUnitUpdate", updatedDocumentUnit)
}

function documentUnitSave() {
  emits("documentUnitSave")
}
</script>

<template>
  <DocumentUnitCategories
    v-if="documentUnit"
    :document-unit="documentUnit"
    :validation-errors="validationErrors"
    @document-unit-save="documentUnitSave"
    @document-unit-update="updateDocumentUnit"
  />
</template>
