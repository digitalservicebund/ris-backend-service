<script lang="ts" setup>
import { RisChipsInput } from "@digitalservicebund/ris-ui/components"
import dayjs from "dayjs"
import customParseFormat from "dayjs/plugin/customParseFormat"
import { computed, ref } from "vue"
import { ValidationError } from "@/components/input/types"

interface Props {
  id: string
  modelValue?: string[]
  ariaLabel: string
  hasError?: boolean
  readOnly?: boolean
  testId?: string
}

const props = defineProps<Props>()

const emit = defineEmits<{
  "update:modelValue": [value?: string[]]
  "update:validationError": [value?: ValidationError]
}>()

const localChips = computed<string[]>(() => props.modelValue ?? [])
const lastChip = ref<string | undefined>("")
const isValidYear = computed(() => validateYear(lastChip.value))
const isInFuture = computed(() =>
  dayjs(lastChip.value, "YYYY", true).isAfter(dayjs()),
)
const isDuplicate = computed(
  () => lastChip.value && localChips.value.includes(lastChip.value),
)

const chips = computed<string[]>({
  get: () => localChips.value,

  set: (newValue: string[]) => {
    const oldLength = localChips.value.length
    const newLength = newValue.length

    if (newLength === 0) {
      emit("update:modelValue", undefined)
      clearValidationErrors()
      return
    }

    if (newLength > oldLength) {
      lastChip.value = newValue.at(-1)

      validateInput()
      if (isValidYear.value && !isInFuture.value && !isDuplicate.value) {
        emit("update:modelValue", newValue)
      }
    } else if (newLength < oldLength) {
      clearValidationErrors()
      emit("update:modelValue", newValue)
    }
  },
})

function validateInput() {
  if (!isValidYear.value && lastChip.value) {
    emit("update:validationError", {
      message: "Kein valides Jahr",
      instance: props.id,
    })
  } else if (isInFuture.value) {
    emit("update:validationError", {
      message: props.ariaLabel + " darf nicht in der Zukunft liegen",
      instance: props.id,
    })
    return
  } else if (isDuplicate.value) {
    emit("update:validationError", {
      message: lastChip.value + " bereits vorhanden",
      instance: props.id,
    })
  } else {
    clearValidationErrors()
  }
}

function clearValidationErrors() {
  emit("update:validationError", undefined)
}

function validateYear(input: string | undefined): boolean {
  if (!input || input.length < 4) return false

  const date = dayjs(input, "YYYY", true)
  return date.isValid() && date.year() >= 1000 && date.year() <= 9999
}

dayjs.extend(customParseFormat)
</script>

<template>
  <RisChipsInput
    v-model="chips"
    :aria-label="ariaLabel"
    :data-testid="testId"
    :has-error="hasError"
    :input-id="id"
    mask="9999"
    placeholder="JJJJ"
    :read-only="readOnly"
    @blur="clearValidationErrors"
  />
</template>
