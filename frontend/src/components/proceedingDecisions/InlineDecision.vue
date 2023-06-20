<script setup lang="ts">
import ActiveCitation from "@/domain/activeCitation"
import ProceedingDecision from "@/domain/proceedingDecision"

defineProps<{
  decision: ProceedingDecision | ActiveCitation
}>()
</script>

<template>
  <div>
    <router-link
      v-if="decision.isDocUnit()"
      class="link-01-bold underline"
      tabindex="-1"
      target="_blank"
      :to="{
        name: 'caselaw-documentUnit-:documentNumber-categories',
        params: { documentNumber: decision.documentNumber },
      }"
    >
      <button class="underline">
        {{ decision.renderDecision }}
      </button>
    </router-link>

    <span v-else class="label-02-reg">{{ decision.renderDecision }}</span>
  </div>
</template>
