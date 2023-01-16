<script lang="ts" setup>
import { computed } from "vue"
import CheckboxInput from "@/components/CheckboxInput.vue"
import ChipsDateInput from "@/components/ChipsDateInput.vue"
import ChipsInput from "@/components/ChipsInput.vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import DateInput from "@/components/DateInput.vue"
import DropdownInput from "@/components/DropdownInput.vue"
import FileInput from "@/components/FileInput.vue"
import NestedInput from "@/components/NestedInput.vue"
import TextInput from "@/components/TextInput.vue"
import type { InputAttributes, ModelType } from "@/domain"
import { InputType, ValidationError } from "@/domain"

interface Props {
  id: string
  type?: InputType
  modelValue: ModelType
  attributes: InputAttributes
  validationError?: ValidationError
}

interface Emits {
  (event: "update:modelValue", value: ModelType): void
  (event: "validationError", value: string): void
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
    case InputType.CHIPS:
      return ChipsInput
    case InputType.DATECHIPS:
      return ChipsDateInput
    case InputType.NESTED:
      return NestedInput
    case InputType.COMBOBOX:
      return ComboboxInput
    default:
      throw new Error(`Unknown input type: ${props.type}`)
  }
})

const value = computed({
  get: () => props.modelValue,
  set: (newValue) => emit("update:modelValue", newValue),
})

function onValidationError(validationError: string) {
  console.log("Validation Error in Input Element", validationError)
  emit("validationError", validationError)
}
</script>

<template>
  <component
    :is="component"
    :id="id"
    v-model="value"
    v-bind="attributes"
    :validation-error="validationError"
    @validation-error="onValidationError"
  />
</template>
