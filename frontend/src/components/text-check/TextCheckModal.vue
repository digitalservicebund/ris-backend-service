<script lang="ts" setup>
import { computed } from "vue"
import IgnoredWordHandler from "@/components/text-check/IgnoredWordHandler.vue"
import ReplacementBar from "@/components/text-check/ReplacementBar.vue"

import { NeurisTextCheckService } from "@/editor/commands/textCheckCommands"
import { Match, Replacement } from "@/types/textCheck"

const props = defineProps<{
  match: Match
}>()

const emit = defineEmits<{
  "word:remove": [value: string]
  "word:add": [word: string]
  "word:replace": [value: string]
}>()

function acceptSuggestion(replacement: string) {
  emit("word:replace", replacement)
}

function addIgnoredWord(word: string) {
  emit("word:add", word)
}

function removeIgnoredWord(word: string) {
  emit("word:remove", word)
}

function getValues(replacements: Replacement[]) {
  return replacements.flatMap((replacement) => replacement.value)
}

const isMatchIgnored = computed(() => {
  return NeurisTextCheckService.isMatchIgnored(props.match)
})
</script>

<template>
  <div
    class="flex min-w-[432px] flex-col flex-wrap items-start justify-start gap-16 border-2 border-solid border-blue-800 bg-white p-24"
    data-testid="text-check-modal"
  >
    <div class="flex flex-row gap-8">
      <span class="ris-body1-regular" data-testid="text-check-modal-word">
        {{ match.word }}
      </span>
    </div>

    <IgnoredWordHandler
      v-if="isMatchIgnored"
      :match="match"
      @ignored-word:remove="removeIgnoredWord(match.word)"
    />

    <p v-if="!isMatchIgnored">{{ match.shortMessage || match.message }}</p>

    <ReplacementBar
      v-if="!isMatchIgnored"
      replacement-mode="single"
      :replacements="getValues(match.replacements)"
      @ignored-word:add="addIgnoredWord(match.word)"
      @suggestion:update="acceptSuggestion"
    />
  </div>
</template>
