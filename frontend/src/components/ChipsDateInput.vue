<script lang="ts" setup>
import dayjs from "dayjs"
import { ref, onMounted, watch, computed } from "vue"
import { ValidationError } from "@/domain"

const props = defineProps<Props>()
const emits = defineEmits<Emits>()

interface Props {
  id: string
  value?: string[]
  modelValue?: string[]
  ariaLabel: string
  placeholder?: string
  isFutureDate?: boolean
  validationError?: ValidationError
}

interface Emits {
  (event: "update:modelValue", value?: string[]): void
  (event: "update:validationError", value?: ValidationError): void
  (event: "input", value: Event): void
}

const chips = ref<string[]>(props.modelValue ?? [])
const currentInput = ref<string | undefined>(undefined)
const currentInputField = ref<HTMLInputElement>()
const focusedItemIndex = ref<number>()
const containerRef = ref<HTMLElement>()

watch(
  props,
  () =>
    (chips.value = props.modelValue
      ? props.modelValue.map((value) => dayjs(value).format("YYYY-MM-DD"))
      : []),
  {
    immediate: true,
  }
)

function updateModelValue() {
  emits(
    "update:modelValue",
    chips.value.length === 0
      ? undefined
      : chips.value.map((value) => dayjs(value).toISOString())
  )
}

function saveChip() {
  if (!hasError.value && currentInput.value && currentInput.value.length > 0) {
    chips.value.push(currentInput.value)
    updateModelValue()
    currentInput.value = undefined
    resetFocus()
  }
}

function deleteChip(index: number) {
  currentInput.value = undefined
  chips.value.splice(index, 1)
  updateModelValue()
  resetFocus()
}

function resetFocus() {
  currentInputField.value?.blur()
  focusedItemIndex.value = undefined
  currentInputField.value?.focus()
}

function backspaceDelete() {
  if (currentInput.value === undefined) {
    chips.value.splice(chips.value.length - 1)
    updateModelValue()
    resetFocus()
  }
}

function enterDelete() {
  if (focusedItemIndex.value !== undefined) {
    currentInput.value = undefined
    chips.value.splice(focusedItemIndex.value, 1)
    // bring focus on second last item if last item was deleted
    if (focusedItemIndex.value === chips.value.length) {
      focusPrevious()
    }
    if (focusedItemIndex.value === -1) {
      resetFocus()
    }
  }

  updateModelValue()
}

const focusPrevious = () => {
  if (
    (currentInput.value && currentInput.value.length > 0) ||
    focusedItemIndex.value === 0
  ) {
    return
  }
  focusedItemIndex.value =
    focusedItemIndex.value === undefined
      ? chips.value.length - 1
      : focusedItemIndex.value - 1
  const prev = containerRef.value?.children[
    focusedItemIndex.value
  ] as HTMLElement
  if (prev) prev.focus()
}

const focusNext = () => {
  if (
    (currentInput.value && currentInput.value.length > 0) ||
    focusedItemIndex.value === undefined
  ) {
    return
  }
  if (focusedItemIndex.value == chips.value.length - 1) {
    resetFocus()
    return
  }
  focusedItemIndex.value =
    focusedItemIndex.value === undefined ? 0 : focusedItemIndex.value + 1
  const next = containerRef.value?.children[
    focusedItemIndex.value
  ] as HTMLElement
  if (next) next.focus()
}

const setFocusedItemIndex = (index: number) => {
  focusedItemIndex.value = index
}

function handleOnBlur() {
  currentInput.value = undefined
}

const isInPast = computed(() => {
  if (currentInput.value) {
    const date = new Date(currentInput.value)
    const today = new Date()
    return date < today
  } else return true
})

const hasError = computed(
  () =>
    props.validationError ||
    (!isInPast.value && !props.isFutureDate) ||
    currentInput.value == ""
)

watch(hasError, () => {
  hasError.value
    ? !isInPast.value && !props.isFutureDate
      ? emits("update:validationError", {
          defaultMessage:
            "Das Entscheidungsdatum darf nicht in der Zukunft liegen",
          field: props.id,
        })
      : emits("update:validationError", {
          defaultMessage: "Entscheidungsdatum ist kein valides Datum",
          field: props.id,
        })
    : emits("update:validationError", undefined)
})

const conditionalClasses = computed(() => ({
  input__error: props.validationError || hasError.value,
}))

onMounted(() => {
  document.addEventListener("keydown", (e) => {
    if (e.key === "ArrowUp" || e.key === "ArrowDown") {
      e.preventDefault()
    }
  })
})
</script>

<template>
  <div class="bg-white input" :class="conditionalClasses">
    <div ref="containerRef" class="flex flex-row flex-wrap" tabindex="-1">
      <div
        v-for="(chip, i) in chips"
        :key="i"
        aria-label="chip"
        class="bg-blue-500 body-01-reg chip"
        tabindex="0"
        @click="setFocusedItemIndex(i)"
        @keydown.delete="backspaceDelete"
        @keypress.enter="enterDelete"
        @keyup.left="focusPrevious"
        @keyup.right="focusNext"
      >
        <div class="label-wrapper">{{ dayjs(chip).format("DD.MM.YYYY") }}</div>

        <div class="icon-wrapper">
          <em
            aria-Label="Löschen"
            class="material-icons"
            @click="deleteChip(i)"
            @keydown.enter="deleteChip(i)"
            >clear</em
          >
        </div>
      </div>
    </div>

    <input
      :id="id"
      ref="currentInputField"
      v-model="currentInput"
      :aria-label="ariaLabel"
      :class="conditionalClasses"
      max="9999-12-31"
      min="1000-01-01"
      type="date"
      @blur="handleOnBlur"
      @keydown.delete="backspaceDelete"
      @keypress.enter="saveChip"
      @keyup.left="focusPrevious"
      @keyup.right="focusNext"
    />
  </div>
</template>

<style lang="scss" scoped>
.input {
  display: flex;
  width: 100%;
  min-height: 3.75rem;
  flex-wrap: wrap;
  align-content: space-between;
  padding: 12px 16px 4px;
  @apply border-2 border-solid border-blue-800 uppercase;

  &:focus {
    outline: none;
  }

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

  .chip {
    display: flex;
    align-items: center;
    border-radius: 10px;
    margin: 0 8px 8px 0;

    .icon-wrapper {
      display: flex;
      padding: 4px 3px;
      border-radius: 0 10px 10px 0;

      em {
        cursor: pointer;
      }
    }

    .label-wrapper {
      display: flex;
      padding: 3px 0 3px 8px;
      margin-right: 8px;
    }

    &:focus {
      outline: none;

      .icon-wrapper {
        @apply bg-blue-900;

        em {
          color: white;
        }
      }
    }
  }

  input {
    min-width: 8.74rem;
    flex: 1 1 auto;
    border: none;
    margin-bottom: 8px;
    outline: none;
    text-transform: uppercase;
  }
}
</style>
