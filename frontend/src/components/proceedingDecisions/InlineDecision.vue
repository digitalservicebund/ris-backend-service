<script setup lang="ts">
import { ProceedingDecision } from "@/domain/documentUnit"

defineProps<{
  decision: ProceedingDecision
}>()
</script>

<template>
  <router-link
    v-if="ProceedingDecision.hasLink(decision)"
    class="text-blue-800 underline"
    target="_blank"
    :to="{
      name: 'caselaw-documentUnit-:documentNumber-categories',
      params: { documentNumber: decision.documentNumber },
    }"
  >
    {{ ProceedingDecision.renderDecision(decision) }}
  </router-link>

  <span v-else>{{ ProceedingDecision.renderDecision(decision) }}</span>
</template>
