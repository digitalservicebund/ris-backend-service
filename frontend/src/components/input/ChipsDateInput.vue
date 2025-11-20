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

const lastChip = ref<string | undefined>("")
const formattedChips = ref<string[]>([])

function isValidDate(value?: string) {
  return value ? dayjs(value, "DD.MM.YYYY", true).isValid() : false
}
function isInFuture(value?: string) {
  return value ? dayjs(value, "DD.MM.YYYY", true).isAfter(dayjs()) : false
}
function isDuplicate(value?: string, chipsArr: string[] = []) {
  return value ? chipsArr.includes(value) : false
}

const chips = computed<string[]>({
  get: () => formattedChips.value,
  set: (newValue: string[]) => {
    const oldLength = formattedChips.value.length
    const newLength = newValue.length

    if (newLength >= oldLength) {
      lastChip.value = newValue.at(-1)

      const isValid = validateInput(lastChip.value, newValue.slice(0, -1))

      if (isValid) {
        const valuesInStandardFormat = newValue.map((value) =>
          dayjs(value, "DD.MM.YYYY", true).format("YYYY-MM-DD"),
        )
        emit("update:modelValue", valuesInStandardFormat)
      }
    } else if (newLength < oldLength) {
      clearValidationErrors()

      const valuesInStandardFormat = newValue.map((value) =>
        dayjs(value, "DD.MM.YYYY", true).format("YYYY-MM-DD"),
      )
      emit("update:modelValue", valuesInStandardFormat)
    }
  },
})

function validateInput(oldValue?: string, newValue?: string[]) {
  if (!isValidDate(oldValue)) {
    emit("update:validationError", {
      message: "Kein valides Datum",
      instance: props.id,
    })
    return false
  } else if (isInFuture(oldValue)) {
    emit("update:validationError", {
      message: props.ariaLabel + " darf nicht in der Zukunft liegen",
      instance: props.id,
    })
    return false
  } else if (isDuplicate(oldValue, newValue)) {
    emit("update:validationError", {
      message: oldValue + " bereits vorhanden",
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
