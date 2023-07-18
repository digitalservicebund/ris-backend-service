<script setup lang="ts">
import InlineDecision from "@/components/proceedingDecisions/InlineDecision.vue"
import ProceedingDecision from "@/domain/proceedingDecision"

defineProps<{
  decisions: ProceedingDecision[]
}>()

const emit = defineEmits<{
  removeLink: [decision: ProceedingDecision]
}>()
</script>

<template>
  <div class="flex w-full flex-col items-start text-start">
    <div
      v-for="decision in decisions"
      :key="decision.uuid"
      class="flex-start ds-label-02-reg flex w-full justify-between border-b-1 border-b-blue-500 py-10 first:pt-0 last:border-none last:pb-0"
    >
      <InlineDecision :decision="decision" />
      <span
        aria-label="Löschen"
        class="font-base icon material-icons ml-[1.5rem] cursor-pointer text-blue-800"
        tabindex="0"
        @click="emit('removeLink', decision)"
        @keyup.enter="emit('removeLink', decision)"
        >delete_outline</span
      >
    </div>
  </div>
</template>
