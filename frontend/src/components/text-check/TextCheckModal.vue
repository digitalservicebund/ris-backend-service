<script lang="ts" setup>
import ReplacementBar from "@/components/text-check/ReplacementBar.vue"

import { Match, Replacement } from "@/types/languagetool"

defineProps<{
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
  >
    <div class="flex flex-row gap-8">
      <label class="font-bold"> {{ match.word }} </label>
    </div>

    <p>{{ match.shortMessage || match.message }}</p>

    <ReplacementBar
      replacement-mode="single"
      :replacements="getValues(match.replacements)"
      @suggestion:ignore="ignoreSuggestion"
      @suggestion:update="acceptSuggestion"
    />
  </div>
</template>
