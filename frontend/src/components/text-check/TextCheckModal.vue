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
  "suggestion:update": [value: string]
  "ignored-word:add": [word: string]
}>()

function acceptSuggestion(replacement: string) {
  emit("suggestion:update", replacement)
}

function addIgnoredWord(word: string) {
  emit("ignored-word:add", word)
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
      <span class="font-bold" data-testid="text-check-modal-word">
        {{ match.word }}
      </span>
    </div>

    <IgnoredWordHandler
      v-if="isMatchIgnored"
      :match="match"
      @ignore-text-check-word:add="addIgnoredWord"
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
