<template>
  <input
    :id="id"
    v-model="inputValue"
    class="input"
    type="text"
    :aria-label="ariaLabel"
    @input="emitInputEvent"
  />
</template>

<script lang="ts" setup>
import { useInputModel } from "@/composables/useInputModel"

interface Props {
  id: string
  value?: string
  modelValue?: string
  ariaLabel: string
}

interface Emits {
  (event: "update:modelValue", value: string | undefined): void
  (event: "input", value: Event): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const { inputValue, emitInputEvent } = useInputModel<string, Props, Emits>(
  props,
  emit
)
</script>

<style lang="scss" scoped>
.input {
  width: 100%;
  padding: 17px 24px;
  outline: 2px solid $text-tertiary;
}
</style>
