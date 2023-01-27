<script lang="ts" setup>
import dayjs from "dayjs"
import { computed, ref, watch } from "vue"
import { ValidationError } from "@/domain"

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
const inputValue = ref<string>()

watch(
  props,
  () =>
    (inputValue.value = props.modelValue
      ? dayjs(props.modelValue).format("YYYY-MM-DD")
      : props.value),
  {
    immediate: true,
  }
)

const isInPast = computed(() => {
  if (inputValue.value && inputValue.value !== "") {
    const date = new Date(inputValue.value)
    const today = new Date()
    return date < today
  } else return true
})

const hasError = computed(
  () =>
    props.validationError ||
    (!isInPast.value && !props.isFutureDate) ||
    inputValue.value == ""
)

watch(
  inputValue,
  () => {
    if (hasError.value) {
      if (inputValue.value == "") {
        emit("update:validationError", {
          defaultMessage: "Kein valides Datum",
          field: props.id,
        })
      }
    } else {
      emit("update:validationError", undefined)
    }
  },
  { immediate: true }
)

watch(
  isInPast,
  () => {
    if (hasError.value) {
      if (!isInPast.value && !props.isFutureDate) {
        emit("update:validationError", {
          defaultMessage:
            "Das " + props.ariaLabel + " darf nicht in der Zukunft liegen",
          field: props.id,
        })
      }
    } else emit("update:validationError", undefined)
  },
  { immediate: true }
)

const conditionalClasses = computed(() => ({
  input__error: props.validationError || hasError.value,
}))

function handleOnBlur() {
  if (!hasError.value)
    emit("update:modelValue", dayjs(inputValue.value).toISOString())
  // TODO support clearing date and sending undefined to backend
  // empty field should not be an error
}
</script>

<template>
  <input
    :id="id"
    v-model="inputValue"
    :aria-label="ariaLabel"
    class="bg-white border-2 border-blue-800 focus:outline-2 h-[3.75rem] hover:outline-2 input outline-0 outline-blue-800 outline-none outline-offset-[-4px] px-16 uppercase w-full"
    :class="conditionalClasses"
    max="9999-12-31"
    min="1000-01-01"
    type="date"
    @blur="handleOnBlur"
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
    padding: 17px 24px;
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
