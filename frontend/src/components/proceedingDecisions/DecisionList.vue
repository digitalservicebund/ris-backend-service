<script setup lang="ts">
import InlineDecision from "@/components/proceedingDecisions/InlineDecision.vue"
import { ProceedingDecision } from "@/domain/documentUnit"

defineProps<{
  decisions: ProceedingDecision[]
}>()

const emit = defineEmits<{
  (e: "removeLink", decision: ProceedingDecision): Promise<void>
}>()
</script>

<template>
  <ul>
    <li
      v-for="decision in decisions"
      :key="decision.uuid"
      class="link-01-bold mb-24 mt-12"
    >
      <InlineDecision :decision="decision" />
      <span
        aria-label="Löschen"
        class="cursor-pointer font-base icon material-icons ml-[1.5rem] text-blue-800"
        @click="emit('removeLink', decision)"
        @keyup.enter="emit('removeLink', decision)"
        >delete_outline</span
      >
    </li>
  </ul>
</template>
