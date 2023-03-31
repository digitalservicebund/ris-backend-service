<script lang="ts" setup>
import { ref, watch } from "vue"
import InputGroup from "@/shared/components/input/InputGroup.vue"
import { InputField, InputType } from "@/shared/components/input/types"

interface Props {
  modelValue: { leadJurisdiction: string; leadUnit: string }
}

interface Emits {
  (event: "update:modelValue", value: unknown): void
}
const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const inputValues = ref({})
const fields: InputField[] = [
  {
    name: "leadJurisdiction",
    type: InputType.TEXT,
    label: "Ressort",
    inputAttributes: {
      ariaLabel: "Ressort",
    },
  },
  {
    name: "leadUnit",
    type: InputType.TEXT,
    label: "Organisationseinheit",
    inputAttributes: {
      ariaLabel: "Organisationseinheit",
    },
  },
]

watch(
  () => props.modelValue,
  () => (inputValues.value = props.modelValue),
  { immediate: true, deep: true }
)

watch(
  inputValues,
  () => {
    emit("update:modelValue", inputValues.value)
  },
  { deep: true }
)
</script>

<template>
  <InputGroup v-model="inputValues" :column-count="2" :fields="fields" />
</template>
