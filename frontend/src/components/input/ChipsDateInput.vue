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

const lastChip = ref<string | undefined>("")
const formattedChips = computed<string[]>(() =>
  props.modelValue
    ? props.modelValue.map((value) =>
        dayjs(value, "YYYY-MM-DD", true).format("DD.MM.YYYY"),
      )
    : [],
)

const isValidDate = computed(() =>
  dayjs(lastChip.value, "DD.MM.YYYY", true).isValid(),
)
const isInFuture = computed(() =>
  dayjs(lastChip.value, "DD.MM.YYYY", true).isAfter(dayjs()),
)
const isDuplicate = computed(
  () => lastChip.value && formattedChips.value.includes(lastChip.value),
)

const chips = computed<string[]>({
  get: () => formattedChips.value,
  set: (newValue: string[]) => {
    const oldLength = formattedChips.value.length
    const newLength = newValue.length

    if (newLength === 0) {
      emit("update:modelValue", [])
      clearValidationErrors()
      return
    }

    if (newLength > oldLength) {
      lastChip.value = newValue.at(-1)

      validateInput()

      if (isValidDate.value && !isInFuture.value && !isDuplicate.value) {
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

function validateInput() {
  if (!isValidDate.value && lastChip.value) {
    emit("update:validationError", {
      message: "Kein valides Datum",
      instance: props.id,
    })
  } else if (isInFuture.value) {
    emit("update:validationError", {
      message: props.ariaLabel + " darf nicht in der Zukunft liegen",
      instance: props.id,
    })
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

dayjs.extend(customParseFormat)
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
