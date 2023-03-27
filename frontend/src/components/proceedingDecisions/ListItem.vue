<script setup lang="ts">
import { ProceedingDecision } from "@/domain/documentUnit"

defineProps<{
  decision: ProceedingDecision
}>()
</script>

<template>
  {{ decision.court?.type }} {{ decision.court?.location }}
  {{ decision.documentType?.label }}
  {{ decision.date }}
  {{ decision.fileNumber }}
</template>
