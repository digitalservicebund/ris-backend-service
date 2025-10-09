<script lang="ts" setup>
import Button from "primevue/button"

defineProps<{
  replacements: string[]
}>()

const emit = defineEmits<{
  "suggestion:update": [value: string]
}>()

function acceptSuggestion(replacement: string) {
  emit("suggestion:update", replacement)
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
  </div>
</template>
