<script setup lang="ts">
import { computed, ref } from "vue"
import IconBadge from "@/components/IconBadge.vue"
import MatchLinkingButton from "@/components/text-check/MatchLinkingButton.vue"
import MatchNavigator from "@/components/text-check/MatchNavigator.vue"
import ReplacementBar from "@/components/text-check/ReplacementBar.vue"
import { Replacement, Suggestion } from "@/types/languagetool"

const props = defineProps<{
  suggestion: Suggestion
  isSelected?: boolean
}>()

const emit = defineEmits<{
  "suggestion:update": [value: string]
  "suggestion:ignore": [void]
}>()

const currentIndex = ref(0)

const selectedMatch = computed(
  () => props.suggestion.matches[currentIndex.value] ?? undefined,
)

function acceptSuggestion(replacement: string) {
  emit("suggestion:update", replacement)
}

function ignoreSuggestion() {
  emit("suggestion:ignore")
}

function getValues(replacements: Replacement[]) {
  return replacements.flatMap((replacement) => replacement.value)
}

function updateCurrentIndex(index: number) {
  currentIndex.value = index
}
</script>

<template>
  <div class="flex flex-col gap-4 bg-blue-100 p-24">
    <div class="flex flex-row justify-between gap-8">
      <div class="flex flex-row items-center gap-8">
        <div class="ds-label-01-bold">
          {{ suggestion.word }}
        </div>
        <span v-if="suggestion.matches.length > 1">
          <IconBadge
            background-color="bg-red-300"
            color="text-red-900"
            :label="suggestion.matches.length.toString()"
          />
        </span>
        <MatchLinkingButton :category="selectedMatch.category" />
      </div>
      <MatchNavigator
        :current-index="currentIndex"
        :matches="suggestion.matches"
        @select="updateCurrentIndex"
      />
    </div>

    <div>
      <span class="ds-link-01-bold"> Zum globalen Wörterbuch hinzufügen </span>
    </div>
    <div>
      {{ selectedMatch.message }}
    </div>

    <ReplacementBar
      v-if="selectedMatch.replacements"
      :replacement-mode="suggestion.matches.length > 1 ? 'multiple' : 'single'"
      :replacements="getValues(selectedMatch.replacements)"
      @suggestion:ignore="ignoreSuggestion"
      @suggestion:update="acceptSuggestion"
    />
  </div>
</template>
