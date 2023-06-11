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

watch(
  () => props.modelValue,
  () => (value.value = props.modelValue),
  { deep: true }
)

watch(value, () => emit("update:modelValue", value.value), { deep: true })
</script>

<template>
  <div
    class="bg-white border-b border-gray-400 flex gap-8 items-start p-8 pl-16"
  >
    <label class="flex-none label-02-bold my-12 w-[15rem]" :for="id">
      <h2>{{ label }}</h2>
    </label>

    <InputElement
      :id="id"
      v-model="value"
      :attributes="inputAttributes"
      class="label-02-reg"
      disable-error
      :type="type"
    />
  </div>
</template>
