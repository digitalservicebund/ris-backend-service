<script setup lang="ts">
import { useHead, useSeoMeta } from "@unhead/vue"
import ErrorPage from "@/components/ErrorPage.vue"
import DocumentUnitPreview from "@/components/preview/DocumentUnitPreview.vue"
import documentUnitService from "@/services/documentUnitService"

const props = defineProps<{ documentNumber: string }>()

const { data: documentUnit, error } =
  await documentUnitService.getByDocumentNumber(props.documentNumber)

useHead({
  title: props.documentNumber + " · NeuRIS Rechtsinformationssystem",
})

useSeoMeta({
  ogTitle: props.documentNumber + " · NeuRIS Rechtsinformationssystem",
})
</script>
<template>
  <DocumentUnitPreview
    v-if="documentUnit"
    :document-unit="documentUnit"
  ></DocumentUnitPreview>
  <ErrorPage v-else :error="error" :title="error?.title" />
</template>
