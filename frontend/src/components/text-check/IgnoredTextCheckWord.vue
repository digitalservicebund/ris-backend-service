<script setup lang="ts">
import { IgnoredTextCheckWord, Match } from "@/types/textCheck"
import languageToolService from "@/services/textCheckService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const store = useDocumentUnitStore()
const props = defineProps<{
  match: Match
}>()

async function addWordToDocOffice() {
  const newIgnoredTextCheckWord: IgnoredTextCheckWord = {
    word: props.match.word,
    documentationOffice: store.documentUnit?.coreData.documentationOffice!,
  }
  const response =
    await languageToolService.addIgnoredWordForDocumentationOffice(
      newIgnoredTextCheckWord,
    )
}
</script>
<template>
  <div>
    <button class="ds-link-01-bold" @click="addWordToDocOffice">
      Zum globalen Wörterbuch hinzufügen
    </button>
  </div>
</template>
