<script lang="ts" setup>
import dayjs from "dayjs"
import customParseFormat from "dayjs/plugin/customParseFormat"
import { vMaska } from "maska"
import { computed } from "vue"
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

dayjs.extend(customParseFormat)

const inputValue = computed({
  get: () =>
    props.modelValue ? dayjs(props.modelValue).format("DD.MM.YYYY") : undefined,
  set: (newValue) => {
    isValidDate(newValue)
      ? emit("update:modelValue", dayjs(newValue, "DD.MM.YYYY").toISOString())
      : emit("update:modelValue", undefined)
  },
})

function isValidDate(date: string | undefined) {
  return dayjs(date, "DD.MM.YYYY", true).isValid()
}

const conditionalClasses = computed(() => ({
  input__error: props.validationError,
}))

function backspaceDelete() {
  emit("update:modelValue", undefined)
}

function onBlur() {
  //todo
}
</script>

<template>
  <!-- Maska -->
  <input
    :id="id"
    v-model="inputValue"
    v-maska
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
