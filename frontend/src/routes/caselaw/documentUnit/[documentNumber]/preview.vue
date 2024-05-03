<script setup lang="ts">
import DocumentUnitPreview from "@/components/DocumentUnitPreview.vue"
import ErrorPage from "@/components/ErrorPage.vue"
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
