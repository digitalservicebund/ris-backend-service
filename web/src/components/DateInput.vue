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
  isInFuture?: boolean
}

interface Emits {
  (event: "update:modelValue", value: string | undefined): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const inputValue = ref<string>()

watch(props, () => (inputValue.value = props.modelValue ?? props.value), {
  immediate: true,
})

watch(inputValue, (value) => {
  if (!hasError.value) emit("update:modelValue", value)
})

const isInFuture = computed(() => {
  if (inputValue.value) {
    const date = new Date(inputValue.value)
    const today = new Date()
    return date > today
  } else return true
})

const hasError = computed(
  () =>
    props.hasError ||
    props.isInFuture ||
    isInFuture.value ||
    inputValue.value == ""
)

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
