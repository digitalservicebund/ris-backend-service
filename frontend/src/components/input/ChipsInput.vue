<script lang="ts" setup>
import { RisChipsInput } from "@digitalservicebund/ris-ui/components"
import { computed } from "vue"
import { ValidationError } from "@/components/input/types"

interface Props {
  id: string
  modelValue?: string[]
  ariaLabel: string
  readOnly?: boolean
  testId?: string
  placeholder?: string
}

const props = defineProps<Props>()

const emit = defineEmits<{
  "update:modelValue": [value?: string[]]
  "update:validationError": [value?: ValidationError]
}>()

const localChips = computed<string[]>(() => props.modelValue ?? [])

function isDuplicate(value: string, values: string[] = []) {
  return values.filter((v) => v === value).length > 1
}

const chips = computed<string[]>({
  get: () => localChips.value,

  set: (newValues: string[]) => {
    const hasValueWithWhitespaces = newValues.some(
      (value) => value.trim() === "",
    )
    if (hasValueWithWhitespaces) {
      newValues = newValues.filter((value) => value.trim() !== "")
      emit("update:modelValue", newValues)
      return
    }

    const isValid = newValues.every((value) => validateInput(value, newValues))

    if (isValid) {
      clearValidationErrors()
      emit("update:modelValue", newValues)
    }
  },
})

function validateInput(value?: string, allValues: string[] = []) {
  if (value && isDuplicate(value, allValues)) {
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
</script>

<template>
  <RisChipsInput
    v-model="chips"
    :aria-label="ariaLabel"
    :data-testid="testId"
    :input-id="id"
    :placeholder="placeholder"
    :read-only="readOnly"
    @blur="clearValidationErrors"
  />
</template>
