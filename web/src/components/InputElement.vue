<script lang="ts" setup>
import { computed } from "vue"
import CheckboxInput from "@/components/CheckboxInput.vue"
import DateInput from "@/components/DateInput.vue"
import DropdownInput from "@/components/DropdownInput.vue"
import FileInput from "@/components/FileInput.vue"
import TextInput from "@/components/TextInput.vue"
import type { InputAttributes, ModelType } from "@/domain"
import { InputType, ValidationError } from "@/domain"

interface Props {
  type?: InputType
  modelValue?: ModelType
  attributes: InputAttributes
  validationError?: ValidationError
}

interface Emits {
  (event: "update:modelValue", value: ModelType): void
}

const props = withDefaults(defineProps<Props>(), {
  type: InputType.TEXT,
  modelValue: undefined,
  validationError: undefined,
})

const emit = defineEmits<Emits>()

const component = computed(() => {
  switch (props.type) {
    case InputType.TEXT:
      return TextInput
    case InputType.FILE:
      return FileInput
    case InputType.DROPDOWN:
      return DropdownInput
    case InputType.DATE:
      return DateInput
    case InputType.CHECKBOX:
      return CheckboxInput
    default:
      throw new Error(`Unknown input type: ${props.type}`)
  }
})

const value = computed({
  get: () => props.modelValue,
  set: (newValue) => emit("update:modelValue", newValue),
})
</script>

<template>
  <component
    :is="component"
    v-model="value"
    v-bind="attributes"
    :validation-error="validationError"
  />
</template>
