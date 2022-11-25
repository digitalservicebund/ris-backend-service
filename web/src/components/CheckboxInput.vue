<script lang="ts" setup>
import { useInputModel } from "@/composables/useInputModel"
import { ValidationError } from "@/domain"

interface Props {
  id: string
  value?: boolean
  modelValue?: boolean
  ariaLabel: string
  validationError?: ValidationError
}

interface Emits {
  (event: "update:modelValue", value: boolean | undefined): void
  (event: "input", value: Event): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const { inputValue, emitInputEvent } = useInputModel<boolean, Props, Emits>(
  props,
  emit
)
</script>
<template>
  <input
    :id="id"
    v-model="inputValue"
    :aria-label="ariaLabel"
    class="appearance-none border-2 border-blue-800 border-solid focus:outline-none h-40 w-40"
    type="checkbox"
    @input="emitInputEvent"
  />
</template>

<style lang="scss" scoped>
input {
  &:checked {
    appearance: auto;
  }
}
</style>
