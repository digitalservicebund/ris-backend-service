<script lang="ts" setup>
import dayjs from "dayjs"
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
const inputValue = ref<string>()

const options = {
  onMaska: (input: MaskaDetail) => {
    input.completed &&
      emit("update:modelValue", dayjs(inputValue.value).toISOString())
  },
}

watch(
  props,
  () => {
    //TODO formatting with DD.MM.YYYY back and forth causes recursive loop
    inputValue.value = props.modelValue
      ? dayjs(props.modelValue).format("YYYY-MM-DD")
      : undefined
  },
  {
    immediate: true,
  }
)

const conditionalClasses = computed(() => ({
  input__error: props.validationError,
}))

function backspaceDelete() {
  emit("update:modelValue", undefined)
  emit("update:validationError", undefined)
  inputValue.value = undefined
}
</script>

<template>
  <!-- Maska -->
  <input
    :id="id"
    v-model="inputValue"
    v-maska:[options]
    :aria-label="ariaLabel"
    class="bg-white border-2 border-blue-800 focus:outline-2 h-[3.75rem] hover:outline-2 input outline-0 outline-blue-800 outline-none outline-offset-[-4px] px-16 uppercase w-full"
    :class="conditionalClasses"
    data-maska="####.##.##"
    placeholder="YYYY.MM.DD"
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
