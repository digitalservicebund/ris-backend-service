<script lang="ts" setup>
import { ref, onMounted } from "vue"
import { InputField, ValidationError } from "@/domain"

interface Props {
  id: string
  value?: string
  modelValue?: string[]
  ariaLabel: string
  placeholder?: string
  subField?: InputField
  validationError?: ValidationError
}

interface Emits {
  (event: "update:modelValue", value?: string[]): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const chips = ref<string[]>(props.modelValue ?? [])
const currentInput = ref<string>("")
const focusedItemIndex = ref<number>()
const containerRef = ref<HTMLElement>()

function updateModelValue() {
  emit("update:modelValue", chips.value.length === 0 ? undefined : chips.value)
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
  chips.value.splice(index, 1)
  updateModelValue()
}

function resetFocus() {
  focusedItemIndex.value = undefined
}

function backspaceDelete() {
  if (currentInput.value === "") {
    chips.value.splice(chips.value.length - 1)
    updateModelValue()
  }
}

function enterDelete() {
  if (focusedItemIndex.value !== undefined) {
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
  focusedItemIndex.value =
    focusedItemIndex.value === undefined || focusedItemIndex.value === 0
      ? chips.value.length - 1
      : (focusedItemIndex.value -= 1)
  const prev = containerRef.value?.children[
    focusedItemIndex.value
  ] as HTMLElement
  if (prev) prev.focus()
}

const focusNext = () => {
  focusedItemIndex.value =
    focusedItemIndex.value === undefined ||
    focusedItemIndex.value === chips.value.length - 1
      ? 0
      : (focusedItemIndex.value += 1)
  const next = containerRef.value?.children[
    focusedItemIndex.value
  ] as HTMLElement
  if (next) next.focus()
}

onMounted(() => {
  document.addEventListener("keydown", (e) => {
    if (
      e.key === "ArrowUp" ||
      e.key === "ArrowDown" ||
      e.key === "ArrowLeft" ||
      e.key === "ArrowRight"
    ) {
      e.preventDefault()
    }
  })
})
</script>

<template>
  <div class="bg-white input">
    <div ref="containerRef" class="flex flex-row flex-wrap" tabindex="-1">
      <div
        v-for="(chip, i) in chips"
        :key="i"
        class="bg-blue-500 body-01-reg chip"
        tabindex="0"
        @keypress.enter="enterDelete"
        @keyup.left="focusPrevious"
        @keyup.right="focusNext"
      >
        <div class="label-wrapper">{{ chip }}</div>

        <div class="icon-wrapper">
          <i
            class="material-icons"
            @click="deleteChip(i)"
            @keydown.enter="deleteChip(i)"
            >clear</i
          >
        </div>
      </div>
    </div>

    <input
      :id="id"
      v-model="currentInput"
      :aria-label="ariaLabel"
      type="text"
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

  .chip {
    display: flex;
    align-items: center;
    border-radius: 10px;
    margin: 0 8px 8px 0;

    .icon-wrapper {
      display: flex;
      padding: 4px 3px;
      border-radius: 0 10px 10px 0;

      i {
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

        i {
          color: white;
        }
      }
    }
  }

  input {
    width: 30px;
    flex: 1 1 auto;
    border: none;
    margin-bottom: 8px;
    outline: none;
  }
}
</style>
