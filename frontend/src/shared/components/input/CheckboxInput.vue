<script lang="ts" setup>
import { computed } from "vue"
import { ValidationError } from "@/shared/components/input/types"
import { useInputModel } from "@/shared/composables/useInputModel"

interface Props {
  id: string
  value?: boolean
  modelValue?: boolean
  ariaLabel: string
  validationError?: ValidationError
  size?: "small" | "regular"
  disabled?: boolean
}

interface Emits {
  (event: "update:modelValue", value: boolean | undefined): void
  (event: "input", value: Event): void
}

const props = withDefaults(defineProps<Props>(), {
  value: false,
  modelValue: false,
  validationError: undefined,
  size: "regular",
})
const emit = defineEmits<Emits>()

const { inputValue, emitInputEvent } = useInputModel<boolean, Props, Emits>(
  props,
  emit
)

const isInvalid = computed(() => props.validationError !== undefined)
</script>

<template>
  <input
    :id="id"
    v-model="inputValue"
    :aria-label="ariaLabel"
    class="ds-checkbox"
    :class="{ 'has-error': isInvalid, 'ds-checkbox-small': size === 'small' }"
    :disabled="disabled"
    type="checkbox"
    @input="emitInputEvent"
  />
  <label :for="id"></label>
</template>
