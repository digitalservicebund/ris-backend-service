<script setup lang="ts">
import DecisionSummary from "@/components/DecisionSummary.vue"
import RelatedDocumentation from "@/domain/relatedDocumentation"

defineProps<{
  decision: RelatedDocumentation
}>()
</script>

<template>
  <DecisionSummary :decision="decision"></DecisionSummary>
</template>
