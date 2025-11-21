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
  return dayjs(value, "DD.MM.YYYY", true).isValid()
}
function isInFuture(value: string) {
  return dayjs(value, "DD.MM.YYYY", true).isAfter(dayjs())
}
function isDuplicate(value: string, values: string[] = []) {
  return values.filter((v) => v === value).length > 1
}

const chips = computed<string[]>({
  get: () => formattedChips.value,
  set: (newValues: string[]) => {
    const isValid = newValues.every((value) => validateInput(value, newValues))

    if (isValid) {
      const valuesInStandardFormat = newValues.map((value) =>
        dayjs(value, "DD.MM.YYYY", true).format("YYYY-MM-DD"),
      )
      clearValidationErrors()
      emit("update:modelValue", valuesInStandardFormat)
    }
  },
})

function validateInput(value?: string, allValues: string[] = []) {
  if (value && !isValidDate(value)) {
    emit("update:validationError", {
      message: "Kein valides Datum",
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
