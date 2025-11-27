<script lang="ts" setup>
import InputNumber, { InputNumberInputEvent } from "primevue/inputnumber"
import { ref } from "vue"

interface Props {
  id: string
  modelValue?: number
  ariaLabel: string
  hasError?: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  "update:modelValue": [value?: number]
}>()
const inputValue = ref(props.modelValue)

function onInput(event: InputNumberInputEvent) {
  inputValue.value = event.value as number
  emit("update:modelValue", inputValue.value)
}
</script>

<template>
  <InputNumber
    :id="id"
    v-model="inputValue"
    :aria-label="ariaLabel"
    fluid
    :invalid="hasError"
    locale="de"
    :min="1"
    @input="onInput"
  />
</template>
