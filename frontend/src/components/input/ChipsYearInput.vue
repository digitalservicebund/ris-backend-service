<script lang="ts" setup>
import { RisChipsInput } from "@digitalservicebund/ris-ui/components"
import dayjs from "dayjs"
import customParseFormat from "dayjs/plugin/customParseFormat"
import { computed } from "vue"
import { ValidationError } from "@/components/input/types"

interface Props {
  id: string
  modelValue?: number[]
  ariaLabel: string
  hasError?: boolean
  readOnly?: boolean
  testId?: string
}

const props = defineProps<Props>()

const emit = defineEmits<{
  "update:modelValue": [value?: number[]]
  "update:validationError": [value?: ValidationError]
}>()

const localChips = computed<string[]>(
  () => props.modelValue?.map((year) => `${year}`) ?? [],
)

function isValidYear(value: string) {
  return validateYear(value)
}
function isInFuture(value: string) {
  return dayjs(value, "YYYY", true).isAfter(dayjs())
}
function isDuplicate(value: string, values: string[] = []) {
  return values.filter((v) => v === value).length > 1
}

const chips = computed<string[]>({
  get: () => localChips.value,

  set: (newValues: string[]) => {
    const isValid = newValues.every((value) => validateInput(value, newValues))

    if (isValid) {
      clearValidationErrors()
      emit(
        "update:modelValue",
        newValues.map((yearString) => Number.parseInt(yearString, 10)),
      )
    }
  },
})

function validateInput(value?: string, allValues: string[] = []) {
  if (value && !isValidYear(value)) {
    emit("update:validationError", {
      message: "Kein valides Jahr",
      instance: props.id,
    })
    return false
  } else if (value && isInFuture(value)) {
    emit("update:validationError", {
      message: props.ariaLabel + " darf nicht in der Zukunft liegen",
      instance: props.id,
    })
    return false
  } else if (value && isDuplicate(value, allValues)) {
    emit("update:validationError", {
      message: value + " bereits vorhanden",
      instance: props.id,
    })
    return false
  }
  return true
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
