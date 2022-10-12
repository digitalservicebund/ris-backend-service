<script lang="ts" setup>
import { placeholder } from "@babel/types"
import { computed, onMounted, ref, watch } from "vue"

interface Props {
  id: string
  value?: Date
  modelValue?: Date
  ariaLabel: string
  placeholder?: string
  hasError?: boolean
}

interface Emits {
  (event: "update:modelValue", value: Date | undefined): void
  (event: "input", value: Event): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const inputValue = ref<Date>()

watch(props, () => (inputValue.value = props.modelValue ?? props.value), {
  immediate: true,
})

watch(inputValue, () => {
  emit("update:modelValue", inputValue.value)
})

function emitInputEvent(event: Event): void {
  emit("input", event)
}

const conditionalClasses = computed(() => ({
  input__error: props.hasError,
}))

onMounted(() => {
  inputValue.value = new Date()
  console.log(props.modelValue, props.value)
})
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
