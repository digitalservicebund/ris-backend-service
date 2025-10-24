<script lang="ts" setup>
import { Editor } from "@tiptap/vue-3"
import { computed } from "vue"
import IgnoredWordHandler from "@/components/text-check/IgnoredWordHandler.vue"
import { Match } from "@/types/textCheck"

const props = defineProps<{
  match: Match
  // todo: get rid of editor
  editor: Editor
}>()

const emit = defineEmits<{
  // todo: do not emit offset
  "ignore-once:toggle": [offset: number]
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

const isMatchIgnoredLocally = computed(
  () => props.match?.isIgnoredOnce ?? false,
)

function ignoreOnceToggle(offset: number) {
  emit("ignore-once:toggle", offset)
}
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

    <IgnoredWordHandler
      :ignored-locally="isMatchIgnoredLocally"
      :match="match"
      @globally-ignored-word:add="addIgnoredWordGlobally"
      @globally-ignored-word:remove="removeGloballyIgnoredWord(match.word)"
      @ignore-once:toggle="ignoreOnceToggle(editor.state.selection.from)"
      @ignored-word:add="addIgnoredWord(match.word)"
      @ignored-word:remove="removeIgnoredWord(match.word)"
    />
  </div>
</template>
