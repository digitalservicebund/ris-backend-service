<script lang="ts" setup>
import dayjs from "dayjs"
import customParseFormat from "dayjs/plugin/customParseFormat"
import { computed } from "vue"
import ChipsInput from "@/components/input/ChipsInput.vue"
import { ValidationError } from "@/components/input/types"

interface Props {
  id: string
  modelValue?: string[]
  ariaLabel: string
}
const props = defineProps<Props>()

const emit = defineEmits<{
  "update:modelValue": [value?: string[]]
  "update:validationError": [value?: ValidationError]
}>()

const chips = computed<string[]>({
  get: () => {
    return props.modelValue ? props.modelValue : []
  },

  set: (newValue: string[]) => {
    if (!newValue || newValue.length === 0) {
      emit("update:modelValue", undefined)
      return
    }

    const lastValue = newValue.at(-1)
    const lastYear = dayjs(lastValue, "YYYY", true)
    const validYear =
      lastYear.isValid() && lastYear.year() >= 1000 && lastYear.year() <= 9999

    if (!validYear) {
      emit("update:validationError", {
        message: "Kein valides Jahr",
        instance: props.id,
      })
      return
    }

    // if valid date, check for future dates
    const isInFuture = dayjs(lastValue, "YYYY", true).isAfter(dayjs())
    if (isInFuture) {
      emit("update:validationError", {
        message: props.ariaLabel + " darf nicht in der Zukunft liegen",
        instance: props.id,
      })
      return
    }
    emit("update:validationError", undefined)
    emit("update:modelValue", newValue)
  },
})

dayjs.extend(customParseFormat)
</script>

<template>
  <ChipsInput
    :id="id"
    v-model="chips"
    :aria-label="ariaLabel"
    maska="####"
    @update:validation-error="$emit('update:validationError', $event)"
  />
</template>
