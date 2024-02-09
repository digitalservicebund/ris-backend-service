<script lang="ts" setup>
import dayjs from "dayjs"
import customParseFormat from "dayjs/plugin/customParseFormat"
import { computed } from "vue"
import ChipsInput from "@/components/input/ChipsInput.vue"
import { ValidationError } from "@/components/input/types"

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
    if (!newValue || newValue.length === 0) {
      emit("update:modelValue", undefined)
      return
    }

    const lastValue = newValue.at(-1)
    const lastDate = dayjs(lastValue, "DD.MM.YYYY", true)

    if (!lastDate.isValid()) {
      emit("update:validationError", {
        message: "Kein valides Datum",
        instance: props.id,
      })
      return
    }

    // if valid date, check for future dates
    const isInFuture = lastDate.isAfter(dayjs())
    if (isInFuture) {
      emit("update:validationError", {
        message: props.ariaLabel + " darf nicht in der Zukunft liegen",
        instance: props.id,
      })
      return
    }
    emit("update:validationError", undefined)

    emit(
      "update:modelValue",
      newValue.map((value) =>
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
