<script setup lang="ts">
import DocumentUnitCategories from "@/components/DocumentUnitCategories.vue"
import RouteErrorDisplay from "@/components/RouteErrorDisplay.vue"
import documentUnitService from "@/services/documentUnitService"

const props = defineProps<{
  documentNumber: string
}>()

const { data: documentUnit, error } =
  await documentUnitService.getByDocumentNumber(props.documentNumber)
</script>

<template>
  <DocumentUnitCategories v-if="documentUnit" :document-unit="documentUnit" />
  <RouteErrorDisplay v-else :error="error" />
</template>
