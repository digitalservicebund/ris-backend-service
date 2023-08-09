<script lang="ts" setup>
import { computed, ref, watch } from "vue"
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
}

const props = withDefaults(defineProps<Props>(), {
  type: InputType.TEXT,
  readonly: false,
  inputAttributes: undefined,
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
</script>

<template>
  <div
    class="border-l-red-800 [&:has(.has-error)]:border-l-8 [&:has(.has-error)]:pl-8"
  >
    <div
      class="flex items-start gap-8 border-b border-gray-400 bg-white p-8 pl-16"
    >
      <label class="ds-label-02-bold my-12 w-240 flex-none" :for="id">
        <h2>{{ label }}</h2>
      </label>
      <InputField
        :id="id"
        v-slot="{ id: inputElementId, hasError }"
        class="!mb-0"
        :label="label"
        visually-hide-label
      >
        <InputElement
          :id="inputElementId"
          v-model="value"
          :attributes="inputAttributes"
          class="ds-label-02-reg"
          :disable-error="!hasError"
          :has-error="hasError"
          :type="type"
        />
      </InputField>
    </div>
  </div>
</template>
