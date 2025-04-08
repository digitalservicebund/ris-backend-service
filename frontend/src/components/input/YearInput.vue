<script lang="ts" setup>
import dayjs from "dayjs"
import customParseFormat from "dayjs/plugin/customParseFormat"
import InputMask from "primevue/inputmask"
import { computed, onMounted, ref, watch } from "vue"
import { ValidationError } from "@/components/input/types"

interface Props {
  id: string
  modelValue: string | undefined
  hasError?: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  "update:modelValue": [value: string | undefined]
  "update:validationError": [value?: ValidationError]
}>()

const inputCompleted = computed(() => {
  return !!inputValue.value && /\d{4}/.test(inputValue.value)
})

const inputValue = ref(
  props.modelValue ? dayjs(props.modelValue).format("YYYY") : undefined,
)

dayjs.extend(customParseFormat)

const isValidYear = computed(() => {
  return dayjs(inputValue.value, "YYYY", true).isValid()
})

function validateInput() {
  if (!inputCompleted.value) {
    console.log(inputValue.value)
    emit("update:validationError", {
      message: "Unvollständiges Jahr",
      instance: props.id,
    })
    return
  }

  if (!isValidYear.value) {
    emit("update:validationError", {
      message: "Kein valides Jahr",
      instance: props.id,
    })
  } else {
    emit("update:validationError", undefined)
  }
}

onMounted(() => {
  if (inputValue.value) validateInput()
})

function backspaceDelete() {
  emit("update:validationError", undefined)
  if (inputValue.value === "") emit("update:modelValue", inputValue.value)
}

function onBlur() {
  validateInput()
}

watch(
  () => props.modelValue,
  () => {
    inputValue.value = props.modelValue
  },
)

watch(inputValue, () => {
  if (inputValue.value === "") emit("update:modelValue", undefined)
  if (isValidYear.value && inputCompleted.value) {
    emit("update:modelValue", inputValue.value)
  }

  if (inputCompleted.value) {
    validateInput()
  }
})
</script>

<template>
  <InputMask
    :id="id"
    v-model="inputValue"
    :aria-label="($attrs.ariaLabel as string) ?? ''"
    fluid
    :invalid="hasError"
    mask="9999"
    placeholder="JJJJ"
    size="small"
    @blur="onBlur"
    @keydown.delete="backspaceDelete"
  />
</template>
