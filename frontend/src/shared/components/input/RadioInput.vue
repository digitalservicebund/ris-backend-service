<script lang="ts" setup>
import { computed } from "vue"
import { ValidationError } from "./types"

interface Props {
  modelValue: string
  validationError?: ValidationError
  size?: "large" | "medium" | "small"
}

const props = withDefaults(defineProps<Props>(), {
  validationError: undefined,
  size: "large",
})

const emit = defineEmits<{
  "update:modelValue": [value: string]
}>()

const localModelValue = computed({
  get: () => props.modelValue,
  set: (value) => emit("update:modelValue", value),
})
</script>

<template>
  <!-- Label should come from the surrounding context, e.g. InputField component -->
  <!-- eslint-disable vuejs-accessibility/form-control-has-label -->
  <input
    v-model="localModelValue"
    class="ds-radio focus:!shadow-[inset_0_0_0_0.25rem] focus:!shadow-blue-800"
    :class="{
      'has-error': !!props.validationError,
      'mr-12': props.size === 'large',
      'ds-radio-small mr-8': props.size === 'medium',
      'ds-radio-mini mr-6': props.size === 'small',
    }"
    type="radio"
  />
</template>
