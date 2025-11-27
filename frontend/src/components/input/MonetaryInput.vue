<script lang="ts" setup>
import InputNumber, { InputNumberInputEvent } from "primevue/inputnumber"
import { computed, ref } from "vue"
import { ValidationError } from "@/components/input/types"

interface Props {
  id: string
  value?: string
  modelValue?: number
  ariaLabel: string
  hasError?: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  "update:modelValue": [value?: string]
  "update:validationError": [value?: ValidationError]
}>()
const inputValue = ref(props.modelValue)

const isSixDigitNumber = computed(() => {
  return !!inputValue.value && /^\d{6}$/.test(inputValue.value.toString())
})

const isValidAmount = computed(() => {
  return !!inputValue.value && inputValue.value <= 999999
})
function validateInput() {
  if (isValidAmount.value) {
    emit("update:validationError", undefined)
    return
  }
  if (inputValue.value && !isSixDigitNumber.value && !isValidAmount.value) {
    emit("update:validationError", {
      message: "Max. 6 Zeichen",
      instance: props.id,
    })
  }
}

function onInput(event: InputNumberInputEvent) {
  inputValue.value = event.value as number
  validateInput()
}
</script>

<template>
  <InputNumber
    v-model="inputValue"
    :aria-label="ariaLabel"
    fluid
    :invalid="hasError"
    locale="de"
    :min="1"
    @focus="emit('update:validationError', undefined)"
    @input="onInput"
  />
</template>
