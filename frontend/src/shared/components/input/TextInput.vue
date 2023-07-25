<script lang="ts" setup>
import { computed, ref } from "vue"

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

const props = defineProps<Props>()

const emit = defineEmits<{
  "update:modelValue": [value: string | undefined]
  "enter-released": []
}>()

const inputRef = ref<HTMLInputElement | null>()

const localModelValue = computed({
  get: () => props.modelValue,
  set: (value) => emit("update:modelValue", value),
})

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
    v-model="localModelValue"
    :aria-label="ariaLabel"
    class="ds-input"
    :class="conditionalClasses"
    :placeholder="placeholder"
    :readonly="readOnly"
    :tabindex="tabindex"
    type="text"
    @keyup.enter="emit('enter-released')"
  />
</template>
