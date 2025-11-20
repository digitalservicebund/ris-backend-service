<script lang="ts" setup>
import { RisChipsInput } from "@digitalservicebund/ris-ui/components"
import { storeToRefs } from "pinia"
import { computed, ref } from "vue"
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
const lastChip = ref<number | undefined>()
const { documentUnit } = storeToRefs(useDocumentUnitStore())
const isValidBorderNumber = computed(() =>
  documentUnit.value?.managementData.borderNumbers.includes(
    `${lastChip.value}`,
  ),
)
const isDuplicate = computed(
  () => lastChip.value && localChips.value.includes(lastChip.value),
)

const chips = computed<string[]>({
  get: () => {
    return localChips.value.map((value) => value.toString())
  },

  set: (newValue: string[]) => {
    const oldLength = localChips.value.length
    const newLength = newValue.length

    if (newLength === 0) {
      emit("update:modelValue", [])
      clearValidationErrors()
      return
    }

    const newNumber = newValue.at(-1)
    if (newLength > oldLength && newNumber) {
      lastChip.value = Number.parseInt(newNumber)

      validateInput()
      if (isValidBorderNumber.value && !isDuplicate.value) {
        emit(
          "update:modelValue",
          newValue.map((value) => Number.parseInt(value)),
        )
      }
    } else if (newLength < oldLength) {
      clearValidationErrors()
      emit(
        "update:modelValue",
        newValue.map((value) => Number.parseInt(value)),
      )
    }
  },
})

function validateInput() {
  if (!isValidBorderNumber.value && lastChip.value) {
    emit("update:validationError", {
      message: "Randnummer existiert nicht",
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
