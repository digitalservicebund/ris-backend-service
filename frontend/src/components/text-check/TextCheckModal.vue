<script lang="ts" setup>
import IgnoredWordHandler from "@/components/text-check/IgnoredWordHandler.vue"
import ReplacementBar from "@/components/text-check/ReplacementBar.vue"

import { Match, Replacement } from "@/types/textCheck"

const props = defineProps<{
  match: Match
}>()

const emit = defineEmits<{
  "suggestion:update": [value: string]
  "suggestion:ignore": [void]
}>()

function acceptSuggestion(replacement: string) {
  emit("suggestion:update", replacement)
}

function ignoreSuggestion() {
  emit("suggestion:ignore")
}

function getValues(replacements: Replacement[]) {
  return replacements.flatMap((replacement) => replacement.value)
}
</script>

<template>
  <div
    class="flex min-w-[432px] flex-col flex-wrap items-start justify-start gap-16 border-2 border-solid border-blue-800 bg-white p-24"
    data-testid="text-check-modal"
  >
    <div class="flex flex-row gap-8">
      <span class="font-bold" data-testid="text-check-modal-word">
        {{ match.word }}
      </span>
    </div>

    <IgnoredWordHandler
      adding-to-dictionary-enabled
      :match="match"
      @ignore-text-check-word:add="ignoreSuggestion"
    />

    <p>{{ match.shortMessage || match.message }}</p>

    <ReplacementBar
      v-if="match.ignoredTextCheckWords"
      replacement-mode="single"
      :replacements="getValues(match.replacements)"
      @suggestion:ignore="ignoreSuggestion"
      @suggestion:update="acceptSuggestion"
    />
  </div>
</template>
