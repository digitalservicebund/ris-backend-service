<script lang="ts" setup>
import { ref, watch } from "vue"
import { ResponseError } from "@/services/httpClient"
import { useInputModel } from "@/shared/composables/useInputModel"

interface Props {
  modelValue?: string[]
  error?: ResponseError
}

interface Emits {
  (event: "update:modelValue", value?: string[]): void
  (event: "previousClickedOnFirst"): void
  (event: "nextClickedOnLast"): void
  (event: "deleteChip", value?: string): void
  (event: "input", value: Event): void
}

const props = defineProps<Props>()
const emits = defineEmits<Emits>()

const { emitInputEvent } = useInputModel<string[], Props, Emits>(props, emits)
const chips = ref<string[]>(props.modelValue ?? [])
const errorMessage = ref<ResponseError>()
const focusedItemIndex = ref<number>()
const containerRef = ref<HTMLElement>()

function updateModelValue() {
  emits("update:modelValue", chips.value)
}

function deleteChip(index: number) {
  emits("deleteChip", chips.value[index])
  chips.value.splice(index, 1)
  updateModelValue()
  resetFocus()
}

function resetFocus() {
  focusedItemIndex.value = undefined
}

function enterDelete() {
  if (focusedItemIndex.value !== undefined) {
    emits("deleteChip", chips.value[focusedItemIndex.value])
    chips.value.splice(focusedItemIndex.value, 1)
    // bring focus on second last item if last item was deleted
    if (focusedItemIndex.value === chips.value.length) {
      focusPrevious()
    }
  }
  updateModelValue()
}

const focusFirst = () => {
  focusedItemIndex.value = 0
}

const focusPrevious = () => {
  if (focusedItemIndex.value === 0) {
    emits("previousClickedOnFirst")
    return
  }
  focusedItemIndex.value =
    focusedItemIndex.value === undefined
      ? chips.value.length - 1
      : focusedItemIndex.value - 1
}

const focusNext = () => {
  if (focusedItemIndex.value === undefined) {
    return
  }
  if (focusedItemIndex.value == chips.value.length - 1) {
    emits("nextClickedOnLast")
    return
  }
  focusedItemIndex.value =
    focusedItemIndex.value === undefined ? 0 : focusedItemIndex.value + 1
}

const setFocusedItemIndex = (index: number) => {
  focusedItemIndex.value = index
}

watch(props, () => {
  if (props.modelValue) chips.value = props.modelValue
  errorMessage.value = props.error
})

watch(focusedItemIndex, () => {
  if (focusedItemIndex.value !== undefined) {
    const item = containerRef.value?.children[
      focusedItemIndex.value
    ] as HTMLElement
    if (item) item.focus()
  }
})

defineExpose({ focusPrevious, focusNext, resetFocus, focusFirst })
</script>

<template>
  <div>
    <ul
      ref="containerRef"
      class="my-4 mr-8 flex flex-row flex-wrap items-center gap-8 empty:m-0"
    >
      <li
        v-for="(chip, i) in chips"
        :key="i"
        class="group ds-body-01-reg relative flex items-center break-words rounded-10 bg-blue-500 pr-32 outline-none"
        data-testid="chip"
        tabindex="0"
        @click.stop="setFocusedItemIndex(i)"
        @focus="setFocusedItemIndex(i)"
        @input="emitInputEvent"
        @keypress.enter.stop="enterDelete"
        @keyup.left="focusPrevious"
        @keyup.right="focusNext"
      >
        <div
          class="flex whitespace-pre-wrap px-6 py-4 leading-24"
          data-testid="chip-value"
        >
          {{ chip }}
        </div>

        <div
          class="iems-center absolute inset-y-0 right-0 flex h-full items-center rounded-r-10 p-4 group-focus:bg-blue-900 group-focus:text-white"
        >
          <em
            aria-Label="Löschen"
            class="material-icons cursor-pointer text-center"
            @click="deleteChip(i)"
            @keydown.enter="deleteChip(i)"
            >clear</em
          >
        </div>
      </li>
    </ul>
  </div>
</template>
