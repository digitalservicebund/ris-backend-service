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
        :aria-label="`${replacement} übernehmen`"
        button-type="primary"
        data-testid="suggestion-accept-button"
        :label="replacement"
        size="small"
        width="w-max"
        @click="acceptSuggestion(replacement)"
      />
    </div>
    <TextButton
      :aria-label="
        replacementMode === 'single'
          ? 'Vorschlag ignorieren'
          : 'Vorschläge ignorieren'
      "
      button-type="tertiary"
      data-testid="suggestion-ignore-button"
      disabled
      :label="replacementMode === 'single' ? 'Ignorieren ' : 'Alle ignorieren'"
      size="small"
      width="w-max"
      @click="ignoreSuggestion"
    />
  </div>
</template>
