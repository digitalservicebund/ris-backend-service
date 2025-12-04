<script lang="ts" setup>
import DocumentationUnitSummary from "@/components/DocumentationUnitSummary.vue"
import RelatedPendingProceeding from "@/domain/pendingProceedingReference"

defineProps<{
  data: RelatedPendingProceeding
}>()
</script>

<template>
  <div class="flex w-full justify-between">
    <div class="flex flex-row items-center">
      <DocumentationUnitSummary :data="data"> </DocumentationUnitSummary>
    </div>
  </div>
</template>
