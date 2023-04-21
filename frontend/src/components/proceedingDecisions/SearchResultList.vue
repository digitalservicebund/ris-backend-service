<script setup lang="ts">
import { ref, watch } from "vue"
import InlineDecision from "./InlineDecision.vue"
import { ProceedingDecision } from "@/domain/documentUnit"
import TextButton from "@/shared/components/input/TextButton.vue"

const props = defineProps<{ searchResults: SearchResults | undefined }>()
const emits = defineEmits<{
  (event: "linkDecision", uuid: string): void
}>()

const searchResultFeedbackString = ref<string>(
  "Noch keine Suchparameter eingegeben"
)

watch(props, () => {
  if (!props.searchResults) return
  if (props.searchResults.length > 0) {
    searchResultFeedbackString.value = `Suche hat ${props.searchResults.length} Treffer ergeben`
  } else {
    searchResultFeedbackString.value = "Suche hat keine Treffer ergeben"
  }
})
</script>

<script lang="ts">
export type SearchResults = {
  decision: ProceedingDecision
  isLinked: boolean
}[]
</script>

<template>
  <div>
    <span class="label-02-bold">{{ searchResultFeedbackString }}</span>
    <div class="table">
      <div
        v-for="searchResult in searchResults"
        :key="searchResult.decision.uuid"
        class="mb-24 mt-12 table-row"
      >
        <div class="table-cell">
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
  </div>
</template>
