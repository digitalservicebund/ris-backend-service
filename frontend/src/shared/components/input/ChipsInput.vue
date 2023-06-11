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

function saveChip(event: Event) {
  const trimmed = currentInput.value.trim()
  if (trimmed.length > 0) {
    event.stopPropagation()
    chips.value.push(trimmed)
    updateModelValue()
    currentInput.value = ""
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
  <div
    class="-outline-offset-4 [&:has(:focus)]:outline autofill:focus:shadow-white autofill:focus:text-inherit autofill:shadow-white autofill:text-inherit bg-white border-2 border-blue-800 border-solid content-between flex flex-wrap hover:outline min-h-[3.75rem] outline-2 outline-blue-800 px-16 py-8 w-full"
  >
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
      class="bg-transparent border-none flex-auto outline-none p-4 w-32"
      type="text"
      @input="emitInputEvent"
      @keypress.enter="saveChip"
      @keyup.left="focusPrevious"
    />
  </div>
</template>
