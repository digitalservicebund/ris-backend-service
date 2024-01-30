<script lang="ts" setup>
import dayjs from "dayjs"
import customParseFormat from "dayjs/plugin/customParseFormat"
import { computed } from "vue"
import ChipsInput from "@/shared/components/input/ChipsInput.vue"
import { ValidationError } from "@/shared/components/input/types"

const props = defineProps<Props>()
const emit = defineEmits<{
  "update:modelValue": [value?: string[]]
  "update:validationError": [value?: ValidationError]
  input: [value: Event]
}>()

interface Props {
  id: string
  modelValue?: string[]
  ariaLabel: string
}

const chips = computed<string[]>({
  get: () => {
    return props.modelValue
      ? props.modelValue.map((value) =>
          dayjs(value, "YYYY-MM-DD", true).format("DD.MM.YYYY"),
        )
      : []
  },

  set: (newValue: string[]) => {
    const lastValue = newValue[newValue.length - 1]

    const isValidDate = dayjs(lastValue, "DD.MM.YYYY", true).isValid()

    if (isValidDate) {
      // if valid date, check for future dates
      const isInFuture = dayjs(lastValue, "DD.MM.YYYY", true).isAfter(dayjs())
      if (isInFuture) {
        emit("update:validationError", {
          message: props.ariaLabel + " darf nicht in der Zukunft liegen",
          instance: props.id,
        })
        return
      } else {
        emit("update:validationError", undefined)
      }
    } else {
      emit("update:validationError", {
        message: "Kein valides Datum",
        instance: props.id,
      })
      return
    }

    emit(
      "update:modelValue",
      newValue.length === 0
        ? undefined
        : newValue.map((value) =>
            dayjs(value, "DD.MM.YYYY", true).format("YYYY-MM-DD"),
          ),
    )
  },
})

dayjs.extend(customParseFormat)
</script>

<template>
  <ChipsInput
    :id="id"
    v-model="chips"
    :aria-label="ariaLabel"
    maska="##.##.####"
    @update:validation-error="$emit('update:validationError', $event)"
  />
</template>
