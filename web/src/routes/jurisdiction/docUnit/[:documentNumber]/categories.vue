<script setup lang="ts">
import DocUnitCategories from "@/components/DocumentUnitCategories.vue"
import docUnitService from "@/services/documentUnitService"

const props = defineProps<{
  documentNumber: string
}>()

const { data: documentUnit, error } = await docUnitService.getByDocumentNumber(
  props.documentNumber
)
</script>

<template>
  <DocUnitCategories v-if="documentUnit" :document-unit="documentUnit" />
  <div v-else>
    <h2>{{ error?.title }}</h2>
    <p>{{ error?.description }}</p>
  </div>
</template>
