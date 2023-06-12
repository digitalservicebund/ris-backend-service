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

interface Emits {
  (event: "update:modelValue", value: string): void
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: "",
  validationError: undefined,
  readOnly: false,
  autosize: false,
  resize: "none",
  rows: 1,
})

const emit = defineEmits<Emits>()

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
    textareaRef.value
  )

  const borderTop = parseInt(borderTopWidth)
  const borderBottom = parseInt(borderBottomWidth)

  height.value = `${
    textareaRef.value.scrollHeight + borderTop + borderBottom
  }px`
}

watchEffect(() => {
  datermineTextareHeight()
})

watch(localValue, () => {
  datermineTextareHeight()
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
    class="-outline-offset-4 autofill:focus:shadow-white autofill:focus:text-inherit autofill:shadow-white autofill:text-inherit bg-white block border-2 border-blue-800 focus:outline hover:outline input outline-2 outline-blue-800 px-16 py-12 read-only:border-none read-only:hover:outline-0 readonly:focus:outline-none w-full"
    :class="$style.textarea"
    :placeholder="placeholder"
    :readonly="readOnly"
    :rows="rows"
    :tabindex="readOnly ? -1 : ($attrs.tabindex as number)"
  ></textarea>
</template>

<style module>
.textarea {
  height: v-bind(height);
  resize: v-bind(resize);
}
</style>
