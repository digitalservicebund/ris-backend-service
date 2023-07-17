<script lang="ts" setup>
import dayjs from "dayjs"
import customParseFormat from "dayjs/plugin/customParseFormat"
import { vMaska, MaskaDetail } from "maska"
import { computed, ref, watch } from "vue"
import { ValidationError } from "@/shared/components/input/types"

interface Props {
  id: string
  value?: string
  modelValue?: string
  ariaLabel: string
  isFutureDate?: boolean
  hasError?: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  "update:modelValue": [value?: string]
  "update:validationError": [value?: ValidationError]
}>()

const inputCompleted = ref<boolean>(false)
const inputValue = ref(
  props.modelValue ? dayjs(props.modelValue).format("DD.MM.YYYY") : undefined,
)

dayjs.extend(customParseFormat)

const isValidDate = computed(() => {
  return dayjs(inputValue.value, "DD.MM.YYYY", true).isValid()
})

const isInPast = computed(() => {
  if (props.isFutureDate) return true
  return dayjs(inputValue.value, "DD.MM.YYYY", true).isBefore(dayjs())
})

const onMaska = (event: CustomEvent<MaskaDetail>) => {
  inputCompleted.value = event.detail.completed
}

const effectiveHasError = computed(
  () =>
    props.hasError ||
    (inputCompleted.value && !isInPast.value && !props.isFutureDate) ||
    (inputCompleted.value && !isValidDate.value),
)

const conditionalClasses = computed(() => ({
  "has-error placeholder-black": props.hasError || effectiveHasError.value,
}))

function validateInput() {
  if (inputCompleted.value) {
    if (isValidDate.value) {
      // if valid date, check for future dates
      if (!isInPast.value && !props.isFutureDate && isValidDate.value)
        emit("update:validationError", {
          defaultMessage:
            "Das " + props.ariaLabel + " darf nicht in der Zukunft liegen",
          field: props.id,
        })
      else emit("update:validationError", undefined)
    } else {
      emit("update:validationError", {
        defaultMessage: "Kein valides Datum",
        field: props.id,
      })
    }
  } else {
    if (inputValue.value) {
      emit("update:validationError", {
        defaultMessage: "Unvollständiges Datum",
        field: props.id,
      })
    } else {
      emit("update:validationError", undefined)
    }
  }
}

function backspaceDelete() {
  emit("update:validationError", undefined)
  emit("update:modelValue", undefined)
  inputValue.value = undefined
}

function onBlur() {
  validateInput()
}

watch(props, () => {
  inputValue.value = props.modelValue
    ? dayjs(props.modelValue).format("DD.MM.YYYY")
    : undefined
})

watch(inputValue, () => {
  isValidDate.value &&
    isInPast.value &&
    emit(
      "update:modelValue",
      dayjs(inputValue.value, "DD.MM.YYYY").toISOString(),
    )
})

watch(inputCompleted, () => {
  validateInput()
})
</script>

<template>
  <input
    :id="id"
    v-model="inputValue"
    v-maska
    :aria-label="ariaLabel"
    class="ds-input"
    :class="conditionalClasses"
    data-maska="##.##.####"
    placeholder="TT.MM.JJJJ"
    @blur="onBlur"
    @keydown.delete="backspaceDelete"
    @maska="onMaska"
  />
</template>
