<script lang="ts" setup>
import { RisChipsInput } from "@digitalservicebund/ris-ui/components"
import dayjs from "dayjs"
import customParseFormat from "dayjs/plugin/customParseFormat"
import { computed } from "vue"
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

function isValidYear(value?: string) {
  return value ? validateYear(value) : false
}
function isInFuture(value?: string) {
  return value ? dayjs(value, "YYYY", true).isAfter(dayjs()) : false
}
function isDuplicate(value?: string, values: string[] = []) {
  if (!value) return false
  return values.filter((v) => v === value).length > 1
}

const chips = computed<string[]>({
  get: () => localChips.value,

  set: (newValues: string[]) => {
    const isValid = newValues.every((value) => validateInput(value, newValues))

    if (isValid) {
      emit("update:modelValue", newValues)
    }
  },
})

function validateInput(value?: string, allValues: string[] = []) {
  if (!isValidYear(value)) {
    emit("update:validationError", {
      message: "Kein valides Jahr",
      instance: props.id,
    })
    return false
  } else if (isInFuture(value)) {
    emit("update:validationError", {
      message: props.ariaLabel + " darf nicht in der Zukunft liegen",
      instance: props.id,
    })
    return false
  } else if (isDuplicate(value, allValues)) {
    emit("update:validationError", {
      message: value + " bereits vorhanden",
      instance: props.id,
    })
    return false
  } else {
    clearValidationErrors()
    return true
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
