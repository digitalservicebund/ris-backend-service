<script setup lang="ts">
import { useHead } from "@unhead/vue"
import { storeToRefs } from "pinia"
import { onMounted } from "vue"
import DocumentUnitCategories from "@/components/DocumentUnitCategories.vue"
import { ValidationError } from "@/components/input/types"
import DocumentUnit from "@/domain/documentUnit"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const props = defineProps<{
  documentNumber: string
  validationErrors: ValidationError[]
}>()

useHead({
  title: props.documentNumber + " · NeuRIS Rechtsinformationssystem",
})

const store = useDocumentUnitStore()
const { documentUnit } = storeToRefs(store)

onMounted(async () => {
  // In the future, this get request will happen one layer above, so all routes can share the same docunit ref
  await store.loadDocumentUnit(props.documentNumber)
})
</script>

<template>
  <DocumentUnitCategories
    v-if="documentUnit"
    :document-unit="documentUnit as DocumentUnit"
    :validation-errors="validationErrors"
  />
</template>
