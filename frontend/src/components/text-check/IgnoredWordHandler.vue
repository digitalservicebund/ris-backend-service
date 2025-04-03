<script setup lang="ts">
import TextButton from "@/components/input/TextButton.vue"
import { NeurisTextCheckService } from "@/editor/commands/textCheckCommands"
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
    <div v-if="!NeurisTextCheckService.isMatchEditable(match)">
      von juris ignoriert
    </div>
  </div>

  <TextButton
    aria-label="Wort nicht ignorieren"
    button-type="tertiary"
    data-testid="ignored-word-add-button"
    label="Nicht ignorieren"
    size="small"
    width="w-max"
    @click="removeWord"
  />
</template>
