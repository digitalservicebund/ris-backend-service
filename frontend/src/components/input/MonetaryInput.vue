<script lang="ts" setup>
import InputNumber, { InputNumberInputEvent } from "primevue/inputnumber"
import { ref } from "vue"

const props = defineProps<{
  id: string
  modelValue?: number
  hasError?: boolean
}>()
const emit = defineEmits<{
  "update:modelValue": [value?: number]
}>()

const amountRef = ref(props.modelValue)

function onInput(event: InputNumberInputEvent) {
  amountRef.value = event.value as number
  emit("update:modelValue", amountRef.value)
}

function valueChanged(value: number) {
  amountRef.value = value
  emit("update:modelValue", amountRef.value)
}
</script>

<template>
  <InputNumber
    :id="id"
    v-model="amountRef"
    aria-label="Betrag"
    class="w-full"
    fluid
    input-class="w-full"
    :invalid="hasError"
    locale="de"
    :min="0"
    @input="onInput"
    @value-change="valueChanged"
  />
</template>
