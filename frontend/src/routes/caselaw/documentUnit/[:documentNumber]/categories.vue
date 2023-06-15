<script setup lang="ts">
import DocumentUnitCategories from "@/components/DocumentUnitCategories.vue"
import documentUnitService from "@/services/documentUnitService"

const props = defineProps<{
  documentNumber: string
}>()

const { data: documentUnit, error } =
  await documentUnitService.getByDocumentNumber(props.documentNumber)
</script>

<template>
  <DocumentUnitCategories v-if="documentUnit" :document-unit="documentUnit" />
  <div v-else class="m-24">
    <h2>{{ error?.title }}</h2>
    <p>{{ error?.description }}</p>
  </div>
</template>
