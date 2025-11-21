<script lang="ts" setup>
import { RisChipsInput } from "@digitalservicebund/ris-ui/components"
import { storeToRefs } from "pinia"
import { computed } from "vue"
import { ValidationError } from "@/components/input/types"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

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

const localChips = computed(() => props.modelValue ?? [])
const { documentUnit } = storeToRefs(useDocumentUnitStore())

function isValidBorderNumber(value: number) {
  return validateBorderNumber(value)
}
function isDuplicate(value: number, values: number[] = []) {
  return values.filter((v) => v === value).length > 1
}

const chips = computed<string[]>({
  get: () => {
    return localChips.value.map((value) => value.toString())
  },

  set: (newValues: string[]) => {
    const numberValues = newValues.map((value) => Number.parseInt(value))

    const isValid = newValues.every((value) =>
      validateInput(Number.parseInt(value), numberValues),
    )

    if (isValid) {
      clearValidationErrors()
      emit("update:modelValue", numberValues)
    }
  },
})

function validateInput(value?: number, allValues: number[] = []) {
  if (value && !isValidBorderNumber(value)) {
    emit("update:validationError", {
      message: "Randnummer existiert nicht",
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

function validateBorderNumber(value: number): boolean {
  if (documentUnit.value?.managementData.borderNumbers) {
    return documentUnit.value.managementData.borderNumbers.includes(`${value}`)
  }
  return false
}
</script>

<template>
  <RisChipsInput
    v-model="chips"
    :aria-label="ariaLabel"
    :data-testid="testId"
    :has-error="hasError"
    :input-id="id"
    placeholder="Randnummer"
    :read-only="readOnly"
    @blur="clearValidationErrors"
  />
</template>
