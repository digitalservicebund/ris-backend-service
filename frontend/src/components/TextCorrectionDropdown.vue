<script lang="ts" setup>
import FlexContainer from "@/components/FlexContainer.vue"
import TextButton from "@/components/input/TextButton.vue"
import { Replacement } from "@/types/languagetool"

defineProps<{
  replacements: Replacement[]
  matchMessage?: string
}>()

const emit = defineEmits<{
  "suggestion:update": [value: Replacement]
  "suggestion:ignore": [void]
}>()

function acceptSuggestion(replacement: Replacement) {
  emit("suggestion:update", replacement)
}

function ignoreSuggestion() {
  emit("suggestion:ignore")
}
</script>

<template>
  <FlexContainer
    class="flex w-auto flex-col border-2 border-solid border-blue-800 bg-white"
  >
    <div v-for="(replacement, i) in replacements" :key="i + replacement.value">
      <TextButton
        aria-label="Vorschlag übernehmen"
        button-type="ghost"
        :label="replacement.value"
        width="w-full"
        @click="acceptSuggestion(replacement)"
      >
      </TextButton>
    </div>
    <TextButton
      aria-label="Vorschlag ignorieren"
      label="Ignorieren"
      width="w-full"
      @click="ignoreSuggestion"
    ></TextButton>
  </FlexContainer>
</template>
