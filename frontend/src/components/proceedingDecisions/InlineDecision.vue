<script setup lang="ts">
import dayjs from "dayjs"
import { ProceedingDecision } from "@/domain/documentUnit"

const props = defineProps<{
  decision: ProceedingDecision
}>()

function renderDecision(): string {
  const decision = props.decision
  return [
    ...(decision.court ? [`${decision.court.label}`] : []),
    ...(decision.documentType ? [decision.documentType?.jurisShortcut] : []),
    ...(decision.date ? [dayjs(decision.date).format("DD.MM.YYYY")] : []),
    ...(decision.fileNumber ? [decision.fileNumber] : []),
    ...(decision.documentNumber ? [decision.documentNumber] : []),
  ].join(", ")
}

function hasLink(): boolean {
  return props.decision.dataSource !== "PROCEEDING_DECISION"
}
</script>

<template>
  <router-link
    v-if="hasLink()"
    class="text-blue-800 underline"
    target="_blank"
    :to="{
      name: 'caselaw-documentUnit-:documentNumber-categories',
      params: { documentNumber: decision.documentNumber },
    }"
  >
    {{ renderDecision() }}
  </router-link>

  <span v-else>{{ renderDecision() }}</span>
</template>
