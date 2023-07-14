<script lang="ts" setup>
import { computed, ref } from "vue"
import { useInputModel } from "@/shared/composables/useInputModel"

interface Props {
  id: string
  value?: string
  modelValue?: string
  ariaLabel: string
  placeholder?: string
  readOnly?: boolean
  fullHeight?: boolean
  hasError?: boolean
  size?: "regular" | "medium" | "small"
}

interface Emits {
  (event: "update:modelValue", value: string | undefined): void
  (event: "input", value: Event): void
  (event: "enter-released"): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const inputRef = ref<HTMLInputElement | null>()

const { inputValue, emitInputEvent } = useInputModel<string, Props, Emits>(
  props,
  emit,
)

const conditionalClasses = computed(() => ({
  "has-error placeholder-black": props.hasError,
  "h-full": props.fullHeight,
  "ds-input-medium": props.size === "medium",
  "ds-input-small": props.size === "small",
}))

const tabindex = computed(() => (props.readOnly ? -1 : 0))

function focusInput() {
  inputRef.value?.focus()
}

defineExpose({ focusInput })
</script>

<template>
  <input
    :id="id"
    ref="inputRef"
    v-model="inputValue"
    :aria-label="ariaLabel"
    class="ds-input"
    :class="conditionalClasses"
    :placeholder="placeholder"
    :readonly="readOnly"
    :tabindex="tabindex"
    type="text"
    @input="emitInputEvent"
    @keyup.enter="emit('enter-released')"
  />
</template>
