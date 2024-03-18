<script setup lang="ts">
import DocumentUnitCategories from "@/components/DocumentUnitCategories.vue"
import ErrorPage from "@/components/ErrorPage.vue"
import documentUnitService from "@/services/documentUnitService"

const props = defineProps<{
  documentNumber: string
}>()

const { data: documentUnit, error } =
  await documentUnitService.getByDocumentNumber(props.documentNumber)
</script>

<template>
  <DocumentUnitCategories v-if="documentUnit" :document-unit="documentUnit" />
  <ErrorPage v-else :error="error" :title="error?.title" />
</template>
