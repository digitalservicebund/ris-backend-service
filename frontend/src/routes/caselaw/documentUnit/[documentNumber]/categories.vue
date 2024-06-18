<script setup lang="ts">
import { useHead } from "@unhead/vue"
import DocumentUnitCategories from "@/components/DocumentUnitCategories.vue"
import DocumentUnit from "@/domain/documentUnit"

const props = defineProps<{
  documentUnit: DocumentUnit
}>()

useHead({
  title:
    props.documentUnit.documentNumber + " · NeuRIS Rechtsinformationssystem",
})
</script>

<template>
  <DocumentUnitCategories v-if="documentUnit" :document-unit="documentUnit" />
</template>
