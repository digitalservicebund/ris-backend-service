<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import InputElement from "@/shared/components/input/InputElement.vue"
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
}

const props = withDefaults(defineProps<Props>(), {
  type: InputType.TEXT,
  readonly: false,
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
    class="flex items-start gap-8 border-b border-gray-400 bg-white p-8 pl-16"
  >
    <label class="ds-label-02-bold my-12 w-[15rem] flex-none" :for="id">
      <h2>{{ label }}</h2>
    </label>

    <InputElement
      :id="id"
      v-model="value"
      :attributes="inputAttributes"
      class="ds-label-02-reg"
      disable-error
      :type="type"
    />
  </div>
</template>
