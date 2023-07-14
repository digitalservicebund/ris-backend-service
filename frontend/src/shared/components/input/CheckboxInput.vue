<script lang="ts" setup>
import { computed } from "vue"
import { ValidationError } from "@/shared/components/input/types"
import { useInputModel } from "@/shared/composables/useInputModel"

interface Props {
  modelValue?: boolean
  validationError?: ValidationError
  size?: "small" | "regular"
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
  emit,
)

const isInvalid = computed(() => props.validationError !== undefined)
</script>

<template>
  <!-- Label should come from the surrounding context, e.g. InputField component -->
  <!-- eslint-disable vuejs-accessibility/form-control-has-label -->
  <input
    v-model="inputValue"
    class="ds-checkbox mr-4"
    :class="{ 'has-error': isInvalid, 'ds-checkbox-small': size === 'small' }"
    type="checkbox"
    @input="emitInputEvent"
  />
</template>
