<script lang="ts" setup>
import TextButton from "@/components/input/TextButton.vue"

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
      <TextButton
        aria-label="Vorschlag übernehmen"
        button-type="primary"
        :label="replacement"
        size="small"
        width="w-max"
        @click="acceptSuggestion(replacement)"
      />
    </div>
    <TextButton
      aria-label="Vorschlag ignorieren"
      button-type="tertiary"
      disabled
      :label="replacementMode === 'single' ? 'Ignorieren ' : 'Alle Ignorieren'"
      size="small"
      width="w-max"
      @click="ignoreSuggestion"
    />
  </div>
</template>
