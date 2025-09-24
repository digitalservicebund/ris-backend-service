<script lang="ts" setup>
import { computed, nextTick, ref, watch, watchEffect } from "vue"
import { ValidationError } from "@/components/input/types"

type Props = {
  id: string
  modelValue: string
  ariaLabel: string
  placeholder?: string
  readOnly?: boolean
  autosize?: boolean
  resize?: "none" | "both" | "horizontal" | "vertical"
  rows?: number
  size?: "regular" | "medium" | "small"
  hasError?: boolean
  customClasses?: string
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: "",
  validationError: undefined,
  readOnly: false,
  autosize: false,
  resize: "none",
  rows: 2,
  size: "regular",
  customClasses: "",
})

const emit = defineEmits<{
  "update:modelValue": [value: string]
  "update:validationError": [value: ValidationError | undefined]
}>()

const localValue = computed({
  get() {
    return props.modelValue
  },
  set(value: string) {
    emit("update:modelValue", value)
  },
})

/* -------------------------------------------------- *
 * Autosizing                                         *
 * -------------------------------------------------- */

const textareaRef = ref<HTMLTextAreaElement | null>(null)

const height = ref("auto")

async function determineTextareaHeight() {
  if (!props.autosize || !textareaRef.value) return

  // We first need to reset the height to auto, so that the scrollHeight
  // is not limited by the current height. Then wait for the next tick
  // so that the textarea has time to resize.
  height.value = "auto"
  await nextTick()

  const { borderTopWidth, borderBottomWidth } = getComputedStyle(
    textareaRef.value as Element,
  )

  const borderTop = Number.parseInt(borderTopWidth)
  const borderBottom = Number.parseInt(borderBottomWidth)

  const calculatedHeight =
    textareaRef.value.scrollHeight + borderTop + borderBottom
  if (calculatedHeight > 0) {
    height.value = `${calculatedHeight}px`
  }
}

watchEffect(() => {
  determineTextareaHeight().catch(() => {
    // left blank intentionally
  })
})

watch(localValue, async () => {
  await determineTextareaHeight()
})

/* -------------------------------------------------- *
 * Public interface                                   *
 * -------------------------------------------------- */

function focus() {
  textareaRef.value?.focus()
}

defineExpose({ focus })
</script>

<template>
  <textarea
    :id="id"
    ref="textareaRef"
    v-model="localValue"
    :aria-label="ariaLabel"
    class="shadow-blue focus:shadow-focus hover:shadow-hover h-unset py-12 focus:outline-none"
    :class="{
      'has-error': hasError,
      'px-16': size === 'small',
      'px-20': size === 'medium',
      'px-24': size === 'regular',
      [customClasses]: true,
      [$style.textarea]: true,
    }"
    :placeholder="placeholder"
    :readonly="readOnly"
    :rows="rows"
    :tabindex="readOnly ? -1 : ($attrs.tabindex as number)"
    @input="$emit('update:validationError', undefined)"
    @keypress.enter.stop="() => {}"
  ></textarea>
  <!-- No-op keypress handler for preventing other enter-based event handlers such as
  submitting data from firing when users are trying to insert newlines  -->
</template>

<style module>
.textarea {
  height: v-bind(height);
  resize: v-bind(resize);
}
</style>
