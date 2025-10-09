<script setup lang="ts">
import Button from "primevue/button"
import { computed } from "vue"
import { useFeatureToggle } from "@/composables/useFeatureToggle"
import { Match } from "@/types/textCheck"
import IconDescription from "~icons/ic/baseline-description"
import IconSpellCheck from "~icons/material-symbols/spellcheck"
import IconBookOpenPageVariant from "~icons/mdi/book-open-page-variant"
import IconBookOpenVariant from "~icons/mdi/book-open-variant"

const props = defineProps<{
  match: Match
}>()

const emit = defineEmits<{
  "ignored-word:add": [void]
  "ignored-word:remove": [string]
  "globally-ignored-word:remove": [string]
  "globally-ignored-word:add": [string]
}>()

function addIgnoredWord() {
  emit("ignored-word:add")
}

async function removeWord() {
  emit("ignored-word:remove", props.match.word)
}

function addIgnoredWordGlobally() {
  emit("ignored-word:remove", props.match.word)
  emit("globally-ignored-word:add", props.match.word)
}

async function removeWordGlobally() {
  emit("globally-ignored-word:remove", props.match.word)
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

    <div class="flex flex-grow flex-col gap-4">
      <Button
        v-if="
          textCheckGlobal &&
          !matchIsIgnoredGlobally &&
          !matchIsIgnoredInDocument &&
          !matchIsIgnoredJDV
        "
        aria-label="Vorschlag ignorieren"
        data-testid="ignored-word-add-button"
        label="Ignorieren"
        severity="secondary"
        size="small"
        @click="addIgnoredWord"
      >
        <template #icon>
          <IconDescription />
        </template>
      </Button>

      <Button
        v-if="
          matchIsIgnoredInDocument &&
          !matchIsIgnoredJDV &&
          !matchIsIgnoredGlobally
        "
        aria-label="Wort nicht ignorieren"
        button-type="tertiary"
        data-testid="ignored-word-remove-button"
        icon="IconDescription"
        label="Nicht ignorieren"
        severity="secondary"
        size="small"
        @click="removeWord"
      >
        <template #icon>
          <IconSpellCheck />
        </template>
      </Button>

      <Button
        v-if="textCheckGlobal && matchIsIgnoredGlobally && !matchIsIgnoredJDV"
        aria-label="Wort aus globalem Wörterbuch entfernen"
        data-testid="ignored-word-global-remove-button"
        label="Aus globalem Wörterbuch entfernen"
        size="small"
        text
        @click="removeWordGlobally"
      >
        <template #icon>
          <IconBookOpenPageVariant />
        </template>
      </Button>

      <Button
        v-if="textCheckGlobal && !matchIsIgnoredGlobally"
        label="Zum globalen Wörterbuch hinzufügen"
        size="small"
        text
        @click="addIgnoredWordGlobally"
      >
        <template #icon>
          <IconBookOpenVariant />
        </template>
      </Button>
    </div>
  </div>
</template>
