<script setup lang="ts">
import Button from "primevue/button"
import { computed } from "vue"
import { Match } from "@/types/textCheck"
import IconAutoStoriesOffVariant from "~icons/material-symbols/auto-stories-off-outline"
import IconAutoStoriesVariant from "~icons/material-symbols/auto-stories-outline"
import IconDescription from "~icons/material-symbols/description-outline"
import IconSpaceBar from "~icons/material-symbols/space-bar"
import IconSpellCheck from "~icons/material-symbols/spellcheck"

const props = defineProps<{
  match: Match
  ignoredLocally: boolean
}>()

const emit = defineEmits<{
  "ignored-word:add": [void]
  "ignored-word:remove": [void]
  "globally-ignored-word:remove": [void]
  "globally-ignored-word:add": [void]
  "ignore-once:toggle": [void]
}>()

// Hier ignorieren
function ignoreOnceToggle() {
  emit("ignore-once:toggle")
}

// In Dokeinheit ignorieren
function addIgnoredWord() {
  if (props.ignoredLocally) {
    emit("ignore-once:toggle")
  }
  emit("ignored-word:add")
}

async function removeWord() {
  emit("ignored-word:remove")
}

// Zum Wörterbuch hin zufügen
function addIgnoredWordGlobally() {
  if (props.ignoredLocally) {
    emit("ignore-once:toggle")
  }
  emit("ignored-word:remove")
  emit("globally-ignored-word:add")
}

async function removeWordGlobally() {
  emit("globally-ignored-word:remove")
}

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
  <div class="flex flex-col gap-16" data-testid="ignored-word-handler">
    <div v-if="matchIsIgnoredJDV">Von jDV ignoriert</div>

    <template v-else>
      <template v-if="matchIsIgnoredGlobally">
        <div>im Wörterbuch / für alle Dokstellen ignoriert</div>

        <Button
          aria-label="Wort aus Wörterbuch entfernen"
          class="self-start"
          data-testid="ignored-word-global-remove-button"
          label="Aus Wörterbuch entfernen"
          size="small"
          text
          @click="removeWordGlobally"
        >
          <template #icon>
            <IconAutoStoriesOffVariant />
          </template>
        </Button>
      </template>

      <template v-else-if="matchIsIgnoredInDocument">
        <div>in dieser Dokumentationseinheit ignoriert</div>

        <Button
          aria-label="Nicht in Dokumentationseinheit ignorieren"
          class="self-start"
          data-testid="ignored-word-remove-button"
          label="Nicht in Dokumentationseinheit ignorieren"
          severity="secondary"
          size="small"
          @click="removeWord"
        >
          <template #icon>
            <IconSpellCheck />
          </template>
        </Button>

        <Button
          aria-label="Zum Wörterbuch hinzufügen"
          class="self-start"
          data-testid="ignored-word-global-add-button"
          label="Zum Wörterbuch hinzufügen"
          size="small"
          text
          @click="addIgnoredWordGlobally"
        >
          <template #icon>
            <IconAutoStoriesVariant />
          </template>
        </Button>
      </template>

      <template v-else-if="ignoredLocally">
        <div>an dieser Stelle ignoriert</div>

        <Button
          aria-label="Hier nicht ignorieren"
          class="self-start"
          data-testid="unignore-once-button"
          label="Hier nicht ignorieren"
          severity="secondary"
          size="small"
          @click="ignoreOnceToggle"
        >
          <template #icon>
            <IconSpellCheck />
          </template>
        </Button>

        <Button
          aria-label="In Dokumentationseinheit ignorieren"
          class="self-start"
          data-testid="ignored-word-add-button"
          label="In Dokumentationseinheit ignorieren"
          size="small"
          text
          @click="addIgnoredWord"
        >
          <template #icon>
            <IconDescription />
          </template>
        </Button>

        <Button
          aria-label="Zum Wörterbuch hinzufügen"
          class="self-start"
          data-testid="ignored-word-global-add-button"
          label="Zum Wörterbuch hinzufügen"
          size="small"
          text
          @click="addIgnoredWordGlobally"
        >
          <template #icon>
            <IconAutoStoriesVariant />
          </template>
        </Button>
      </template>

      <template v-else>
        <div>{{ match.shortMessage }}</div>

        <Button
          aria-label="Hier ignorieren"
          class="self-start"
          data-testid="ignore-once-button"
          label="Hier ignorieren"
          severity="secondary"
          size="small"
          @click="ignoreOnceToggle"
        >
          <template #icon>
            <IconSpaceBar />
          </template>
        </Button>

        <Button
          aria-label="In Dokumentationseinheit ignorieren"
          class="self-start"
          data-testid="ignored-word-add-button"
          label="In Dokumentationseinheit ignorieren"
          severity="secondary"
          size="small"
          @click="addIgnoredWord"
        >
          <template #icon>
            <IconDescription />
          </template>
        </Button>

        <Button
          aria-label="Zum Wörterbuch hinzufügen"
          class="self-start"
          data-testid="ignored-word-global-add-button"
          label="Zum Wörterbuch hinzufügen"
          size="small"
          text
          @click="addIgnoredWordGlobally"
        >
          <template #icon>
            <IconAutoStoriesVariant />
          </template>
        </Button>
      </template>
    </template>
  </div>
</template>
