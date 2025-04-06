<script setup lang="ts">
import Button from "primevue/button"
import { Match } from "@/types/textCheck"

const props = defineProps<{
  match: Match
  addingToDictionaryEnabled?: boolean
}>()

const emit = defineEmits<{
  "ignored-word:remove": [string]
}>()

async function removeWord() {
  emit("ignored-word:remove", props.match.word)
}
</script>

<template>
  <div data-testid="ignored-word-handler">
    <div
      v-if="
        match.ignoredTextCheckWords?.some(
          (ignoredWord) => ignoredWord.type === 'global',
        )
      "
    >
      Von jDV ignoriert
    </div>

    <Button
      v-if="
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
