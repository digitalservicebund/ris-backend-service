<script setup lang="ts">
import { NeurisTextCheckService } from "@/editor/commands/textCheckCommands"
import { Match } from "@/types/textCheck"

const props = defineProps<{
  match: Match
  addingToDictionaryEnabled?: boolean
}>()

const emit = defineEmits<{
  "ignoreTextCheckWord:add": [string]
}>()

async function addWordToDocOffice() {
  emit("ignoreTextCheckWord:add", props.match.word)
}
</script>

<template>
  <div data-testid="ignored-word-handler">
    <div v-if="!NeurisTextCheckService.isMatchEditable(match)">
      von juris ignoriert
    </div>

    <div v-else-if="addingToDictionaryEnabled">
      <div
        v-if="
          match.rule.issueType == 'misspelling' &&
          !match.ignoredTextCheckWords?.length
        "
      >
        <button class="ds-link-01-bold" @click="addWordToDocOffice">
          Zum globalen Wörterbuch hinzufügen
        </button>
      </div>
    </div>
  </div>
</template>
