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

const hasError = computed(
  () =>
    props.validationError ||
    (inputCompleted.value && !isValidDate(inputValue.value))
)

dayjs.extend(customParseFormat)

const options = {
  onMaska: (input: MaskaDetail) => {
    inputCompleted.value = input.completed
  },
}

const conditionalClasses = computed(() => ({
  input__error: props.validationError || hasError.value,
}))

function isValidDate(date: string | undefined) {
  return dayjs(date, "DD.MM.YYYY", true).isValid()
}

function backspaceDelete() {
  emit("update:modelValue", undefined)
  emit("update:validationError", undefined)
}

function onBlur() {
  !isValidDate(inputValue.value)
    ? (emit("update:validationError", {
        defaultMessage: "Kein valides Datum",
        field: props.id,
      }),
      emit("update:modelValue", undefined))
    : emit("update:validationError", undefined)
}

watch(inputValue, (is) => {
  isValidDate(is) &&
    emit("update:modelValue", dayjs(is, "DD.MM.YYYY").toISOString())
})

watch(inputCompleted, (is) => {
  if (is) {
    !isValidDate(inputValue.value)
      ? emit("update:validationError", {
          defaultMessage: "Kein valides Datum",
          field: props.id,
        })
      : emit("update:validationError", undefined)
  } else {
    emit("update:validationError", undefined)
  }
})

watch(props, () => {
  inputValue.value = props.modelValue
    ? dayjs(props.modelValue).format("DD.MM.YYYY")
    : undefined
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
