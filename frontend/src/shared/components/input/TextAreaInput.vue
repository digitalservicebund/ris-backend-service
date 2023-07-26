<script lang="ts" setup>
import { computed, nextTick, ref, watch, watchEffect } from "vue"
import { ValidationError } from "./types"

type Props = {
  id: string
  modelValue: string
  ariaLabel: string
  placeholder?: string
  validationError?: ValidationError
  readOnly?: boolean
  autosize?: boolean
  resize?: "none" | "both" | "horizontal" | "vertical"
  rows?: number
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: "",
  validationError: undefined,
  readOnly: false,
  autosize: false,
  resize: "none",
  rows: 2,
})

const emit = defineEmits<{
  "update:modelValue": [value: string]
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

async function datermineTextareHeight() {
  if (!props.autosize || !textareaRef.value) return

  // We first need to reset the height to auto, so that the scrollHeight
  // is not limited by the current height. Then wait for the next tick
  // so that the textarea has time to resize.
  height.value = "auto"
  await nextTick()

  const { borderTopWidth, borderBottomWidth } = getComputedStyle(
    textareaRef.value as Element,
  )

  const borderTop = parseInt(borderTopWidth)
  const borderBottom = parseInt(borderBottomWidth)

  height.value = `${
    textareaRef.value.scrollHeight + borderTop + borderBottom
  }px`
}

watchEffect(() => {
  datermineTextareHeight().catch(() => {
    // left blank intentionally
  })
})

watch(localValue, async () => {
  await datermineTextareHeight()
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
    class="input readonly:focus:outline-none block w-full overflow-hidden border-2 border-blue-800 bg-white px-20 py-12 outline-2 -outline-offset-4 outline-blue-800 autofill:text-inherit autofill:shadow-white read-only:border-none hover:outline read-only:hover:outline-0 focus:outline autofill:focus:text-inherit autofill:focus:shadow-white"
    :class="{ 'overflow-hidden': autosize, [$style.textarea]: true }"
    :placeholder="placeholder"
    :readonly="readOnly"
    :rows="rows"
    :tabindex="readOnly ? -1 : ($attrs.tabindex as number)"
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
