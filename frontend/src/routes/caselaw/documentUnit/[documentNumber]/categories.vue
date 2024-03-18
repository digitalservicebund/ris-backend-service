<script setup lang="ts">
import DocumentUnitCategories from "@/components/DocumentUnitCategories.vue"
import ErrorPage from "@/components/ErrorPage.vue"
import { LOADING_ERROR } from "@/i18n/errors.json"
import documentUnitService from "@/services/documentUnitService"

const props = defineProps<{
  documentNumber: string
}>()

const errorTitle = LOADING_ERROR.title

const { data: documentUnit, error } =
  await documentUnitService.getByDocumentNumber(props.documentNumber)
</script>

<template>
  <DocumentUnitCategories v-if="documentUnit" :document-unit="documentUnit" />
  <ErrorPage v-else :error="error" :title="errorTitle" />
</template>
