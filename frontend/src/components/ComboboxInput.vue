<script setup lang="ts" generic="T extends InputModelProps">
import { useDebounceFn } from "@vueuse/core"
import {
  computed,
  onBeforeUnmount,
  onMounted,
  ref,
  shallowRef,
  watch,
} from "vue"
import FlexContainer from "@/components/FlexContainer.vue"
import {
  ComboboxAttributes,
  ComboboxInputModelType,
  ComboboxItem,
} from "@/components/input/types"
import LoadingSpinner from "@/components/LoadingSpinner.vue"
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
  readOnly?: boolean
  remoteSearchId?: string // unique identifier for remote search events (e.g. by ExtractionHighlight.vue) <- could be "id", but it's not unique currently
}>()

const emit = defineEmits<{
  "update:modelValue": [value?: ComboboxInputModelType]
  focus: [void]
}>()

const isDefined = <A,>(item: A | undefined): item is A => !!item

const NO_MATCHING_ENTRY = "Kein passender Eintrag"

const candidateForSelection = ref<ComboboxItem>() // <-- the top search result
const inputText = ref<string>()
const currentlyDisplayedItems = computed<ComboboxItem[]>(() =>
  [...(existingItems.value ?? []), createNewItem.value].filter(isDefined),
)
const createNewItem = ref<ComboboxItem>()
const showDropdown = ref(false)
const filter = ref<string>()
const dropdownContainerRef = ref<HTMLElement>()
const dropdownItemsRef = shallowRef<HTMLElement[]>([])
const inputFieldRef = ref<HTMLInputElement>()
const focusedItemIndex = ref<number>(-1)

const ariaLabelDropdownIcon = computed(() =>
  showDropdown.value ? "Dropdown schließen" : "Dropdown öffnen",
)

const conditionalClasses = computed(() => ({
  "!shadow-red-900 !bg-red-200": props.hasError,
  "!shadow-none !bg-blue-300": props.readOnly,
}))

const toggleDropdown = async () => {
  focusedItemIndex.value = -1
  showDropdown.value = !showDropdown.value
  if (showDropdown.value) {
    filter.value = inputText.value
    await updateCurrentItems()
    inputFieldRef.value?.focus()
  }
}

const showUpdatedDropdown = async () => {
  emit("focus")
  focusedItemIndex.value = -1
  showDropdown.value = true
  filter.value = inputText.value
  await updateCurrentItems()
}

const clearDropdown = async () => {
  if (props.noClear) return

  emit("update:modelValue", undefined)
  filter.value = ""
  inputText.value = ""
  focusedItemIndex.value = -1
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
    inputText.value = item.label
  }
  candidateForSelection.value = undefined
}

/** When user hits enter while fetching -> wait for results before executing enter action */
const hasQueuedEnter = ref(false)
const onEnter = async () => {
  if (isFetchingOrTyping.value) {
    hasQueuedEnter.value = true
  } else {
    hasQueuedEnter.value = false
    if (candidateForSelection.value) {
      setChosenItem(candidateForSelection.value)
      return
    }
    await toggleDropdown()
  }
}

const onInput = async () => {
  if (inputText.value === "") {
    if (!props.noClear) emit("update:modelValue", undefined)
  }

  await showUpdatedDropdown()
}

const keyArrowUp = () => {
  if (focusedItemIndex.value > 0) {
    focusedItemIndex.value -= 1
  }
  updateFocusedItem()
}

const keyArrowDown = () => {
  if (focusedItemIndex.value < dropdownItemsRef.value.length - 1) {
    focusedItemIndex.value += 1
  }
  updateFocusedItem()
}

const updateFocusedItem = () => {
  candidateForSelection.value = undefined
  const item = dropdownItemsRef.value[focusedItemIndex.value]
  if (item && item.innerText !== NO_MATCHING_ENTRY) item.focus()
}

const isFetchingOrTyping = ref(false)

/**
 * When a new request is started while a previous one is still running -> cancel the old one.
 * The canceled one should not unset the isFetching state as it is followed by a new request.
 */
async function updateCurrentItems() {
  isFetchingOrTyping.value = true
  if (canAbort.value) {
    abort()
  }
  const response = await debouncedFetchItems()
  const wasCanceled = !response
  if (!wasCanceled) isFetchingOrTyping.value = false
}

/** We do not want to select the first result on enter when a request is running, the selection is deferred until the results are in */
watch(isFetchingOrTyping, async (isFetching, wasFetching) => {
  if (wasFetching === true && isFetching === false) {
    if (hasQueuedEnter.value) {
      hasQueuedEnter.value = false
      await onEnter()
    }
  }
})

const {
  data: existingItems,
  execute: fetchItems,
  canAbort,
  abort,
} = props.itemService(filter)
const debouncedFetchItems = useDebounceFn(fetchItems, 200)

const noMatchingItems = [{ label: NO_MATCHING_ENTRY }]

/**
 * When the search result (existingItems) was fetched, we update the displayed items.
 * (Special cases: no results, createNewItem "neu erstellen")
 */
watch(existingItems, () => {
  if (existingItems.value === null) return
  if (existingItems.value === noMatchingItems) return

  const noMatchesFound = !existingItems.value.length
  const hasManualEntry = props.manualEntry && inputText.value
  if (!hasManualEntry && noMatchesFound) {
    existingItems.value = noMatchingItems
    candidateForSelection.value = undefined
    return
  }

  const exactMatchFound = existingItems.value?.find(
    (item) => item.label === filter.value?.trim(),
  )
  if (!exactMatchFound && hasManualEntry) {
    createNewItem.value = {
      label: `${inputText.value} neu erstellen`,
      value: { label: inputText.value! },
      labelCssClasses: "ris-label1-bold text-blue-800 underline",
    }
  } else {
    createNewItem.value = undefined
  }

  candidateForSelection.value = existingItems.value?.[0] ?? createNewItem.value
  focusedItemIndex.value = 0
})

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
  if (!props.readOnly) inputFieldRef.value?.select()
}

