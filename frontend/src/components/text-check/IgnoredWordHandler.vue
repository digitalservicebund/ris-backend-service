<script setup lang="ts">
import Button from "primevue/button"
import { useFeatureToggle } from "@/composables/useFeatureToggle"
import { Match } from "@/types/textCheck"

const props = defineProps<{
  match: Match
}>()

const emit = defineEmits<{
  "ignored-word:remove": [string]
  "globally-ignored-word:remove": [string]
}>()

async function removeWord() {
  emit("ignored-word:remove", props.match.word)
}

async function removeWordGlobally() {
  emit("globally-ignored-word:remove", props.match.word)
}

const textCheckGlobal = useFeatureToggle("neuris.text-check-global")
</script>

<template>
  <div data-testid="ignored-word-handler">
    <div
      v-if="
        match.ignoredTextCheckWords?.some(
          (ignoredWord) =>
            ignoredWord.type === 'global' && !ignoredWord.isEditable,
        )
      "
    >
      Von jDV ignoriert
    </div>

    <Button
      v-else-if="
        textCheckGlobal &&
        match.ignoredTextCheckWords?.some(
          (ignoredWord) =>
            ignoredWord.type === 'global' && ignoredWord.isEditable,
        )
      "
      aria-label="Wort aus globalem Wörterbuch entfernen"
      button-type="ghost"
      data-testid="ignored-word-global-remove-button"
      label="Aus globalem Wörterbuch entfernen"
      severity="secondary"
      size="small"
      @click="removeWordGlobally"
    >
      Aus globalem Wörterbuch entfernen
    </Button>

    <Button
      v-else-if="
        match.ignoredTextCheckWords?.some(
          (ignoredWord) => ignoredWord.type === 'documentation_unit',
        )
      "
      aria-label="Wort nicht ignorieren"
      button-type="tertiary"
      data-testid="ignored-word-remove-button"
      label="Nicht ignorieren"
      severity="secondary"
      size="small"
      @click="removeWord"
    />
  </div>
</template>
