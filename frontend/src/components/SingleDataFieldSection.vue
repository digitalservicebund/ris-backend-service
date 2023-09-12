<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { useLocator } from "@/composables/useLocator"
import InputElement from "@/shared/components/input/InputElement.vue"
import InputField from "@/shared/components/input/InputField.vue"
import {
  InputAttributes,
  InputType,
  ModelType,
} from "@/shared/components/input/types"

interface Props {
  id: string
  label: string
  type?: InputType
  modelValue: ModelType
  readonly?: boolean
  inputAttributes?: InputAttributes
  required?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  type: InputType.TEXT,
  readonly: false,
  inputAttributes: undefined,
  required: false,
})

const emit = defineEmits<{
  "update:modelValue": [value: ModelType]
}>()

const value = ref(props.modelValue)

const inputAttributes = computed(
  (): InputAttributes => ({
    ariaLabel: props.label,
    readOnly: props.readonly,
    autosize: props.type === InputType.TEXTAREA,
    ...props.inputAttributes,
  }),
)

watch(
  () => props.modelValue,
  () => (value.value = props.modelValue),
  { deep: true },
)

watch(value, () => emit("update:modelValue", value.value), { deep: true })

const { getLocator } = useLocator()
const inputFieldId = getLocator(() => [props.id])
</script>

<template>
  <div
    class="flex items-start gap-8 border-b border-gray-400 bg-white p-8 pl-16"
  >
    <label class="ds-label-02-bold my-12 w-240 flex-none" :for="inputFieldId">
      <h2 class="mr-8 inline-block">{{ label }}</h2>
      <span v-if="required">*</span>
    </label>
    <InputField
      :id="inputFieldId"
      v-slot="{ id: inputElementId, hasError, updateValidationError }"
      class="!mb-0 overflow-hidden"
      :label="label"
      visually-hide-label
    >
      <InputElement
        :id="inputElementId"
        v-model="value"
        :attributes="inputAttributes"
        class="ds-label-02-reg"
        disable-error
        :has-error="hasError"
        :type="type"
        @update:validation-error="updateValidationError"
      />
    </InputField>
  </div>
</template>
