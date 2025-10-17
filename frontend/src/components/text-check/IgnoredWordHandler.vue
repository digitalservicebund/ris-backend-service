<script setup lang="ts">
import Button from "primevue/button"
import { computed } from "vue"
import { useFeatureToggle } from "@/composables/useFeatureToggle"
import { Match } from "@/types/textCheck"
import IconAutoStoriesVariant from "~icons/material-symbols/auto-stories"
import IconAutoStoriesOffVariant from "~icons/material-symbols/auto-stories-off"
import IconDescription from "~icons/material-symbols/description"
import IconSpellCheck from "~icons/material-symbols/spellcheck"

const props = defineProps<{
  match: Match
}>()

const emit = defineEmits<{
  "ignored-word:add": [void]
  "ignored-word:remove": [void]
  "globally-ignored-word:remove": [void]
  "globally-ignored-word:add": [void]
}>()

function addIgnoredWord() {
  emit("ignored-word:add")
}

async function removeWord() {
  emit("ignored-word:remove")
}

function addIgnoredWordGlobally() {
  emit("ignored-word:remove")
  emit("globally-ignored-word:add")
}

async function removeWordGlobally() {
  emit("globally-ignored-word:remove")
}

const textCheckGlobal = useFeatureToggle("neuris.text-check-global")

const matchIsIgnoredGlobally = computed(() => {
  return props.match.ignoredTextCheckWords?.some(
    (ignoredWord) => ignoredWord.type === "global",
  )
})

const matchIsIgnoredJDV = computed(() => {
  return props.match.ignoredTextCheckWords?.some(
    (ignoredWord) => ignoredWord.type === "global_jdv",
  )
})

const matchIsIgnoredInDocument = computed(() => {
  return props.match.ignoredTextCheckWords?.some(
    (ignoredWord) => ignoredWord.type === "documentation_unit",
  )
})
</script>

<template>
  <div class="flex flex-grow flex-col" data-testid="ignored-word-handler">
    <div v-if="matchIsIgnoredJDV">Von jDV ignoriert</div>

    <div v-else class="flex flex-grow flex-col items-start gap-16">
      <Button
        v-if="!matchIsIgnoredGlobally && matchIsIgnoredInDocument"
        aria-label="Wort nicht ignorieren"
        data-testid="ignored-word-remove-button"
        label="Nicht in Dokeinheit ignorieren"
        size="small"
        text
        @click="removeWord"
      >
        <template #icon>
          <IconSpellCheck />
        </template>
      </Button>

      <Button
        v-else-if="!matchIsIgnoredGlobally && !matchIsIgnoredInDocument"
        aria-label="Wort ignorieren"
        data-testid="ignored-word-add-button"
        label="In Dokeinheit ignorieren"
        size="small"
        text
        @click="addIgnoredWord"
      >
        <template #icon>
          <IconDescription />
        </template>
      </Button>

      <!-- Todo: Remove the outer div, when removing feature toggle textCheckGlobal -->
      <div
        v-if="textCheckGlobal"
        class="flex flex-grow flex-col items-start gap-16"
      >
        <Button
          v-if="matchIsIgnoredGlobally"
          aria-label="Wort aus globalem Wörterbuch entfernen"
          data-testid="ignored-word-global-remove-button"
          label="Aus globalem Wörterbuch entfernen"
          size="small"
          text
          @click="removeWordGlobally"
        >
          <template #icon>
            <IconAutoStoriesOffVariant />
          </template>
        </Button>

        <Button
          v-else
          aria-label="Wort zu globalem Wörterbuch hinzufügen"
          label="Zum globalen Wörterbuch hinzufügen"
          size="small"
          text
          @click="addIgnoredWordGlobally"
        >
          <template #icon>
            <IconAutoStoriesVariant />
          </template>
        </Button>
      </div>
    </div>
  </div>
</template>