const closeDropdownAndRevertToLastSavedValue = () => {
  showDropdown.value = false
  inputText.value = props.modelValue?.label
}

async function handleRemoteSearch(event: Event) {
  const { id, query } = (event as CustomEvent).detail
  if (id === props.remoteSearchId) {
    console.log("Remote search triggered", { id, query })
    inputText.value = query
    filter.value = query
    await updateCurrentItems()
    // a) auto-set if only one result or exact match
    if (
      existingItems.value?.length === 1 ||
      (existingItems.value?.length &&
        existingItems.value[0].label.toLowerCase() === query.toLowerCase())
    ) {
      setChosenItem(existingItems.value[0])
    }
    // b) ambiguous result: open dropdown for user selection
    else {
      inputText.value = query // without, it would disappear again
      showDropdown.value = true
      dropdownItemsRef.value[0]?.focus()
    }
  }
}

watch(
  () => props.modelValue,
  (newValue, oldValue) => {
    // On autosave, the props get updated. We only need to update the input if the value has actually changed. Otherwise, this would overwrite user input when autosave happens.
    if (newValue?.label === oldValue?.label) return
    inputText.value = props.modelValue?.label
  },
  { immediate: true },
)

onMounted(() => {
  window.addEventListener("click", handleClickOutside)
  window.addEventListener(COMBOBOX_REMOTE_SEARCH_EVENT, handleRemoteSearch)
})

onBeforeUnmount(() => {
  window.removeEventListener("click", handleClickOutside)
  window.removeEventListener(COMBOBOX_REMOTE_SEARCH_EVENT, handleRemoteSearch)
})
</script>

<script lang="ts">
export type InputModelProps =
  | {
      label: string
    }
  | undefined

export const COMBOBOX_REMOTE_SEARCH_EVENT = "combobox-remote-search"
</script>

<template>
  <div ref="dropdownContainerRef" class="relative w-full">
    <div
      class="space-between shadow-blue flex h-48 flex-row bg-white px-16 py-12 whitespace-nowrap"
      :class="conditionalClasses"
    >
      <input
        :id="id"
        ref="inputFieldRef"
        v-model="inputText"
        :aria-label="ariaLabel"
        autocomplete="off"
        class="placeholder:font-font-family-sans w-full bg-transparent placeholder:text-gray-800 placeholder:not-italic focus:outline-none"
        :placeholder="placeholder"
        :readonly="readOnly"
        tabindex="0"
        @click="selectAllText"
        @focus="showUpdatedDropdown"
        @input="onInput"
        @keydown.down.prevent="keyArrowDown"
        @keydown.enter="onEnter"
        @keydown.esc="closeDropdownAndRevertToLastSavedValue"
        @keydown.tab="closeDropdownAndRevertToLastSavedValue"
      />
      <div v-if="!readOnly" class="flex flex-row items-center">
        <!-- Loading spinner needed for e2e tests -->
        <LoadingSpinner
          v-if="isFetchingOrTyping"
          data-testid="combobox-spinner"
          size="extra-small"
        />
        <button
          v-if="inputText && !noClear"
          aria-label="Auswahl zurücksetzen"
          class="flex items-center text-blue-800 focus:outline-none focus-visible:outline-4 focus-visible:outline-offset-4 focus-visible:outline-blue-800"
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
      v-if="showDropdown && !readOnly"
      class="absolute top-[100%] right-0 left-0 z-20 flex max-h-[300px] flex-col overflow-y-scroll bg-white px-8 py-12 drop-shadow-md"
      tabindex="-1"
    >
      <!-- Caution: misusage of index as key in v-for is needed for updateFocusedItem, do not remove before refactoring focus logic -->
      <button
        v-for="(item, index) in currentlyDisplayedItems"
        :key="index"
        ref="dropdownItemsRef"
        aria-label="dropdown-option"
        class="cursor-pointer px-16 py-12 text-left hover:bg-blue-100 focus:border-l-4 focus:border-solid focus:border-l-blue-800 focus:bg-blue-200 focus:outline-none"
        :class="{
          'border-l-4 border-solid border-l-blue-800 bg-blue-200':
            candidateForSelection &&
            candidateForSelection.label === item.label &&
            candidateForSelection.additionalInformation ===
              item.additionalInformation,
        }"
        tabindex="0"
        @click="setChosenItem(item)"
        @keydown.down.prevent="keyArrowDown"
        @keydown.enter="setChosenItem(item)"
        @keydown.tab="closeDropdownAndRevertToLastSavedValue"
        @keydown.up.prevent="keyArrowUp"
      >
        <FlexContainer
          align-items="items-end"
          justify-content="justify-between"
        >
          <span>
            <span :class="item.labelCssClasses">{{ item.label }}</span>
            <div
              v-if="item.additionalInformation"
              aria-label="additional-dropdown-info"
              class="ris-label2-regular text-gray-700"
            >
              {{ item.additionalInformation }}
            </div>
          </span>

          <span
            v-if="item.sideInformation"
            id="dropDownSideInformation"
            class="ris-label2-regular text-gray-700"
          >
            {{ item.sideInformation }}
          </span>
        </FlexContainer>
      </button>
    </div>
  </div>
</template>
