<script lang="ts" setup>
import { RisChipsInput } from "@digitalservicebund/ris-ui/components"
import dayjs from "dayjs"
import customParseFormat from "dayjs/plugin/customParseFormat"
import { computed, ref, watch } from "vue"
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

const formattedChips = ref<string[]>([])

function isValidDate(value?: string) {
  return value ? dayjs(value, "DD.MM.YYYY", true).isValid() : false
}
function isInFuture(value?: string) {
  return value ? dayjs(value, "DD.MM.YYYY", true).isAfter(dayjs()) : false
}
function isDuplicate(value?: string, values: string[] = []) {
  if (!value) return false
  const firstIndex = values.indexOf(value)
  const lastIndex = values.lastIndexOf(value)
  return firstIndex !== -1 && firstIndex !== lastIndex
}

const chips = computed<string[]>({
  get: () => formattedChips.value,
  set: (newValues: string[]) => {
    const oldLength = formattedChips.value.length
    const newLength = newValues.length

    if (newLength >= oldLength) {
      const isValid = newValues.every((value) =>
        validateInput(value, newValues),
      )

      if (isValid) {
        const valuesInStandardFormat = newValues.map((value) =>
          dayjs(value, "DD.MM.YYYY", true).format("YYYY-MM-DD"),
        )
        emit("update:modelValue", valuesInStandardFormat)
      }
    } else if (newLength < oldLength) {
      clearValidationErrors()

      const valuesInStandardFormat = newValues.map((value) =>
        dayjs(value, "DD.MM.YYYY", true).format("YYYY-MM-DD"),
      )
      emit("update:modelValue", valuesInStandardFormat)
    }
  },
})

function validateInput(value?: string, allValues: string[] = []) {
  if (!isValidDate(value)) {
    emit("update:validationError", {
      message: "Kein valides Datum",
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

dayjs.extend(customParseFormat)

watch(
  () => props.modelValue,
  (newValue, oldValue) => {
    if (JSON.stringify(newValue) !== JSON.stringify(oldValue)) {
      formattedChips.value = newValue
        ? newValue.map((value) =>
            dayjs(value, "YYYY-MM-DD", true).format("DD.MM.YYYY"),
          )
        : []
    }
  },
  { immediate: true },
)
</script>

<template>
  <RisChipsInput
    v-model="chips"
    :aria-label="ariaLabel"
    :data-testid="testId"
    :has-error="hasError"
    :input-id="id"
    mask="99.99.9999"
    placeholder="TT.MM.JJJJ"
    :read-only="readOnly"
    @blur="clearValidationErrors"
    @focus="clearValidationErrors"
  />
</template>
