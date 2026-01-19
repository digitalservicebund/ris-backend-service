<script setup lang="ts">
import Button from "primevue/button"
import { ref, computed } from "vue"
import IconBaselineContentCopy from "~icons/ic/baseline-content-copy"
import IconCheckRounded from "~icons/material-symbols/check-rounded"

const props = withDefaults(
  defineProps<{
    text: string
    value?: string
    name?: string
  }>(),
  {
    value: undefined,
    name: "Wert",
  },
)

const copySuccess = ref(false)

const tooltipText = computed(() =>
  copySuccess.value ? "Kopiert!" : "Kopieren",
)

async function copy() {
  try {
    await navigator.clipboard.writeText(props.value ?? props.text)
    copySuccess.value = true
    setTimeout(() => {
      copySuccess.value = false
    }, 1000)
  } catch (err) {
    console.error(err)
  }
}
</script>

<template>
  <div class="inline-flex items-center gap-8">
    <span>{{ text }}</span>

    <Button
      v-tooltip.bottom="{
        value: tooltipText,
      }"
      :aria-label="`${name} in die Zwischenablage kopieren`"
      class="!p-1"
      size="small"
      text
      @click="copy"
    >
      <template #icon>
        <IconBaselineContentCopy v-if="!copySuccess" />
        <IconCheckRounded v-else />
      </template>
    </Button>
  </div>
</template>
