<script setup lang="ts">
import { ProceedingDecision } from "@/domain/documentUnit"

defineProps<{
  decisions: ProceedingDecision[]
}>()
</script>

<template>
  <ul>
    <li
      v-for="decision in decisions"
      :key="decision.uuid"
      class="link-01-bold mb-24 mt-12"
    >
      {{ decision.court?.type }} {{ decision.court?.location }}
      {{ decision.documentType?.label }}
      {{ decision.date }}
      {{ decision.fileNumber }}
    </li>
  </ul>
</template>
