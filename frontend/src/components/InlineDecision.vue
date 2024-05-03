<script setup lang="ts">
import RelatedDocumentation from "@/domain/relatedDocumentation"

defineProps<{
  decision: RelatedDocumentation
}>()
</script>

<template>
  <div>
    <router-link
      v-if="decision.hasForeignSource"
      class="ds-link-01-bold underline"
      tabindex="-1"
      target="_blank"
      :to="{
        name: 'caselaw-documentUnit-documentNumber-preview',
        params: { documentNumber: decision.documentNumber },
      }"
    >
      <button
        class="underline focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800"
      >
        {{ decision.renderDecision }}
      </button>
    </router-link>

    <span v-else class="ds-label-02-reg">{{ decision.renderDecision }}</span>
  </div>
</template>
