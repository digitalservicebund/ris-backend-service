<script lang="ts" setup>
import { produce } from "immer"
import {
  nextTick,
  onBeforeUnmount,
  onMounted,
  ref,
  watch,
  watchEffect,
} from "vue"
import ChipsList from "@/shared/components/input/ChipsList.vue"

interface Props {
  id: string
  modelValue?: string[]
  ariaLabel?: string
  readOnly?: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  "update:modelValue": [value?: string[]]
  chipAdded: [value: string]
  chipDeleted: [value: string]
}>()

/* -------------------------------------------------- *
 * Data handling                                      *
 * -------------------------------------------------- */

const newChipText = ref<string>("")

function addChip() {
  if (props.readOnly) return

  const chip = newChipText.value.trim()
  if (!chip) return

  const next = props.modelValue
    ? produce(props.modelValue, (draft) => {
        draft.push(chip)
      })
    : [chip]

  emit("chipAdded", chip)
  emit("update:modelValue", next)
  newChipText.value = ""
}

function onDeleteChip(chip: string) {
  if (props.readOnly) return

  focusInputIfEmpty()
  emit("chipDeleted", chip)
}

/* -------------------------------------------------- *
 * Focus management                                   *
 * -------------------------------------------------- */

const chipsInput = ref<HTMLInputElement | null>(null)

const focusedChip = ref<number | undefined>(undefined)

function maybeFocusPrevious() {
  if (chipsInput.value?.selectionStart === 0) {
    focusedChip.value =
      props.modelValue !== undefined ? props.modelValue.length - 1 : undefined
  }
}

const focusInput = () => {
  focusedChip.value = undefined
  chipsInput.value?.focus()
}

async function focusInputIfEmpty() {
  if (props.modelValue?.length === 1) focusInput()
}

/* -------------------------------------------------- *
 * Input width management (needed for the enter icon) *
 * -------------------------------------------------- */

const wrapperEl = ref<HTMLElement | null>(null)

// We're keeping the wrapper size to limit the width of the container of the
// input field. Ideally we'd have CSS do this for us, but that can be tricky
// with flexbox and we don't want that complexity to leak into the parent
// component - better have it contained here.
const wrapperContentWidth = ref<number | undefined>()

function updateWrapperSize() {
  if (!wrapperEl.value) return

  const { paddingLeft, paddingRight } = getComputedStyle(wrapperEl.value)
  const padding = parseInt(paddingLeft) + parseInt(paddingRight)
  wrapperContentWidth.value = wrapperEl.value.clientWidth - padding
}

const wrapperResizeObserver = new ResizeObserver(() => {
  updateWrapperSize()
})

onMounted(() => {
  if (!wrapperEl.value) return
  wrapperResizeObserver.observe(wrapperEl.value)
  updateWrapperSize()
})

onBeforeUnmount(() => {
  wrapperResizeObserver.disconnect()
})

const inputContentWidth = ref<string | undefined>("auto")

async function determineInputWidth() {
  if (!chipsInput.value) return

  // We first need to reset the height to auto, so that the scrollHeight
  // is not limited by the current height. Then wait for the next tick
  // so that the input has time to resize.
  inputContentWidth.value = undefined
  await nextTick()

  const { borderLeftWidth, borderRightWidth } = getComputedStyle(
    chipsInput.value as Element,
  )

  const borderLeft = parseInt(borderLeftWidth)
  const borderRight = parseInt(borderRightWidth)

  inputContentWidth.value = `${
    chipsInput.value.scrollWidth + borderLeft + borderRight
  }px`
}

watchEffect(() => {
  determineInputWidth().catch(() => {
    // left blank intentionally
  })
})

watch(newChipText, async () => {
  await determineInputWidth()
})
</script>

<template>
  <!-- Ignore requirement to have a keyboard listener as it's only a convenience
  for mouse users, but keyboard users can already do the same thing by tabbing
  just fine -->
  <!-- eslint-disable vuejs-accessibility/click-events-have-key-events -->
  <div
    ref="wrapperEl"
    class="flex min-h-[4rem] w-full cursor-text flex-wrap items-center overflow-hidden border-2 border-solid border-blue-800 bg-white px-16 py-8 outline-2 -outline-offset-4 outline-blue-800 autofill:text-inherit autofill:shadow-white hover:outline autofill:focus:text-inherit autofill:focus:shadow-white [&:has(:focus)]:outline"
    :class="{ 'hover:outline-none': readOnly }"
    :data-testid="`chips-input_${id}`"
    @click="focusInput"
  >
    <ChipsList
      v-model:focused-item="focusedChip"
      :model-value="modelValue"
      :read-only="readOnly"
      @chip-deleted="(_, value) => onDeleteChip(value)"
      @next-clicked-on-last="focusInput"
      @update:model-value="$emit('update:modelValue', $event)"
    />

    <span
      v-if="!readOnly"
      class="flex max-w-full flex-auto items-center justify-start"
      :style="{ maxWidth: `${wrapperContentWidth}px` }"
    >
      <span :id="`enter-note-for-${id}`" class="sr-only">
        Enter drücken, um die Eingabe zu bestätigen
      </span>
      <input
        :id="id"
        ref="chipsInput"
        v-model="newChipText"
        :aria-describedby="`enter-note-for-${id}`"
        :aria-label="ariaLabel"
        class="peer w-4 min-w-0 border-none bg-transparent outline-none"
        :style="{ width: inputContentWidth }"
        type="text"
        @focus="focusedChip = undefined"
        @keydown.enter.stop.prevent="addChip"
        @keydown.left="maybeFocusPrevious"
      />
      <span
        class="material-icons flex-none text-16 text-transparent peer-focus:text-gray-900"
      >
        subdirectory_arrow_left
      </span>
    </span>
  </div>
</template>
