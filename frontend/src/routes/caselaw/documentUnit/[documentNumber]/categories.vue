<script setup lang="ts">
import { useHead } from "@unhead/vue"
import DocumentUnitCategories from "@/components/DocumentUnitCategories.vue"
import { ValidationError } from "@/components/input/types"
import DocumentUnit from "@/domain/documentUnit"

const props = defineProps<{
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

useHead({
  title:
    props.documentUnit.documentNumber + " · NeuRIS Rechtsinformationssystem",
})
</script>

<template>
  <DocumentUnitCategories
    v-if="documentUnit"
    :document-unit="documentUnit"
    :validation-errors="validationErrors"
    @documenet-unit-save="documentUnitSave"
    @document-unit-update="updateDocumentUnit"
  />
</template>
