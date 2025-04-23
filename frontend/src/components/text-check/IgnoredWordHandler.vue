<script setup lang="ts">
import Button from "primevue/button"
import { computed } from "vue"
import { useFeatureToggle } from "@/composables/useFeatureToggle"
import { Match } from "@/types/textCheck"

const props = defineProps<{
  match: Match
}>()

const emit = defineEmits<{
  "ignored-word:remove": [string]
  "globally-ignored-word:remove": [string]
  "globally-ignored-word:add": [string]
}>()

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
    (ignoredWord) =>
      ignoredWord.type === "global" || ignoredWord.type === "global_jdv",
  )
})
</script>

<template>
  <div
    class="flex flex-grow flex-col gap-16"
    data-testid="ignored-word-handler"
  >
    <div
      v-if="
        match.ignoredTextCheckWords?.some(
          (ignoredWord) => ignoredWord.type === 'global_jdv',
        )
      "
    >
      Von jDV ignoriert
    </div>

    <Button
      v-else-if="
        textCheckGlobal &&
        match.ignoredTextCheckWords?.some(
          (ignoredWord) => ignoredWord.type === 'global',
        )
      "
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

    <div>
      <Button
        v-if="
          match.ignoredTextCheckWords?.some(
            (ignoredWord) => ignoredWord.type === 'documentation_unit',
          ) &&
          !match.ignoredTextCheckWords?.some(
            (ignoredWord) => ignoredWord.type === 'global_jdv',
          )
        "
        aria-label="Wort nicht ignorieren"
        button-type="tertiary"
        data-testid="ignored-word-remove-button"
        label="Nicht ignorieren"
        severity="secondary"
        size="small"
        @click="removeWord"
      />
    </div>
  </div>
</template>
