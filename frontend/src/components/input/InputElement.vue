<script lang="ts" setup>
import { computed, useAttrs } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import CheckboxInput from "@/components/input/CheckboxInput.vue"
import ChipsDateInput from "@/components/input/ChipsDateInput.vue"
import ChipsInput from "@/components/input/ChipsInput.vue"
import DateInput from "@/components/input/DateInput.vue"
import DropdownInput from "@/components/input/DropdownInput.vue"
import FileInput from "@/components/input/FileInput.vue"
import TextAreaInput from "@/components/input/TextAreaInput.vue"
import TextInput from "@/components/input/TextInput.vue"
import TimeInput from "@/components/input/TimeInput.vue"
import {
  InputAttributes,
  InputType,
  ModelType,
  ValidationError,
} from "@/components/input/types"
import YearInput from "@/components/input/YearInput.vue"

interface Props {
  id: string
  type?: InputType
  modelValue: ModelType
  attributes: InputAttributes
  validationError?: ValidationError
  disableError?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  type: InputType.TEXT,
  modelValue: undefined,
  validationError: undefined,
  disableError: false,
})

const emit = defineEmits<{
  "update:modelValue": [value: ModelType]
  "update:validationError": [value: ValidationError | undefined]
}>()

const fallthroughAttributes = useAttrs()
const combinedAttributes = computed(() => ({
  ...props.attributes,
  ...fallthroughAttributes,
}))

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
    case InputType.COMBOBOX:
      return ComboboxInput
    case InputType.TEXTAREA:
      return TextAreaInput
    case InputType.YEAR:
      return YearInput
    case InputType.TIME:
      return TimeInput
    default:
      throw new Error(`Unknown input type: ${props.type}`)
  }
})

const value = computed({
  get: () => props.modelValue,
  set: (newValue) => emit("update:modelValue", newValue),
})

const validationError = computed({
  get: () => props.validationError,
  set: (newValue) => {
    emit("update:validationError", newValue)
  },
})
</script>

<script lang="ts">
export default {
  inheritAttrs: false,
}
</script>

<template>
  <component
    :is="component"
    :id="id"
    v-model="value"
    v-bind="combinedAttributes"
    v-model:validation-error="validationError"
  />
</template>
