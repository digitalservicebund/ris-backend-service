<script lang="ts" setup>
import { computed } from "vue"
import DateInput from "@/components/DateInput.vue"
import DropdownInput from "@/components/DropdownInput.vue"
import FileInputButton from "@/components/FileInputButton.vue"
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
    case "text":
      return TextInput
    case "file":
      return FileInputButton
    case "dropdown":
      return DropdownInput
    case "date":
      return DateInput
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
