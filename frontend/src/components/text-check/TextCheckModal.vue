<script lang="ts" setup>
import { Editor } from "@tiptap/vue-3"
import { Selection } from "prosemirror-state"
import { computed, ComputedRef } from "vue"
import IgnoredWordHandler from "@/components/text-check/IgnoredWordHandler.vue"
import { IgnoreOnceTagName } from "@/editor/ignoreOnceMark"
import { Match } from "@/types/textCheck"

const props = defineProps<{
  match: Match
  selection: Selection
  editor: Editor
}>()

const emit = defineEmits<{
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

const isMatchIgnoredLocally: ComputedRef<boolean> = computed(() => {
  const cursorLocation = props.editor?.state?.selection?.from
  const activeNode = props.editor?.state?.doc?.nodeAt(cursorLocation)
  if (activeNode && activeNode.marks) {
    for (const mark of activeNode.marks) {
      if (mark.type.name === IgnoreOnceTagName) {
        return true
      }
    }
  }
  return false
})

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
      @ignore-once:toggle="ignoreOnceToggle(selection.from)"
      @ignored-word:add="addIgnoredWord(match.word)"
      @ignored-word:remove="removeIgnoredWord(match.word)"
    />
  </div>
</template>
