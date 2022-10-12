<script lang="ts" setup>
import { placeholder } from "@babel/types"
import { computed, ref, watch } from "vue"

interface Props {
  id: string
  value?: string
  modelValue?: string
  ariaLabel: string
  placeholder?: string
  hasError?: boolean
}

interface Emits {
  (event: "update:modelValue", value: string | undefined): void
  (event: "input", value: Event): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const inputValue = ref<string>()
const hasError = ref(false)

watch(props, () => (inputValue.value = props.modelValue ?? props.value), {
  immediate: true,
})

function emitInputEvent(event: Event): void {
  hasError.value = false
  emit("input", event)
}

function handleBlur(): void {
  if (inputValue.value) {
    if (inputValue.value != "" && !isInFuture(inputValue.value))
      emit("update:modelValue", inputValue.value)
    else hasError.value = true
  }
}

function isInFuture(value: string) {
  const date = new Date(value)
  const today = new Date()
  console.log(date > today)
  return date > today
}

const conditionalClasses = computed(() => ({
  input__error: props.hasError || hasError.value,
}))
</script>

<template>
  <input
    :id="id"
    v-model="inputValue"
    :aria-label="ariaLabel"
    class="bg-white input"
    :class="conditionalClasses"
    :placeholder="placeholder"
    type="date"
    @blur="handleBlur"
    @input="emitInputEvent"
  />
</template>

<style lang="scss" scoped>
.input {
  width: 100%;
  padding: 17px 24px;
  @apply border-2 border-solid border-blue-800 uppercase;

  &:focus {
    outline: none;
  }

  &:autofill {
    @apply shadow-white text-inherit;
  }

  &:autofill:focus {
    @apply shadow-white text-inherit;
  }

  &__error {
    width: 100%;
    padding: 17px 24px;
    @apply border-red-800 bg-red-200;

    &:autofill {
      @apply shadow-error text-inherit;
    }

    &:autofill:focus {
      @apply shadow-error text-inherit;
    }
  }
}
</style>
