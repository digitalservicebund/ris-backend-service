<script setup lang="ts">
import FieldOfLawMain from "@/components/FieldOfLawMain.vue"
import DocumentUnit from "@/domain/documentUnit"

const props = defineProps<{
  documentUnit: DocumentUnit
}>()
</script>

<template>
  <div class="mb-[4rem]">
    <h1 class="heading-02-regular mb-[1rem]">Inhaltliche Erschließung</h1>
    <!-- Schlagwörter -->
    <FieldOfLawMain :document-unit-uuid="props.documentUnit.uuid" />
    <!-- Normen -->
    <!-- Aktivzitierung -->
  </div>
</template>
