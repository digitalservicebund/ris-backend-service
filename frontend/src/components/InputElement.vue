<script lang="ts" setup>
import { computed, ref } from "vue"
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
