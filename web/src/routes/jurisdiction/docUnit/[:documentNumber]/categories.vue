<script setup lang="ts">
import DocUnitCategories from "@/components/DocUnitCategories.vue"
import docUnitService from "@/services/docUnitService"

const props = defineProps<{
  documentNumber: string
}>()

const { data: docUnit, error } = await docUnitService.getByDocumentNumber(
  props.documentNumber
)
</script>

<template>
  <DocUnitCategories v-if="docUnit" :doc-unit="docUnit" />
  <div v-else>
    <h2>{{ error?.title }}</h2>
    <p>{{ error?.description }}</p>
  </div>
</template>
