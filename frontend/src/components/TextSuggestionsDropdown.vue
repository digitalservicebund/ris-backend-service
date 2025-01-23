<script lang="ts" setup>
import TextButton from "@/components/input/TextButton.vue"
import { Match, Replacement } from "@/types/languagetool"

const props = defineProps<{
  match: Match
}>()

const emit = defineEmits<{
  "suggestion:update": [value: Replacement]
  "suggestion:ignore": [void]
}>()

function acceptSuggestion(replacement: Replacement) {
  emit("suggestion:update", replacement)
}

function ignoreSuggestion() {
  emit("suggestion:ignore")
}
</script>

<template>
  <div
    class="flex flex-col items-start justify-start border-2 border-solid border-blue-800 bg-white p-24"
  >
    <button
      class="ds-link-01-bold whitespace-nowrap leading-24 focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800"
      @click="ignoreSuggestion"
    >
      Ignorieren
    </button>

    <label>
      {{ match.message }}
    </label>
    <div
      v-for="(replacement, i) in match.replacements"
      :key="i + replacement.value"
      class="flex w-full flex-row flex-nowrap gap-24"
    >
      <TextButton
        aria-label="Vorschlag übernehmen"
        button-type="primary"
        :label="replacement.value"
        size="small"
        width="w-max"
        @click="acceptSuggestion(replacement)"
      >
      </TextButton>
    </div>
  </div>
</template>
