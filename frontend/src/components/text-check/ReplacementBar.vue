<script lang="ts" setup>
import Button from "primevue/button"

defineProps<{
  replacements: string[]
  replacementMode: "single" | "multiple"
}>()

const emit = defineEmits<{
  "suggestion:update": [value: string]
  "suggestion:ignore": [void]
}>()

function acceptSuggestion(replacement: string) {
  emit("suggestion:update", replacement)
}

function ignoreSuggestion() {
  emit("suggestion:ignore")
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
      :aria-label="
        replacementMode === 'single'
          ? 'Vorschlag ignorieren'
          : 'Vorschläge ignorieren'
      "
      data-testid="suggestion-ignore-button"
      :label="replacementMode === 'single' ? 'Ignorieren ' : 'Alle ignorieren'"
      severity="secondary"
      size="small"
      @click="ignoreSuggestion"
    ></Button>
  </div>
</template>
