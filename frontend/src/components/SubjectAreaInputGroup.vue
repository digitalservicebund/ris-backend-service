<script lang="ts" setup>
import { ref, watch } from "vue"
import InputGroup from "@/shared/components/input/InputGroup.vue"
import { InputField, InputType } from "@/shared/components/input/types"

interface Props {
  modelValue: {
    subjectFna: string
    subjectPreviousFna: string
    subjectGesta: string
    subjectBgb3: string
  }
}

interface Emits {
  (event: "update:modelValue", value: unknown): void
}
const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const inputValues = ref({})
const fields: InputField[] = [
  {
    name: "subjectFna",
    type: InputType.TEXT,
    label: "FNA-Nummer",
    inputAttributes: {
      ariaLabel: "FNA-Nummer",
    },
  },
  {
    name: "subjectPreviousFna",
    type: InputType.TEXT,
    label: "Frühere FNA-Nummer",
    inputAttributes: {
      ariaLabel: "Frühere FNA-Nummer",
    },
  },
  {
    name: "subjectGesta",
    type: InputType.TEXT,
    label: "GESTA-Nummer",
    inputAttributes: {
      ariaLabel: "GESTA-Nummer",
    },
  },
  {
    name: "subjectBgb3",
    type: InputType.TEXT,
    label: "Bundesgesetzblatt Teil III",
    inputAttributes: {
      ariaLabel: "Bundesgesetzblatt Teil III",
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
