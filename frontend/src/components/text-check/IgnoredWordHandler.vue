<script setup lang="ts">
import { NeurisTextCheckService } from "@/editor/commands/textCheckCommands"
import languageToolService from "@/services/textCheckService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { IgnoredTextCheckWord, Match } from "@/types/textCheck"

const props = defineProps<{
  match: Match
  addingToDictionaryEnabled?: boolean
}>()

const emit = defineEmits<{
  "ignoreTextCheckWord:add": [void]
}>()

const store = useDocumentUnitStore()

async function addWordToDocOffice() {
  if (!store.documentUnit?.uuid) {
    console.error("Documentation unit does not exist")
    return
  }

  const newIgnoredTextCheckWord: IgnoredTextCheckWord = {
    word: props.match.word,
    type: "documentation_office",
  }

  try {
    await languageToolService.addIgnoredWordForDocumentationOffice(
      store.documentUnit.uuid,
      newIgnoredTextCheckWord,
    )
    emit("ignoreTextCheckWord:add")
  } catch (error) {
    console.error("Error adding ignored word:", error)
  }
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
