<script lang="ts" setup>
import { ref, watch } from "vue"
import ChipsList from "@/shared/components/input/ChipsList.vue"
import { ValidationError } from "@/shared/components/input/types"
import { useInputModel } from "@/shared/composables/useInputModel"

interface Props {
  id: string
  value?: string[]
  modelValue?: string[]
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

const chipsList = ref<typeof ChipsList>()
const chipsInput = ref<HTMLInputElement>()

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

const handleOnBlur = () => {
  currentInput.value = ""
}

const handleTab = (event: KeyboardEvent) => {
  //only focus previous on shift + tab
  if (event.shiftKey) {
    event.preventDefault()
    focusPrevious()
  }
}

const focusPrevious = () => {
  if (chipsList.value !== undefined && currentInput.value === "")
    chipsList.value.focusPrevious()
}

const focusInput = () => {
  if (chipsList.value !== undefined) chipsList.value.resetFocus()
  if (chipsInput.value !== undefined) chipsInput.value.focus()
}

watch(props, () => {
  if (props.modelValue) chips.value = props.modelValue
})

watch(chips, () => {
  if (chips.value === undefined) focusInput()
})
</script>

<template>
  <div class="input">
    <ChipsList
      ref="chipsList"
      v-model="chips"
      @next-clicked-on-last="focusInput"
    />
    <input
      :id="id"
      ref="chipsInput"
      v-model="currentInput"
      :aria-label="ariaLabel"
      type="text"
      @blur="handleOnBlur"
      @input="emitInputEvent"
      @keydown.tab="handleTab"
      @keypress.enter="saveChip"
      @keyup.left="focusPrevious"
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
  padding: 8px 16px;
  background-color: white;
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
    padding: 4px;
    border: none;
    outline: none;
  }
}
</style>
