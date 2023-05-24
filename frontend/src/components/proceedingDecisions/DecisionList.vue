<script setup lang="ts">
import dayjs from "dayjs"
import { computed } from "vue"
import InlineDecision from "@/components/proceedingDecisions/InlineDecision.vue"
import ProceedingDecision from "@/domain/proceedingDecision"

const props = defineProps<{
  decisions: ProceedingDecision[]
}>()

const emit = defineEmits<{
  (e: "removeLink", decision: ProceedingDecision): Promise<void>
}>()

const sortedDecisions = computed(() =>
  [...props.decisions].sort((a, b) => dayjs(b.date).diff(a.date))
)
</script>

<template>
  <div class="flex flex-col items-start text-start w-full">
    <div
      v-for="decision in sortedDecisions"
      :key="decision.uuid"
      class="border-b-1 border-b-blue-500 first:pt-0 flex flex-start justify-between label-02-reg last:border-none last:pb-0 py-10 w-full"
    >
      <InlineDecision :decision="decision" />
      <span
        aria-label="Löschen"
        class="cursor-pointer font-base icon material-icons ml-[1.5rem] text-blue-800"
        tabindex="0"
        @click="emit('removeLink', decision)"
        @keyup.enter="emit('removeLink', decision)"
        >delete_outline</span
      >
    </div>
  </div>
</template>
