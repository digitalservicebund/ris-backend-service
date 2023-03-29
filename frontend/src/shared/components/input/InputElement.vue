<script lang="ts" setup>
import { computed, ref } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import CheckboxInput from "@/shared/components/input/CheckboxInput.vue"
import ChipsDateInput from "@/shared/components/input/ChipsDateInput.vue"
import ChipsInput from "@/shared/components/input/ChipsInput.vue"
import CustomDateInput from "@/shared/components/input/CustomDateInput.vue"
import DateInput from "@/shared/components/input/DateInput.vue"
import DropdownInput from "@/shared/components/input/DropdownInput.vue"
import FileInput from "@/shared/components/input/FileInput.vue"
import NestedInput from "@/shared/components/input/NestedInput.vue"
import TextInput from "@/shared/components/input/TextInput.vue"
import {
  InputAttributes,
  ModelType,
  InputType,
  ValidationError,
} from "@/shared/components/input/types"

interface Props {
  id: string
  type?: InputType
  modelValue: ModelType
  attributes: InputAttributes
  validationError?: ValidationError
}

interface Emits {
  (event: "update:modelValue", value: ModelType): void
  (event: "update:validationError", value: ValidationError | undefined): void
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
    case InputType.CUSTOMDATE:
      return CustomDateInput
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

const errorMessage = ref<ValidationError | undefined>(props.validationError)

const validationError = computed({
  get: () => props.validationError,
  set: (newValue) => {
    errorMessage.value = newValue
    emit("update:validationError", newValue)
  },
})
</script>

<template>
  <component
    :is="component"
    :id="id"
    v-model="value"
    v-bind="attributes"
    v-model:validation-error="validationError"
  />
  <!-- TODO this is a workaround, errors shoudld be displayes in in the InputField component in the future -->
  <div class="h-16 label-03-reg text-red-800">
    {{ errorMessage?.defaultMessage }}
  </div>
</template>
