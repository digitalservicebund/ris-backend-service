<script setup lang="ts">
import { ProceedingDecision } from "@/domain/documentUnit"

defineProps<{
  decision: ProceedingDecision
}>()
</script>

<template>
  <div
    class="border-b-1 border-b-blue-500 first:pt-0 flex flex-start label-02-reg last:border-none last:pb-0 py-10 w-full"
  >
    <router-link
      v-if="ProceedingDecision.hasLink(decision)"
      class="link-01-bold underline"
      target="_blank"
      :to="{
        name: 'caselaw-documentUnit-:documentNumber-categories',
        params: { documentNumber: decision.documentNumber },
      }"
    >
      {{ ProceedingDecision.renderDecision(decision) }}
    </router-link>

    <span v-else class="label-02-reg">{{
      ProceedingDecision.renderDecision(decision)
    }}</span>
  </div>
</template>
