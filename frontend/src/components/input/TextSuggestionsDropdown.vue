<script lang="ts" setup>
import TextButton from "@/components/input/TextButton.vue"
import { Match, Replacement } from "@/types/languagetool"

defineProps<{
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
    class="flex min-w-[432px] flex-col flex-wrap items-start justify-start gap-16 border-2 border-solid border-blue-800 bg-white p-24"
  >
    <div class="flex flex-row gap-8">
      <label class="font-bold"> {{ match.word }} </label>
    </div>

    <p>{{ match.shortMessage || match.message }}</p>
    <div class="flex w-full flex-row flex-wrap gap-16">
      <div
        v-for="(replacement, i) in match.replacements"
        :key="i + replacement.value"
      >
        <TextButton
          aria-label="Vorschlag übernehmen"
          button-type="primary"
          :label="replacement.value"
          size="small"
          width="w-max"
          @click="acceptSuggestion(replacement)"
        />
      </div>
      <TextButton
        aria-label="Vorschlag ignorieren"
        button-type="tertiary"
        label="Ignorieren"
        size="small"
        width="w-max"
        @click="ignoreSuggestion"
      />
    </div>
  </div>
</template>
