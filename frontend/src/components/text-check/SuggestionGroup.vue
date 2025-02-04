<script setup lang="ts">
import IconBadge from "@/components/IconBadge.vue"
import ReplacementBar from "@/components/text-check/ReplacementBar.vue"
import { Suggestion } from "@/types/languagetool"

defineProps<{
  suggestion: Suggestion
  isSelected?: boolean
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

function getAllReplacementValues(suggestion: Suggestion) {
  return [
    ...new Set(
      suggestion.matches
        .flatMap((item) => item.replacements)
        .map((replacement) => replacement.value),
    ),
  ]
}
</script>

<template>
  <div
    class="flex flex-col gap-4 bg-blue-100 p-24"
    :class="[isSelected ? 'border-4 border-blue-900' : '']"
  >
    <div class="flex flex-row gap-8">
      <div class="ds-label-01-bold">
        {{ suggestion.word }}
      </div>
      <span v-if="suggestion.matches.length > 1">
        <IconBadge
          background-color="bg-red-300"
          color="text-red-900"
          :label="suggestion.matches.length.toString()"
          :margin-x="4"
        />
      </span>
    </div>

    <div>
      <span class="ds-link-01-bold"> Zum globalen Wörterbuch hinzufügen </span>
    </div>
    <div>
      {{ suggestion.matches[0].message }}
    </div>

    <ReplacementBar
      :replacement-mode="suggestion.matches.length > 1 ? 'multiple' : 'single'"
      :replacements="getAllReplacementValues(suggestion)"
      @suggestion:ignore="ignoreSuggestion"
      @suggestion:update="acceptSuggestion"
    />
  </div>
</template>
