<script setup lang="ts">
import { useHead, useSeoMeta } from "@unhead/vue"
import DocumentUnitPreview from "@/components/preview/DocumentUnitPreview.vue"
import DocumentUnit from "@/domain/documentUnit"

const props = defineProps<{ documentUnit: DocumentUnit }>()

useHead({
  title:
    props.documentUnit.documentNumber + " · NeuRIS Rechtsinformationssystem",
})

useSeoMeta({
  ogTitle:
    props.documentUnit.documentNumber + " · NeuRIS Rechtsinformationssystem",
})
</script>
<template>
  <DocumentUnitPreview
    v-if="documentUnit"
    :document-unit="documentUnit"
  ></DocumentUnitPreview>
</template>
