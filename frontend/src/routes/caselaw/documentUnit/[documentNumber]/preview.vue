<script setup lang="ts">
import ErrorPage from "@/components/ErrorPage.vue"
import DocumentUnitPreview from "@/components/preview/DocumentUnitPreview.vue"
import documentUnitService from "@/services/documentUnitService"

const props = defineProps<{ documentNumber: string }>()

const { data: documentUnit, error } =
  await documentUnitService.getByDocumentNumber(props.documentNumber)
</script>
<template>
  <DocumentUnitPreview
    v-if="documentUnit"
    :document-unit="documentUnit"
  ></DocumentUnitPreview>
  <ErrorPage v-else :error="error" :title="error?.title" />
</template>
