<script setup lang="ts">
import Button from "primevue/button"
import { ref, computed } from "vue"
import IconBaselineContentCopy from "~icons/ic/baseline-content-copy"
import IconCheckRounded from "~icons/material-symbols/check-rounded"

const props = withDefaults(
  defineProps<{
    /** Visible text. */
    text: string

    /**
     * Value that should be copied. If no value is provided, copying will
     * copy the `text` by default.
     */
    value?: string

    /**
     * Human-readable description of the value that should be copied. This
     * will be used to provide an accessible label for the control.
     *
     * @default "Wert"
     */
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
