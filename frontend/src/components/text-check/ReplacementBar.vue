<script lang="ts" setup>
import Button from "primevue/button"

defineProps<{
  replacements: string[]
}>()

const emit = defineEmits<{
  "suggestion:update": [value: string]
  "ignored-word:add": [void]
}>()

function acceptSuggestion(replacement: string) {
  emit("suggestion:update", replacement)
}

function addIgnoredWord() {
  emit("ignored-word:add")
}
</script>

<template>
  <div class="flex w-full flex-row flex-wrap gap-16">
    <div v-for="(replacement, i) in replacements" :key="i + replacement">
      <Button
        :aria-label="`${replacement} übernehmen`"
        data-testid="suggestion-accept-button"
        :label="replacement"
        size="small"
        @click="acceptSuggestion(replacement)"
      ></Button>
    </div>
    <Button
      aria-label="Vorschlag ignorieren"
      data-testid="ignored-word-add-button"
      label="Ignorieren"
      severity="secondary"
      size="small"
      @click="addIgnoredWord"
    />
  </div>
</template>
