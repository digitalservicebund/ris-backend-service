<script setup lang="ts" generic="T extends InputModelProps">
import * as Sentry from "@sentry/vue"
import { onBeforeUnmount, onMounted, ref, watch, computed } from "vue"
import {
  ComboboxAttributes,
  ComboboxInputModelType,
  ComboboxItem,
} from "@/components/input/types"
import IconKeyboardArrowDown from "~icons/ic/baseline-keyboard-arrow-down"
import IconKeyboardArrowUp from "~icons/ic/baseline-keyboard-arrow-up"
import IconClear from "~icons/material-symbols/close-small"

const props = defineProps<{
  id: string
  itemService: ComboboxAttributes["itemService"]
  modelValue: T
  ariaLabel: string
  placeholder?: string
  clearOnChoosingItem?: boolean
  manualEntry?: boolean
  noClear?: boolean
  hasError?: boolean
  readonly?: boolean
}>()

const emit = defineEmits<{
  "update:modelValue": [value?: ComboboxInputModelType]
  input: [value: Event]
}>()

const NO_MATCHING_ENTRY = "Kein passender Eintrag"

const candidateForSelection = ref<ComboboxItem>() // <-- the top search result
const inputText = ref<string>()
const currentlyDisplayedItems = ref<ComboboxItem[]>()
const createNewItem = ref<ComboboxItem>()
const showDropdown = ref(false)
const filter = ref<string>()
const dropdownContainerRef = ref<HTMLElement>()
const dropdownItemsRef = ref<HTMLElement>()
const inputFieldRef = ref<HTMLInputElement>()
const focusedItemIndex = ref<number>(0)

const isUpdating = ref(false)
const hasToUpdate = ref(false)

const ariaLabelDropdownIcon = computed(() =>
  showDropdown.value ? "Dropdown schließen" : "Dropdown öffnen",
)
const noCurrentlyDisplayeditems = computed(
  () =>
    !currentlyDisplayedItems.value ||
    currentlyDisplayedItems.value.length === 0,
)

const conditionalClasses = computed(() => ({
  "!shadow-red-900 !bg-red-200": props.hasError,
  "!shadow-none !bg-blue-300": props.readonly,
}))

const toggleDropdown = async () => {
  focusedItemIndex.value = 0
  showDropdown.value = !showDropdown.value
  if (showDropdown.value) {
    if (inputText.value) {
      filter.value = inputText.value
    }
    await updateCurrentItems()
    inputFieldRef.value?.focus()
  }
}

const showUpdatedDropdown = async () => {
  focusedItemIndex.value = 0
  showDropdown.value = true
  if (inputText.value) {
    filter.value = inputText.value
  }
  await updateCurrentItems()
}

const clearDropdown = async () => {
  if (props.noClear) return

  emit("update:modelValue", undefined)
  filter.value = ""
  inputText.value = ""
  focusedItemIndex.value = 0
  if (showDropdown.value) {
    await updateCurrentItems()
  }
  inputFieldRef.value?.focus()
}

const setChosenItem = (item: ComboboxItem) => {
  if (item.label === NO_MATCHING_ENTRY) return
  showDropdown.value = false
  emit("update:modelValue", item.value)
  if (props.clearOnChoosingItem) {
    filter.value = ""
    inputText.value = ""
  } else {
    filter.value = item.label
  }
  candidateForSelection.value = undefined
}

const onEnter = async () => {
  if (candidateForSelection.value) {
    setChosenItem(candidateForSelection.value)
    return
  }
  await toggleDropdown()
}

const onInput = async () => {
  if (inputText.value === "") {
    if (!props.noClear) emit("update:modelValue", undefined)
  }

  await showUpdatedDropdown()
}

const keyup = () => {
  if (focusedItemIndex.value > 1) {
    focusedItemIndex.value -= 1
  }
  updateFocusedItem()
}

const keydown = () => {
  if (
    currentlyDisplayedItems.value &&
    focusedItemIndex.value < currentlyDisplayedItems.value.length
  ) {
    focusedItemIndex.value += 1
  }
  updateFocusedItem()
}

const updateFocusedItem = () => {
  candidateForSelection.value = undefined
  const item = dropdownItemsRef.value?.childNodes[
    focusedItemIndex.value
  ] as HTMLElement
  if (item && item.innerText !== NO_MATCHING_ENTRY) item.focus()
}

const updateCurrentItems = async () => {
  hasToUpdate.value = true
  if (isUpdating.value && hasToUpdate.value) {
    return
  }

  isUpdating.value = true

  let response
  let tries = 0
  do {
    hasToUpdate.value = false
    response = await props.itemService(filter.value)
    isUpdating.value = false
    tries++
  } while (hasToUpdate.value && tries < 6)

  if (tries >= 6) {
    Sentry.captureMessage(
      "more than 5 tries to call the item service at the combobox",
      "error",
    )
  }

  if (!response.data) {
    console.error(response.error)
    return
  }

  currentlyDisplayedItems.value = response.data

  if (
    noCurrentlyDisplayeditems.value ||
    //no exact match found when add manual entry option set
    (props.manualEntry &&
      filter.value &&
      !currentlyDisplayedItems.value.find(
        (item) => item.label === filter.value?.trim(),
      ))
  ) {
    handleNoSearchResults(filter.value)
  } else {
    createNewItem.value = undefined
    candidateForSelection.value = currentlyDisplayedItems.value[0]
    focusedItemIndex.value = 1
  }
}

