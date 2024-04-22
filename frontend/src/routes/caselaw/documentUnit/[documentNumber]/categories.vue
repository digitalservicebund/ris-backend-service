<script setup lang="ts">
import { useRoute } from "vue-router"
import DocumentUnitCategories from "@/components/DocumentUnitCategories.vue"
import ErrorPage from "@/components/ErrorPage.vue"
import documentUnitService from "@/services/documentUnitService"

const props = defineProps<{
  documentNumber: string
}>()

const route = useRoute()

const { data: documentUnit, error } =
  await documentUnitService.getByDocumentNumber(props.documentNumber)
</script>

<template>
  <DocumentUnitCategories
    v-if="documentUnit"
    :document-unit="documentUnit"
    :show-attachment-panel="route.query.showAttachmentPanel === 'true'"
  />
  <ErrorPage v-else :error="error" :title="error?.title" />
</template>
