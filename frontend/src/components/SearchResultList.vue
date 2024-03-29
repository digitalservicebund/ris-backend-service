<script setup lang="ts">
import InlineDecision from "./InlineDecision.vue"
import TextButton from "@/components/input/TextButton.vue"
import LoadingSpinner from "@/components/LoadingSpinner.vue"
import RelatedDocumentation from "@/domain/relatedDocumentation"

defineProps<{
  searchResults?: SearchResults<RelatedDocumentation>
  isLoading: boolean
}>()

const emits =
  defineEmits<(event: "linkDecision", decision: RelatedDocumentation) => void>()
</script>

<script lang="ts">
export type SearchResults<Type extends RelatedDocumentation> = {
  decision: Type
  isLinked: boolean
}[]
</script>

<template>
  <div v-if="!!isLoading" class="mt-40 text-center">
    <LoadingSpinner />
  </div>

  <div
    v-else-if="searchResults?.length === 0"
    class="grid justify-items-center"
  >
    <span>Keine Ergebnisse gefunden.</span>
  </div>

  <div v-else-if="searchResults" class="mt-16 table">
    <div
      v-for="searchResult in searchResults"
      :key="searchResult.decision.uuid"
      class="mb-24 mt-12 table-row"
    >
      <div class="table-cell">
        <InlineDecision :decision="searchResult.decision" />
      </div>
      <div class="table-cell p-8">
        <TextButton
          aria-label="Treffer übernehmen"
          class="ml-24"
          :disabled="searchResult.isLinked"
          label="Übernehmen"
          size="small"
          @click.stop="emits('linkDecision', searchResult.decision)"
        />
      </div>
      <span
        v-if="searchResult.isLinked"
        class="ds-label-03-reg ml-24 rounded-full bg-green-700 px-24 py-4 text-white"
        >Bereits hinzugefügt</span
      >
    </div>
  </div>
</template>
