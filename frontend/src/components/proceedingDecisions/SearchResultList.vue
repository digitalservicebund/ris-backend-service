<script setup lang="ts">
import { ProceedingDecision } from "@/domain/documentUnit"

defineProps<{ searchResults: SearchResults }>()
const emits = defineEmits<{
  (event: "linkDecision", uuid: string): void
}>()
</script>

<script lang="ts">
export type SearchResults = {
  decision: ProceedingDecision
  isLinked: boolean
}[]
</script>

<template>
  <strong>Suchergebnis:</strong>
  <div class="table">
    <div
      v-for="searchResult in searchResults"
      :key="searchResult.decision.uuid"
      class="link-01-bold mb-24 mt-12 table-row underline"
    >
      <div class="table-cell">
        <InlineDecision :decision="searchResult.decision" />
        <span :v-if="searchResult.isLinked">Bereits hinzugefügt</span>
      </div>
      <div class="p-8 table-cell">
        <TextButton
          aria-label="Treffer übernehmen"
          label="Übernehmen"
          @click="emits('linkDecision', searchResult.decision.uuid as string)"
        />
      </div>
    </div>
  </div>
</template>