function handleNoSearchResults(searchStr?: string) {
  if (props.manualEntry && searchStr) {
    createNewItem.value = {
      label: `${searchStr} neu erstellen`,
      value: { label: searchStr },
    }
    candidateForSelection.value = { label: searchStr }
  } else {
    currentlyDisplayedItems.value = [{ label: NO_MATCHING_ENTRY }]
    candidateForSelection.value = undefined
  }
}

const handleClickOutside = (event: MouseEvent) => {
  const dropdown = dropdownContainerRef.value
  if (
    !dropdown ||
    (event.target as HTMLElement) === dropdown ||
    event.composedPath().includes(dropdown)
  )
    return
  closeDropdownAndRevertToLastSavedValue()
}

const selectAllText = () => {
  if (!props.readonly) inputFieldRef.value?.select()
}

const closeDropdownAndRevertToLastSavedValue = () => {
  showDropdown.value = false
  inputText.value = props.modelValue?.label
}

watch(
  props,
  () => {
    inputText.value = props.modelValue?.label
  },
  { immediate: true },
)

onMounted(() => {
  window.addEventListener("click", handleClickOutside)
})

onBeforeUnmount(() => {
  window.removeEventListener("click", handleClickOutside)
})
</script>

<script lang="ts">
export type InputModelProps =
  | {
      label: string
    }
  | undefined
</script>

<template>
  <div ref="dropdownContainerRef" class="relative w-full">
    <div
      class="space-between flex h-48 flex-row whitespace-nowrap bg-white px-16 py-12 shadow-button shadow-blue-800"
      :class="conditionalClasses"
    >
      <input
        :id="id"
        ref="inputFieldRef"
        v-model="inputText"
        :aria-label="ariaLabel"
        autocomplete="off"
        class="w-full bg-transparent placeholder:font-font-family-sans placeholder:not-italic placeholder:text-gray-800 focus:outline-none"
        :placeholder="placeholder"
        :readonly="readonly"
        tabindex="0"
        @click="selectAllText"
        @focus="showUpdatedDropdown"
        @input="onInput"
        @keydown.enter="onEnter"
        @keydown.esc="closeDropdownAndRevertToLastSavedValue"
        @keydown.tab="closeDropdownAndRevertToLastSavedValue"
        @keyup.down="keydown"
      />
      <div v-if="!readonly" class="flex flex-row">
        <button
          v-if="inputText && !noClear"
          aria-label="Auswahl zurücksetzen"
          class="input-close-icon flex items-center text-blue-800 focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800"
          tabindex="0"
          @click="clearDropdown"
        >
          <IconClear />
        </button>

        <button
          :aria-label="ariaLabelDropdownIcon"
          class="input-expand-icon flex items-center text-blue-800"
          tabindex="-1"
          @click="toggleDropdown"
        >
          <IconKeyboardArrowDown v-if="!showDropdown" />
          <IconKeyboardArrowUp v-else />
        </button>
      </div>
    </div>
    <div
      v-if="showDropdown && !readonly"
      ref="dropdownItemsRef"
      class="absolute left-0 right-0 top-[100%] z-20 flex max-h-[300px] flex-col overflow-y-scroll bg-white px-8 py-12 drop-shadow-md"
      tabindex="-1"
    >
      <div
        v-for="(item, index) in currentlyDisplayedItems"
        :key="index"
        aria-label="dropdown-option"
        class="cursor-pointer px-16 py-12 hover:bg-blue-100 focus:border-l-4 focus:border-solid focus:border-l-blue-800 focus:bg-blue-200 focus:outline-none"
        :class="{
          'border-l-4 border-solid border-l-blue-800 bg-blue-200':
            candidateForSelection === item,
        }"
        role="button"
        tabindex="0"
        @click="setChosenItem(item)"
        @keydown.tab="closeDropdownAndRevertToLastSavedValue"
        @keypress.enter="setChosenItem(item)"
        @keyup.down="keydown"
        @keyup.up="keyup"
      >
        <span>
          <span>{{ item.label }}</span>
          <div
            v-if="item.additionalInformation"
            aria-label="additional-dropdown-info"
            class="ds-body-02-reg text-neutral-700"
          >
            {{ item.additionalInformation }}
          </div>
        </span>
      </div>
      <div
        v-if="createNewItem"
        key="createNewItem"
        aria-label="dropdown-option"
        class="cursor-pointer px-16 py-12 hover:bg-blue-100 focus:border-l-4 focus:border-solid focus:border-l-blue-800 focus:bg-blue-200 focus:outline-none"
        role="button"
        tabindex="0"
        @click="setChosenItem(createNewItem)"
        @keydown.tab="closeDropdownAndRevertToLastSavedValue"
        @keypress.enter="setChosenItem(createNewItem)"
        @keyup.down="keydown"
        @keyup.up="keyup"
      >
        <span>
          <span class="ds-label-01-bold text-blue-800 underline">{{
            createNewItem?.label
          }}</span>
        </span>
      </div>
    </div>
  </div>
</template>
