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
  (event: "deleteChip", value?: string): Promise<void>
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

async function deleteChip(index: number) {
  await emits("deleteChip", chips.value[index])
  if (!errorMessage.value) {
    chips.value.splice(index, 1)
    updateModelValue()
  }
  resetFocus()
}

function resetFocus() {
  focusedItemIndex.value = undefined
}

async function enterDelete() {
  if (focusedItemIndex.value !== undefined) {
    await emits("deleteChip", chips.value[focusedItemIndex.value])
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
    <div
      ref="containerRef"
      class="flex flex-row flex-wrap items-center"
      tabindex="-1"
    >
      <div
        v-for="(chip, i) in chips"
        :key="i"
        aria-label="chip"
        class="bg-blue-500 body-01-reg chip"
        tabindex="0"
        @click="setFocusedItemIndex(i)"
        @focus="setFocusedItemIndex(i)"
        @input="emitInputEvent"
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
.chip {
  display: flex;
  align-items: center;
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
