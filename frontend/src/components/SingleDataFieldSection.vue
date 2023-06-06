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

interface Emits {
  (event: "update:modelValue", value: ModelType): void
}

const props = withDefaults(defineProps<Props>(), {
  type: InputType.TEXT,
  readonly: false,
})

const emit = defineEmits<Emits>()
const value = ref(props.modelValue)

const inputAttributes = computed(
  (): InputAttributes => ({ ariaLabel: props.label, readOnly: props.readonly })
)

const dynamicClasses = computed(() => ({
  "hover:!bg-blue-200": !props.readonly,
  "hover:cursor-pointer": !props.readonly,
  "focus:bg-blue-200": !props.readonly,
  "focus:hover:!outline-2": !props.readonly,
  "focus:hover:!outline-blue-800": !props.readonly,
  "h-44": [InputType.TEXT, InputType.DATE].includes(props.type),
}))

watch(
  () => props.modelValue,
  () => (value.value = props.modelValue),
  { deep: true }
)

watch(value, () => emit("update:modelValue", value.value), { deep: true })
</script>

<template>
  <div class="bg-white border-b border-gray-400 flex p-6 pl-16">
    <label
      class="label-02-bold max-w-[14rem] min-w-[14rem] my-12 text-left"
      :for="id"
    >
      <h2>{{ label }}</h2>
    </label>

    <InputElement
      :id="id"
      v-model="value"
      :attributes="inputAttributes"
      class="!border-none hover:outline-none label-02-reg outline-offset-2 self-center w-full"
      :class="dynamicClasses"
      disable-error
      :type="type"
    />
  </div>
</template>
