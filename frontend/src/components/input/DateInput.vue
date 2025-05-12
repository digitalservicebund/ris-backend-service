<script lang="ts" setup>
import dayjs from "dayjs"
import customParseFormat from "dayjs/plugin/customParseFormat"
import InputMask from "primevue/inputmask"
import { computed, onMounted, ref, watch } from "vue"
import { ValidationError } from "@/components/types"

interface Props {
  id: string
  value?: string
  modelValue?: string
  ariaLabel: string
  isFutureDate?: boolean
  hasError?: boolean
  size?: "regular" | "medium" | "small"
  readOnly?: boolean
  disabled?: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  "update:modelValue": [value?: string]
  "update:validationError": [value?: ValidationError]
}>()

const inputCompleted = computed(() => {
  return !!inputValue.value && /^\d{2}\.\d{2}\.\d{4}$/.test(inputValue.value)
})

const inputValue = ref(
  props.modelValue ? dayjs(props.modelValue).format("DD.MM.YYYY") : undefined,
)

dayjs.extend(customParseFormat)

const isValidDate = computed(() => {
  return dayjs(inputValue.value, "DD.MM.YYYY", true).isValid()
})

const isInPast = computed(() => {
  if (props.isFutureDate) return true
  return dayjs(inputValue.value, "DD.MM.YYYY", true).isBefore(dayjs())
})

function validateInput() {
  if (!inputValue.value) {
    emit("update:validationError", undefined)
    return
  }
  if (!inputCompleted.value) {
    emit("update:validationError", {
      message: "Unvollständiges Datum",
      instance: props.id,
    })
    return
  }

  if (!isValidDate.value) {
    emit("update:validationError", {
      message: "Kein valides Datum",
      instance: props.id,
    })
    return
  }

  if (!isInPast.value && !props.isFutureDate) {
    emit("update:validationError", {
      message: "Das Datum darf nicht in der Zukunft liegen",
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
  (is) => {
    inputValue.value = is
      ? dayjs(is, "YYYY-MM-DD", true).format("DD.MM.YYYY")
      : undefined
  },
)

watch(inputValue, (is) => {
  if (is === "") emit("update:modelValue", undefined)
  if (isValidDate.value && isInPast.value) {
    emit(
      "update:modelValue",
      dayjs(is, "DD.MM.YYYY", true).format("YYYY-MM-DD"),
    )
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
    :aria-label="ariaLabel"
    :auto-clear="false"
    :disabled="disabled"
    fluid
    :invalid="hasError"
    mask="99.99.9999"
    placeholder="TT.MM.JJJJ"
    :readonly="readOnly"
    @blur="onBlur"
    @complete="validateInput"
    @focus="emit('update:validationError', undefined)"
    @keydown.delete="backspaceDelete"
  />
</template>
