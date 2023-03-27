<script setup lang="ts">
import dayjs from "dayjs"
import { ProceedingDecision } from "@/domain/documentUnit"

defineProps<{
  decision: ProceedingDecision
}>()

function renderDecision(decision: ProceedingDecision) {
  return [
    ...(decision.court
      ? [`${decision.court.type} ${decision.court.location}`]
      : []),
    ...(decision.documentType ? [decision.documentType?.jurisShortcut] : []),
    ...(decision.date ? [dayjs(decision.date).format("DD.MM.YYYY")] : []),
    ...(decision.fileNumber ? [decision.fileNumber] : []),
  ].join(", ")
}
</script>

<template>
  {{ renderDecision(decision) }}
</template>
