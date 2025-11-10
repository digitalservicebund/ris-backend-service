<script lang="ts" setup>
import dayjs from "dayjs"
import customParseFormat from "dayjs/plugin/customParseFormat"
import { storeToRefs } from "pinia"
import { computed, ref } from "vue"
import ChipsInput from "@/components/input/ChipsInput.vue"
import { ValidationError } from "@/components/input/types"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

interface Props {
  id: string
  modelValue?: number[]
  ariaLabel: string
  hasError?: boolean
  readOnly?: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  "update:modelValue": [value?: number[]]
  "update:validationError": [value?: ValidationError]
}>()

const { documentUnit } = storeToRefs(useDocumentUnitStore())
const lastChipValue = ref<number | undefined>()
const isValidBorderNumber = computed(
  () =>
    !documentUnit.value?.managementData.borderNumbers.includes(
      `${lastChipValue.value}`,
    ),
)

const chips = computed<string[]>({
  get: () => {
    return props.modelValue
      ? props.modelValue.map((value) => value.toString())
      : []
  },

  set: (newValue: string[]) => {
    if (!newValue || newValue.length === 0) {
      emit("update:modelValue", undefined)
      return
    }

    const foo = newValue.at(-1) ?? ""
    lastChipValue.value = Number.parseInt(foo)

    validateInput()

    if (isValidBorderNumber.value)
      emit(
        "update:modelValue",
        newValue.map((value) => Number.parseInt(value)),
      )
  },
})

function validateInput(event?: ValidationError) {
  if (event) {
    emit("update:validationError", event)
    return
  }
  if (!isValidBorderNumber.value && lastChipValue.value) {
    emit("update:validationError", {
      message: "Randnummer existiert nicht",
      instance: props.id,
    })
  } else {
    emit("update:validationError", undefined)
  }
}

dayjs.extend(customParseFormat)
</script>

<template>
  <ChipsInput
    :id="id"
    v-model="chips"
    :aria-label="ariaLabel"
    :has-error="hasError"
    maska="####"
    :read-only="readOnly"
    @update:validation-error="validateInput($event)"
  />
</template>
