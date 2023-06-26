<script lang="ts" setup>
import {
  nextTick,
  onBeforeUnmount,
  onMounted,
  ref,
  watch,
  watchEffect,
} from "vue"
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

/* -------------------------------------------------- *
 * Data handling                                      *
 * -------------------------------------------------- */

const { emitInputEvent } = useInputModel<string[], Props, Emits>(props, emits)
const chips = ref<string[]>(props.modelValue ?? [])
const currentInput = ref<string>("")

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

watch(props, () => {
  if (props.modelValue) chips.value = props.modelValue
})

/* -------------------------------------------------- *
 * Focus management                                   *
 * -------------------------------------------------- */

const chipsList = ref<typeof ChipsList>()
const chipsInput = ref<HTMLInputElement>()

const focusPrevious = () => {
  if (chipsList.value !== undefined && currentInput.value === "")
    chipsList.value.focusPrevious()
}

const focusInput = () => {
  if (chipsList.value !== undefined) chipsList.value.resetFocus()
  if (chipsInput.value !== undefined) chipsInput.value.focus()
}

// Temporarily disabled to prevent this from stealing focus from any input
// on the page when the chips are empty. To fix this, we would need to either
// react to the 'deleteChip' event or watch the chips list to see if it's empty.
// However due to the way the chips list updates data, the list is not in the
// state you'd expect when the events/watchers are triggered, so we'll need
// to fix that first.
// TODO: Re-enable this once modelValue is properly updated
// watch(
//   chips,
//   (next) => {
//     if (!next?.length) focusInput()
//   },
//   { deep: true }
// )

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
  // so that the textarea has time to resize.
  inputContentWidth.value = undefined
  await nextTick()

  const { borderLeftWidth, borderRightWidth } = getComputedStyle(
    chipsInput.value as Element
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

watch(currentInput, async () => {
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
    class="-outline-offset-4 [&:has(:focus)]:outline autofill:focus:shadow-white autofill:focus:text-inherit autofill:shadow-white autofill:text-inherit bg-white border-2 border-blue-800 border-solid cursor-text flex flex-wrap hover:outline items-center min-h-[3.75rem] outline-2 outline-blue-800 overflow-hidden px-16 py-8 w-full"
    @click="focusInput"
  >
    <ChipsList
      ref="chipsList"
      v-model="chips"
      @next-clicked-on-last="focusInput"
    />
    <span
      class="flex flex-auto items-center justify-start max-w-full"
      :style="{ maxWidth: `${wrapperContentWidth}px` }"
    >
      <span :id="`enter-note-for-${id}`" class="sr-only">
        Enter drücken, um die Eingabe zu bestätigen
      </span>
      <input
        :id="id"
        ref="chipsInput"
        v-model="currentInput"
        :aria-describedby="`enter-note-for-${id}`"
        :aria-label="ariaLabel"
        class="bg-transparent border-none min-w-0 outline-none peer w-[1ch]"
        :style="{ width: inputContentWidth }"
        type="text"
        @input="emitInputEvent"
        @keypress.enter="saveChip"
        @keyup.left="focusPrevious"
      />
      <span
        class="flex-none material-icons peer-focus:text-gray-900 text-16 text-transparent"
      >
        subdirectory_arrow_left
      </span>
    </span>
  </div>
</template>
