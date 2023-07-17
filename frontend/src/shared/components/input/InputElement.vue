<script lang="ts" setup>
import { computed, ref, useAttrs } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import CheckboxInput from "@/shared/components/input/CheckboxInput.vue"
import ChipsDateInput from "@/shared/components/input/ChipsDateInput.vue"
import ChipsInput from "@/shared/components/input/ChipsInput.vue"
import DateInput from "@/shared/components/input/DateInput.vue"
import DropdownInput from "@/shared/components/input/DropdownInput.vue"
import FileInput from "@/shared/components/input/FileInput.vue"
import NestedInput from "@/shared/components/input/NestedInput.vue"
import TextAreaInput from "@/shared/components/input/TextAreaInput.vue"
import TextInput from "@/shared/components/input/TextInput.vue"
import TimeInput from "@/shared/components/input/TimeInput.vue"
import {
  InputAttributes,
  InputType,
  ModelType,
  ValidationError,
} from "@/shared/components/input/types"
import YearInput from "@/shared/components/input/YearInput.vue"

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
    case InputType.NESTED:
      return NestedInput
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

const errorMessage = ref<ValidationError | undefined>(props.validationError)

const validationError = computed({
  get: () => props.validationError,
  set: (newValue) => {
    errorMessage.value = newValue
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
  <!-- TODO this is a workaround, errors shoudld be displayes in in the InputField component in the future -->
  <div v-if="!disableError" class="label-03-reg h-16 text-red-800">
    {{ errorMessage?.defaultMessage }}
  </div>
</template>
