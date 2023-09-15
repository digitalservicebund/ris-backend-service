<script lang="ts" setup>
import { produce } from "immer"
import { ref, watch } from "vue"

interface Props {
  modelValue: string[] | undefined
  focusedItem?: number
  readOnly?: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  chipDeleted: [index: number, value: string]
  nextClickedOnLast: []
  previousClickedOnFirst: []
  "update:modelValue": [value?: string[]]
  "update:focusedItem": [index: number | undefined]
}>()

/* -------------------------------------------------- *
 * Model value                                        *
 * -------------------------------------------------- */

function deleteChip(index: number, value: string) {
  if (props.readOnly) return

  if (!props.modelValue || index >= props.modelValue.length) return
  const next = produce(props.modelValue, (draft) => {
    draft.splice(index, 1)
  })

  emit("update:modelValue", next.length === 0 ? undefined : next)
  emit("chipDeleted", index, value)
}

/* -------------------------------------------------- *
 * Focus management                                   *
 * -------------------------------------------------- */

const containerRef = ref<HTMLElement>()

// Decoupling the local state for the focused item from the model value allows
// us to manage the focus even if no model value has been passed. Parents are
// free to pass a model value though if they want to take control of the focus.
const localFocusedItem = ref<number | undefined>(props.focusedItem)

// Sync prop to local state, making sure it's within the bounds of the model
// value
watch(
  () => props.focusedItem,
  (is) => {
    if (is === localFocusedItem.value) return
    localFocusedItem.value = is !== undefined ? toExistingIndex(is) : undefined
  },
)

// Sync local state to prop
watch(
  () => localFocusedItem.value,
  (is) => {
    if (is === props.focusedItem) return
    emit("update:focusedItem", is)
  },
)

// Adjust local state when model value changes to make sure it's still adressing
// a valid item
watch(
  () => props.modelValue,
  (is) => {
    if (!is) localFocusedItem.value = undefined
    else localFocusedItem.value = toExistingIndex(localFocusedItem.value)
  },
)

watch(localFocusedItem, (is) => {
  // TODO: Remove focus when changing to undefined
  if (is === undefined) return
  const item = containerRef.value?.children?.[is] as HTMLElement
  item?.focus()
})

/**
 * Returns the value if it falls within the bounds of the model value, or the
 * closest valid value otherwise.
 */
function toExistingIndex(index: number | undefined): number | undefined {
  if (!props.modelValue?.length || index === undefined) return undefined
  else if (index < 0) return 0
  else if (index >= props.modelValue.length) return props.modelValue.length - 1
  else return index
}

function focusPrevious() {
  if (localFocusedItem.value === undefined) return
  if (localFocusedItem.value === 0) emit("previousClickedOnFirst")
  localFocusedItem.value = Math.max(0, localFocusedItem.value - 1)
}

function focusNext() {
  if (localFocusedItem.value === undefined || !props.modelValue) return
  if (localFocusedItem.value === props.modelValue.length - 1) {
    emit("nextClickedOnLast")
  }
  const next = Math.min(props.modelValue.length - 1, localFocusedItem.value + 1)
  localFocusedItem.value = next
}
</script>

<template>
  <ul
    ref="containerRef"
    class="my-4 mr-8 flex flex-row flex-wrap items-center gap-8 empty:m-0"
  >
    <template v-if="modelValue">
      <!-- eslint-disable-next-line vuejs-accessibility/no-static-element-interactions -->
      <li
        v-for="(chip, i) in modelValue"
        :key="i"
        class="group ds-body-01-reg relative flex items-center break-words rounded-10 outline-none"
        :class="[
          readOnly
            ? 'cursor-default bg-gray-400 pr-0'
            : 'cursor-pointer bg-blue-500 pr-32',
        ]"
        data-testid="chip"
        tabindex="0"
        @click.stop="localFocusedItem = i"
        @focus="localFocusedItem = i"
        @keydown.enter.stop.prevent="deleteChip(i, chip)"
        @keydown.left.stop.prevent="focusPrevious"
        @keydown.right.stop.prevent="focusNext"
      >
        <span
          class="flex min-h-[2rem] whitespace-pre-wrap px-6 py-4 leading-24"
          data-testid="chip-value"
          >{{ chip }}
        </span>
        <button
          v-if="!readOnly"
          aria-label="Löschen"
          class="iems-center absolute inset-y-0 right-0 flex h-full items-center rounded-r-10 p-4 group-focus:bg-blue-900 group-focus:text-white"
          tabindex="-1"
          type="button"
          @click="deleteChip(i, chip)"
        >
          <em class="material-icons text-center">clear</em>
        </button>
      </li>
    </template>
  </ul>
</template>
