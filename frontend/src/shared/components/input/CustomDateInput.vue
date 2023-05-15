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
  validationError?: ValidationError
}

interface Emits {
  (event: "update:modelValue", value?: string): void
  (event: "update:validationError", value?: ValidationError): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const inputCompleted = ref<boolean>(false)
const inputValue = ref(
  props.modelValue ? dayjs(props.modelValue).format("DD.MM.YYYY") : undefined
)

dayjs.extend(customParseFormat)

// eslint-disable @typescript-eslint/no-unused-vars
const options = {
  onMaska: (input: MaskaDetail) => {
    inputCompleted.value = input.completed
  },
}

const isValidDate = computed(() => {
  return dayjs(inputValue.value, "DD.MM.YYYY", true).isValid()
})

const isInPast = computed(() => {
  if (props.isFutureDate) return true
  return dayjs(inputValue.value, "DD.MM.YYYY", true).isBefore(dayjs())
})

const hasError = computed(
  () =>
    props.validationError ||
    (inputCompleted.value && !isInPast.value && !props.isFutureDate) ||
    (inputCompleted.value && !isValidDate.value)
)

const conditionalClasses = computed(() => ({
  input__error: props.validationError || hasError.value,
}))

function validateInput() {
  if (inputCompleted.value) {
    //check for valid dates
    !isValidDate.value
      ? emit("update:validationError", {
          defaultMessage: "Kein valides Datum",
          field: props.id,
        })
      : // if valid date, check for future dates
      !isInPast.value && !props.isFutureDate && isValidDate.value
      ? emit("update:validationError", {
          defaultMessage:
            "Das " + props.ariaLabel + " darf nicht in der Zukunft liegen",
          field: props.id,
        })
      : emit("update:validationError", undefined)
  } else {
    emit("update:validationError", undefined)
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
      dayjs(inputValue.value, "DD.MM.YYYY").toISOString()
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
    v-maska:[options]
    :aria-label="ariaLabel"
    class="bg-white border-2 border-blue-800 focus:outline-2 h-[3.75rem] hover:outline-2 input outline-0 outline-blue-800 outline-none outline-offset-[-4px] px-16 uppercase w-full"
    :class="conditionalClasses"
    data-maska="##.##.####"
    placeholder="DD.MM.YYYY"
    @blur="onBlur"
    @keydown.delete="backspaceDelete"
  />
</template>

<style lang="scss" scoped>
.input {
  &:autofill {
    @apply shadow-white text-inherit;
  }

  &:autofill:focus {
    @apply shadow-white text-inherit;
  }

  &__error {
    width: 100%;
    @apply border-red-800 bg-red-200;

    &:autofill {
      @apply shadow-error text-inherit;
    }

    &:autofill:focus {
      @apply shadow-error text-inherit;
    }
  }
}
</style>
