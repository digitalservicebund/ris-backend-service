<script lang="ts" setup>
import { computed } from "vue"
import CheckboxInput from "@/components/CheckboxInput.vue"
import DateInput from "@/components/DateInput.vue"
import DropdownInput from "@/components/DropdownInput.vue"
import FileInput from "@/components/FileInput.vue"
import MultiTextInput from "@/components/MultiTextInput.vue"
import SubField from "@/components/SubField.vue"
import TextInput from "@/components/TextInput.vue"
import type { InputAttributes, ModelType } from "@/domain"
import { InputType, ValidationError } from "@/domain"

interface Props {
  id: string
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
    case InputType.CHIP:
      return MultiTextInput
    default:
      throw new Error(`Unknown input type: ${props.type}`)
  }
})

const subcomponent = computed(() => {
  switch (props.attributes.subField?.type) {
    case InputType.TEXT:
      return TextInput
    case InputType.DROPDOWN:
      return DropdownInput
    case InputType.DATE:
      return DateInput
    case InputType.CHECKBOX:
      return CheckboxInput
    case InputType.CHIP:
      return MultiTextInput
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
  <div>
    <component
      :is="component"
      :id="id"
      v-model="value"
      v-bind="attributes"
      :validation-error="validationError"
    />
    <SubField v-if="attributes.subField">
      <div class="mt-[2.5rem]">
        <label
          class="flex gap-4 items-center label-03-regular mb-2 text-gray-900"
          :for="attributes.subField.name"
        >
          {{ attributes.subField.label }}
        </label>
        <component
          :is="subcomponent"
          :id="attributes.subField.name"
          v-model="value"
          v-bind="attributes.subField.inputAttributes"
          :validation-error="validationError"
        />
      </div>
    </SubField>
  </div>
</template>
