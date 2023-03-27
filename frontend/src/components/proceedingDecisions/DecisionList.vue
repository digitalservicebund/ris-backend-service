<script setup lang="ts">
import ListItem from "@/components/proceedingDecisions/ListItem.vue"
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
      <ListItem :decision="decision" />
      <span aria-label="Löschen">Löschen</span>
    </li>
  </ul>
</template>
