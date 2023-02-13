<script lang="ts" setup>
import { ref, computed } from "vue"
import { useInputModel } from "@/composables/useInputModel"
import { ValidationError } from "@/domain"

interface Props {
  id: string
  value?: string[]
  modelValue?: string[]
  inLine?: boolean
  ariaLabel: string
  placeholder?: string
  validationError?: ValidationError
}

interface Emits {
  (event: "update:modelValue", value?: string[]): void
  (event: "input", value: Event): void
}

const props = defineProps<Props>()
const emits = defineEmits<Emits>()

const { emitInputEvent } = useInputModel<string[], Props, Emits>(props, emits)
const chips = ref<string[]>(props.modelValue ?? [])
const currentInput = ref<string>("")
const currentInputField = ref<HTMLInputElement>()
const focusedItemIndex = ref<number>()
const containerRef = ref<HTMLElement>()
const conditionalClasses = computed(() => ({
  "flex-col": !props.inLine,
}))

function updateModelValue() {
  emits("update:modelValue", chips.value.length === 0 ? undefined : chips.value)
}

function saveChip() {
  const trimmed = currentInput.value.trim()
  if (trimmed.length > 0) {
    chips.value.push(trimmed)
    updateModelValue()
    currentInput.value = ""
  }
}

function deleteChip(index: number) {
  currentInput.value = ""
  chips.value.splice(index, 1)
  updateModelValue()
  resetFocus()
}

function resetFocus() {
  focusedItemIndex.value = undefined
  currentInputField.value?.focus()
}

function backspaceDelete() {
  if (currentInput.value === "") {
    chips.value.splice(chips.value.length - 1)
    updateModelValue()
    resetFocus()
  }
}

function enterDelete() {
  if (focusedItemIndex.value !== undefined) {
    currentInput.value = ""
    chips.value.splice(focusedItemIndex.value, 1)
    // bring focus on second last item if last item was deleted
    if (focusedItemIndex.value === chips.value.length) {
      focusPrevious()
    }
    if (focusedItemIndex.value === 0) {
      resetFocus()
    }
  }

  updateModelValue()
}

const focusPrevious = () => {
  if (currentInput.value.length > 0 || focusedItemIndex.value === 0) {
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
  if (currentInput.value.length > 0 || focusedItemIndex.value === undefined) {
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

const handleOnBlur = () => {
  currentInput.value = ""
}
</script>

<template>
  <div>
    <input
      :id="id"
      ref="currentInputField"
      v-model="currentInput"
      :aria-label="ariaLabel"
      class="bg-white input"
      type="text"
      @blur="handleOnBlur"
      @input="emitInputEvent"
      @keydown.delete="backspaceDelete"
      @keypress.enter="saveChip"
      @keyup.left="focusPrevious"
      @keyup.right="focusNext"
    />
    <div
      ref="containerRef"
      class="flex flex-wrap"
      :class="conditionalClasses"
      tabindex="-1"
    >
      <div
        v-for="(chip, i) in chips"
        :key="i"
        aria-label="chip"
        class="bg-blue-500 body-01-reg chip"
        tabindex="0"
        @click="setFocusedItemIndex(i)"
        @input="emitInputEvent"
        @keydown.delete="backspaceDelete"
        @keypress.enter="enterDelete"
        @keyup.left="focusPrevious"
        @keyup.right="focusNext"
      >
        <div class="label-wrapper">{{ chip }}</div>

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
  </div>
</template>

<style lang="scss" scoped>
.input {
  display: flex;
  width: 100%;
  min-height: 3.75rem;
  flex-wrap: wrap;
  align-content: space-between;
  padding: 12px 16px;
  margin-bottom: 8px;
  @apply border-2 border-solid border-blue-800;

  &:focus {
    outline: none;
  }

  &:autofill {
    @apply shadow-white text-inherit;
  }

  &:autofill:focus {
    @apply shadow-white text-inherit;
  }

  input {
    width: 30px;
    flex: 1 1 auto;
    border: none;
    outline: none;
  }
}

.chip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-radius: 10px;
  margin: 4px;
  word-break: break-word;

  .icon-wrapper {
    display: flex;
    height: 100%;
    align-items: center;
    padding: 4px 3px;
    border-radius: 0 10px 10px 0;

    em {
      cursor: pointer;
      text-align: center;
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
</style>
