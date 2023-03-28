<script lang="ts" setup>
import { ref, watch } from "vue"
import ChipsList from "@/components/ChipsList.vue"
import { useInputModel } from "@/composables/useInputModel"
import { ValidationError } from "@/domain"

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
  (event: "addChip", value?: string): Promise<void>
  (event: "deleteChip", value?: string): Promise<void>
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

const focusFirst = () => {
  if (chipsList.value !== undefined && currentInput.value === "")
    chipsList.value.focusFirst()
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
  <div>
    <input
      :id="id"
      ref="chipsInput"
      v-model="currentInput"
      :aria-label="ariaLabel"
      class="input mb-[0.5rem]"
      type="text"
      @blur="handleOnBlur"
      @input="emitInputEvent"
      @keypress.enter="saveChip"
      @keyup.right="focusFirst"
    />
    <ChipsList
      ref="chipsList"
      v-model="chips"
      @previous-clicked-on-first="focusInput"
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
