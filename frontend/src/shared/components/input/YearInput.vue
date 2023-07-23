<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import TextInput from "@/shared/components/input/TextInput.vue"
import { ValidationError } from "@/shared/components/input/types"

interface Props {
  id: string
  value?: string
  modelValue?: string
  ariaLabel: string
  hasError?: boolean
  isFutureDate?: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  "update:modelValue": [value?: string]
  "update:validationError": [value?: ValidationError]
}>()

const inputCompleted = ref<boolean>(false)
const inputValue = ref(props.modelValue)
const YearPlaceHolder = "JJJJ"

const isValidYear = computed(() => {
  const inputValueValue = inputValue.value
  if (!inputValueValue) return false // Handle empty input

  const event = window.event as KeyboardEvent | undefined
  if (!event) return false // Handle case when event is undefined

  const isNumber = /^\d+$/.test(event.key)
  const isControlKey = [
    "Backspace",
    "Delete",
    "ArrowLeft",
    "ArrowRight",
  ].includes(event.key)

  if (!isNumber && !isControlKey) {
    event.preventDefault()
    return false
  }

  if (!isNumber || (inputValueValue.length >= 4 && !isControlKey)) {
    event.preventDefault()
    return false
  }

  return /^\d{4}$/.test(inputValueValue)
})

const effectiveHasError = computed(
  () => props.hasError || (inputCompleted.value && !props.isFutureDate),
)
const conditionalClasses = computed(() => ({
  "has-error placeholder-black": props.hasError || effectiveHasError.value,
}))

const handlePaste = async (event: ClipboardEvent) => {
  const clipboardData = event.clipboardData
  if (clipboardData !== null) {
    const pastedText = clipboardData.getData("text/plain")
    if (/^\d+$/.test(pastedText.substring(0, 3))) {
      return
    } else {
      event.preventDefault()
    }
  }
}

function validateInput() {
  if (!isValidYear.value) {
    emit("update:validationError", {
      defaultMessage: "Unvollständiges Jahr",
      field: props.id,
    })
  } else {
    emit("update:validationError", undefined)
  }
}

// function onlyAllowNumbers(event: KeyboardEvent) {
//   const isNumber = /^\d+$/.test(event.key)
//   const isControlKey = [
//     "Backspace",
//     "Delete",
//     "ArrowLeft",
//     "ArrowRight",
//   ].includes(event.key)
//   if (!isNumber && !isControlKey) {
//     event.preventDefault()
//   }

//   if (!isNumber || ((inputValue.value?.length ?? 0) >= 4 && !isControlKey)) {
//     event.preventDefault()
//   }
// }

function onBlur() {
  validateInput()
  if (!isValidYear.value) {
    // If the value is not valid, emit undefined
    emit("update:modelValue", undefined)
  }
}

watch(
  () => props.modelValue,
  (newValue) => {
    if (newValue !== undefined) {
      inputValue.value = newValue
    }
  },
  { immediate: true },
)

watch(inputValue, () => emit("update:modelValue", inputValue.value), {
  deep: true,
})

watch(inputCompleted, () => {
  if (inputCompleted.value === true) validateInput()
})
</script>

<template>
  <TextInput
    :id="id"
    v-model="inputValue"
    :aria-label="ariaLabel"
    :class="conditionalClasses"
    maxlength="4"
    :placeholder="YearPlaceHolder"
    @blur="onBlur"
    @paste="handlePaste"
  />
</template>
