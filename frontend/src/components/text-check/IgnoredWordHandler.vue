<script setup lang="ts">
import Button from "primevue/button"
import { computed } from "vue"
import { useFeatureToggle } from "@/composables/useFeatureToggle"
import { Match } from "@/types/textCheck"

const props = defineProps<{
  match: Match
}>()

const emit = defineEmits<{
  "ignore-once:toggle": [number, number]
  "ignored-word:remove": [string]
  "ignored-word:add": [string]
  "globally-ignored-word:remove": [string]
  "globally-ignored-word:add": [string]
}>()

function ignoreOnceToggle() {
  emit("ignore-once:toggle", props.match.offset, props.match.length)
}

function addIgnoredWord() {
  emit("ignored-word:add", props.match.word)
}

async function removeWord() {
  emit("ignored-word:remove", props.match.word)
}

function addIgnoredWordGlobally() {
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

// TODO: implement this
// const matchIsIgnoredLocally = computed(() => {})
</script>

<template>
  <div class="flex flex-grow flex-col" data-testid="ignored-word-handler">
    <div v-if="matchIsIgnoredJDV">Von jDV ignoriert</div>

    <div class="flex flex-col gap-8">
      <Button
        v-if="
          textCheckGlobal &&
          !matchIsIgnoredInDocument &&
          !matchIsIgnoredGlobally &&
          !matchIsIgnoredJDV
        "
        aria-label="Wort ignorieren"
        label="Ignorieren hier"
        @click="ignoreOnceToggle"
      />

      <Button
        v-if="
          textCheckGlobal &&
          !matchIsIgnoredInDocument &&
          !matchIsIgnoredGlobally
        "
        aria-label="Vorschlag ignorieren"
        data-testid="ignored-word-add-button"
        label="Ignorieren"
        severity="secondary"
        size="small"
        @click="addIgnoredWord"
      />

      <Button
        v-if="matchIsIgnoredInDocument && !matchIsIgnoredJDV"
        aria-label="Wort nicht ignorieren"
        button-type="tertiary"
        data-testid="ignored-word-remove-button"
        label="Nicht ignorieren"
        severity="secondary"
        size="small"
        @click="removeWord"
      />

      <Button
        v-else-if="textCheckGlobal && matchIsIgnoredGlobally"
        aria-label="Wort aus globalem Wörterbuch entfernen"
        data-testid="ignored-word-global-remove-button"
        label="Aus globalem Wörterbuch entfernen"
        size="small"
        text
        @click="removeWordGlobally"
      >
        Aus globalem Wörterbuch entfernen
      </Button>

      <Button
        v-if="textCheckGlobal && !matchIsIgnoredGlobally"
        size="small"
        text
        @click="addIgnoredWordGlobally"
        >Zum globalen Wörterbuch hinzufügen
      </Button>
    </div>
  </div>
</template>
