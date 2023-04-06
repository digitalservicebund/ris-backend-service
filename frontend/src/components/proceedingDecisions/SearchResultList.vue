<script setup lang="ts">
import InlineDecision from "./InlineDecision.vue"
import { SearchResults } from "@/domain/documentUnit"
import TextButton from "@/shared/components/input/TextButton.vue"

defineProps<{ searchResults: SearchResults }>()
const emits = defineEmits<{
  (event: "linkDecision", uuid: string): void
}>()
</script>

<script lang="ts"></script>

<template>
  <strong>Suchergebnis:</strong>
  <div class="table">
    <div
      v-for="searchResult in searchResults"
      :key="searchResult.decision.uuid"
      class="mb-24 mt-12 table-row"
    >
      <div class="link-01-bold table-cell">
        <InlineDecision :decision="searchResult.decision" />
      </div>
      <div class="p-8 table-cell">
        <TextButton
          aria-label="Treffer übernehmen"
          class="ml-24"
          :disabled="searchResult.isLinked"
          label="Übernehmen"
          @click="emits('linkDecision', searchResult.decision.uuid as string)"
        />
      </div>
      <span
        v-if="searchResult.isLinked"
        class="bg-green-700 label-03-reg ml-24 px-24 py-4 rounded-full text-white"
        >Bereits hinzugefügt</span
      >
    </div>
  </div>
</template>
