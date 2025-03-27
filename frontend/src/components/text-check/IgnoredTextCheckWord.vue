<script setup lang="ts">
import languageToolService from "@/services/textCheckService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { IgnoredTextCheckWord, Match } from "@/types/textCheck"

const props = defineProps<{
  match: Match
}>()

const emit = defineEmits<{
  "ignoreTextCheckWord:add": [void]
}>()

const store = useDocumentUnitStore()

async function addWordToDocOffice() {
  if (!store.documentUnit?.coreData.documentationOffice) {
    console.error("Documentation office does not exist")
    return
  }

  const newIgnoredTextCheckWord: IgnoredTextCheckWord = {
    word: props.match.word,
    documentationOffice: store.documentUnit.coreData.documentationOffice,
  }

  try {
    await languageToolService.addIgnoredWordForDocumentationOffice(
      newIgnoredTextCheckWord,
    )
    emit("ignoreTextCheckWord:add")
  } catch (error) {
    console.error("Error adding ignored word:", error)
  }
}
</script>
<template>
  <div v-if="match.rule.issueType == 'misspelling'">
    <button class="ds-link-01-bold" @click="addWordToDocOffice">
      Zum globalen Wörterbuch hinzufügen
    </button>
  </div>
</template>
