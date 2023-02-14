<script lang="ts" setup>
import dayjs from "dayjs"
import customParseFormat from "dayjs/plugin/customParseFormat"
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

dayjs.extend(customParseFormat)
const dayValue = ref<string>()
const monthValue = ref<string>()
const yearValue = ref<string>()

const ariaLabelDay = computed(() => props.ariaLabel + " Tag")
const ariaLabelMonth = computed(() => props.ariaLabel + " Monat")
const ariaLabelYear = computed(() => props.ariaLabel + " Jahr")

const fullDate = computed(() =>
  yearValue.value && monthValue.value && dayValue.value ? true : false
)
const dateValue = computed(() =>
  fullDate.value
    ? yearValue.value + "-" + monthValue.value + "-" + dayValue.value
    : null
)

const isInPast = ref(true)
const isValidDate = ref(true)

function checkValidDate() {
  if (dateValue.value)
    isValidDate.value = dayjs(dateValue.value, "YYYY-MM-DD", true).isValid()
}

function checkIsInPast() {
  if (dateValue.value && isValidDate.value) {
    const date = new Date(dateValue.value)
    const today = new Date()
    isInPast.value = date < today
  } else isInPast.value = true
}

const hasError = computed(
  () =>
    props.validationError ||
    (!isInPast.value && !props.isFutureDate) ||
    !isValidDate.value
)

const conditionalClasses = computed(() => ({
  input__error: props.validationError || hasError.value,
}))

watch(
  props,
  () => {
    if (props.modelValue && !dateValue.value) {
      const formattedModelValue = dayjs(props.modelValue).format("YYYY-MM-DD")
      const splitDate = formattedModelValue.split("-")
      dayValue.value = splitDate[2]
      monthValue.value = splitDate[1]
      yearValue.value = splitDate[0]
    }
  },
  {
    immediate: true,
  }
)

function handleInput(event: Event) {
  const target = event.target as HTMLInputElement
  if (target.value.length >= target.maxLength) {
    if (!target) return
    let next = target
    while ((next = next.nextElementSibling as HTMLInputElement)) {
      if (next == null) break
      if (next.tagName.toLowerCase() == "input") {
        next.focus()
        break
      }
    }
  }
}

function deleteDay() {
  dayValue.value = undefined
}

function deleteMonth() {
  monthValue.value = undefined
}

function deleteYear() {
  yearValue.value = undefined
}

function selectAll(event: Event) {
  ;(event.target as HTMLInputElement).select()
}

watch(
  isValidDate,
  () => {
    if (hasError.value) {
      emit("update:validationError", {
        defaultMessage: "Kein valides Datum",
        field: props.id,
      })
    } else {
      emit("update:validationError", undefined)
    }
  },
  { immediate: true }
)

watch(
  isInPast,
  () => {
    if (!isInPast.value && !props.isFutureDate) {
      emit("update:validationError", {
        defaultMessage:
          "Das " + props.ariaLabel + " darf nicht in der Zukunft liegen",
        field: props.id,
      })
    } else emit("update:validationError", undefined)
  },
  { immediate: true }
)

function handleOnBlur() {
  checkValidDate()
  checkIsInPast()
  if (!hasError.value && dateValue.value) {
    emit("update:modelValue", dayjs(dateValue.value).toISOString())
  }
}
</script>

<template>
  <div
    :ariaLabel="ariaLabel"
    class="bg-white border-2 border-blue-800 flex flex-row focus:outline-2 h-[3.75rem] hover:outline-2 input items-center outline-0 outline-blue-800 outline-none outline-offset-[-4px] px-16 uppercase w-full"
    :class="conditionalClasses"
    tabindex="0"
    @input="handleInput"
  >
    <input
      :id="id"
      v-model="dayValue"
      :aria-label="ariaLabelDay"
      class="focus:outline-none w-20"
      max="31"
      maxLength="2"
      min="0"
      minLength="2"
      name="day"
      pattern="[0-9]*"
      placeholder="TT"
      type="text"
      @blur="handleOnBlur"
      @focus="selectAll"
      @keydown.delete="deleteDay"
    />
    <span class="mr-2">.</span>
    <input
      :id="id"
      v-model="monthValue"
      :aria-label="ariaLabelMonth"
      class="focus:outline-none w-20"
      maxLength="2"
      minLength="2"
      name="month"
      pattern="[0-9]*"
      placeholder="MM"
      type="text"
      @blur="handleOnBlur"
      @focus="selectAll($event)"
      @keydown.delete="deleteMonth"
    />
    <span class="mr-2">.</span>
    <input
      :id="id"
      v-model="yearValue"
      :aria-label="ariaLabelYear"
      class="focus:outline-none w-40"
      maxLength="4"
      minLength="0"
      pattern="[0-9]*"
      placeholder="JJJJ"
      type="text"
      @blur="handleOnBlur"
      @focus="selectAll($event)"
      @keydown.delete="deleteYear"
    />
  </div>
</template>

<style lang="scss" scoped>
input::-webkit-outer-spin-button,
input::-webkit-inner-spin-button {
  appearance: none;
}

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

    input {
      @apply bg-red-200;
    }
  }
}
</style>
