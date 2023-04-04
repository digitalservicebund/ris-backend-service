<script lang="ts" setup>
import { ref, watch } from "vue"
import { ResponseError } from "@/services/httpClient"
import ChipsList from "@/shared/components/input/ChipsList.vue"
import { ValidationError } from "@/shared/components/input/types"
import { useInputModel } from "@/shared/composables/useInputModel"

interface Props {
  id: string
  value?: string[]
  modelValue?: string[]
  error?: ResponseError
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
const errorMessage = ref<ResponseError>()
const currentInput = ref<string>("")

const chipsList = ref<typeof ChipsList>()
const chipsInput = ref<HTMLInputElement>()

function updateModelValue() {
  emits("update:modelValue", chips.value.length === 0 ? undefined : chips.value)
}

function saveChip() {
  const trimmed = currentInput.value.trim()
  if (trimmed.length > 0) {
    if (chips.value.includes(trimmed)) {
      errorMessage.value = { title: "Schlagwort bereits vergeben." }
      currentInput.value = ""
      return
    }
    emits("addChip", trimmed)
    if (!errorMessage.value) {
      chips.value.push(trimmed)
      updateModelValue()
    }
    currentInput.value = ""
  }
}

function deleteChip(keyword: string | undefined) {
  emits("deleteChip", keyword)
}

const handleOnBlur = () => {
  errorMessage.value = undefined
  currentInput.value = ""
}

const handleTab = (event: KeyboardEvent) => {
  //only focus previous on shift + tab
  if (!event.shiftKey) {
    event.preventDefault()
    focusFirst()
  }
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
  errorMessage.value = props.error
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
      @keydown.tab="handleTab"
      @keypress.enter="saveChip"
      @keyup.right="focusFirst"
    />
    <div v-if="errorMessage" class="flex flex-row items-center">
      <span class="leading-default material-icons text-gray-900"
        >error_outline</span
      >
      <p class="label-02-reg m-4 text-gray-900">{{ errorMessage?.title }}</p>
    </div>

    <ChipsList
      ref="chipsList"
      v-model="chips"
      :error="errorMessage"
      @delete-chip="deleteChip"
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
