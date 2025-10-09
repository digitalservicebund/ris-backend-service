<script lang="ts" setup>
import { computed } from "vue"
import IgnoredWordHandler from "@/components/text-check/IgnoredWordHandler.vue"
import { Match } from "@/types/textCheck"

const props = defineProps<{
  match: Match
}>()

const emit = defineEmits<{
  "word:remove": [value: string]
  "word:add": [word: string]
  "globalWord:remove": [value: string]
  "globalWord:add": [word: string]
}>()

function addIgnoredWord(word: string) {
  emit("word:add", word)
}

function removeIgnoredWord(word: string) {
  emit("word:remove", word)
}

function addIgnoredWordGlobally() {
  emit("globalWord:add", props.match.word)
}

function removeGloballyIgnoredWord(word: string) {
  emit("globalWord:remove", word)
}

const isMatchIgnored = computed(() => {
  return (
    Array.isArray(props.match.ignoredTextCheckWords) &&
    props.match.ignoredTextCheckWords.length > 0
  )
})
</script>

<template>
  <div
    class="flex min-w-[432px] flex-col flex-wrap items-start justify-start gap-16 border-2 border-solid border-blue-800 bg-white p-24"
    data-testid="text-check-modal"
  >
    <div class="flex flex-row gap-8">
      <span class="ris-body1-bold" data-testid="text-check-modal-word">
        {{ match.word }}
      </span>
    </div>

    <p v-if="!isMatchIgnored">{{ match.shortMessage || match.message }}</p>

    <IgnoredWordHandler
      :match="match"
      @globally-ignored-word:add="addIgnoredWordGlobally"
      @globally-ignored-word:remove="removeGloballyIgnoredWord(match.word)"
      @ignored-word:add="addIgnoredWord(match.word)"
      @ignored-word:remove="removeIgnoredWord(match.word)"
    />
  </div>
</template>
