<script setup lang="ts" generic="T extends InputModelProps">
import { onBeforeUnmount, onMounted, ref, watch, computed } from "vue"
import {
  ComboboxAttributes,
  ComboboxInputModelType,
  ComboboxItem,
} from "@/shared/components/input/types"

const props = defineProps<{
  id: string
  itemService: ComboboxAttributes["itemService"]
  modelValue: T
  ariaLabel: string
  placeholder?: string
  clearOnChoosingItem?: boolean
  hasError?: boolean
}>()

const emit = defineEmits<{
  "update:modelValue": [value?: ComboboxInputModelType]
  input: [value: Event]
}>()

const NO_MATCHING_ENTRY = "Kein passender Eintrag"

const candidateForSelection = ref<ComboboxItem>() // <-- the top search result
const inputText = ref<string>()
const currentlyDisplayedItems = ref<ComboboxItem[]>()
const showDropdown = ref(false)
const filter = ref<string>()
const dropdownContainerRef = ref<HTMLElement>()
const dropdownItemsRef = ref<HTMLElement>()
const inputFieldRef = ref<HTMLInputElement>()
const focusedItemIndex = ref<number>(0)
const ariaLabelDropdownIcon = computed(() =>
  showDropdown.value ? "Dropdown schließen" : "Dropdown öffnen"
)

const conditionalClasses = computed(() =>
  props.hasError
    ? "border-red-800 bg-red-200 focus:shadow-red-800 placeholder-black"
    : "bg-white border-blue-800 focus:shadow-blue-800 h-[3.75rem] hover:shadow-blue-800"
)

const placeholderColor = computed(() =>
  props.hasError ? "placeholder-black" : ""
)

const toggleDropdown = async () => {
  showDropdown.value = !showDropdown.value
  focusedItemIndex.value = 0
  if (showDropdown.value) {
    if (inputText.value) {
      filter.value = inputText.value
    }
    await updateCurrentItems()
    inputFieldRef.value?.focus()
  }
}

const clearSelection = async () => {
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

const onTextChange = async () => {
  focusedItemIndex.value = 0
  showDropdown.value = true
  filter.value = inputText.value
  await updateCurrentItems()
}

const updateCurrentItems = async () => {
  const response = await props.itemService(filter.value)
  if (!response.data) {
    console.error(response.error)
    return
  }
  currentlyDisplayedItems.value = response.data
  if (
    !currentlyDisplayedItems.value ||
    currentlyDisplayedItems.value.length === 0
  ) {
    currentlyDisplayedItems.value = [{ label: NO_MATCHING_ENTRY }]
    candidateForSelection.value = undefined
  } else {
    candidateForSelection.value = currentlyDisplayedItems.value[0]
    focusedItemIndex.value = 1
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
  inputFieldRef.value?.select()
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
  { immediate: true }
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
  <div
    ref="dropdownContainerRef"
    class="relative w-full"
    @keydown.esc="closeDropdownAndRevertToLastSavedValue"
  >
    <div
      @keydown.enter="onEnter"
      @keydown.tab="closeDropdownAndRevertToLastSavedValue"
    >
      <div
        class="border-2 border-solid flex flex-row focus:shadow-focus h-[3.75rem] hover:shadow-hover px-16 py-12 space-between whitespace-nowrap"
        :class="conditionalClasses"
      >
        <input
          :id="id"
          ref="inputFieldRef"
          v-model="inputText"
          :aria-label="ariaLabel"
          autocomplete="off"
          class="bg-transparent focus:outline-none w-full"
          :class="placeholderColor"
          :placeholder="placeholder"
          :readonly="false"
          tabindex="0"
          @click="selectAllText"
          @input="onTextChange"
          @keyup.down="keydown"
        />
        <button
          v-if="inputText"
          class="input-close-icon mt-[3px]"
          tabindex="0"
          @click="clearSelection"
        >
          <span
            aria-label="Auswahl zurücksetzen"
            class="icon material-icons pr-[1.5rem] text-blue-800"
          >
            close
          </span>
        </button>
        <button
          :aria-label="ariaLabelDropdownIcon"
          class="input-expand-icon mt-[3px]"
          tabindex="-1"
          @click="toggleDropdown"
        >
          <span v-if="!showDropdown" class="icon material-icons text-blue-800">
            expand_more
          </span>
          <span v-else class="icon material-icons"> expand_less </span>
        </button>
      </div>
    </div>
    <div
      v-if="showDropdown"
      ref="dropdownItemsRef"
      class="absolute bg-white drop-shadow-md flex flex-col left-0 max-h-[300px] overflow-y-scroll right-0 top-[100%] z-10"
      tabindex="-1"
    >
      <div
        v-for="(item, index) in currentlyDisplayedItems"
        :key="index"
        aria-label="dropdown-option"
        class="border-b-1 border-b-gray-400 cursor-pointer focus:bg-blue-200 focus:outline-none hover:bg-gray-400 last:border-b-0 px-[1.5rem] py-[1rem]"
        :class="{
          'bg-blue-200': candidateForSelection === item,
          'border-l-4 border-solid border-l-blue-800': inputText === item.label,
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
            class="body-02-reg text-neutral-700"
          >
            {{ item.additionalInformation }}
          </div>
        </span>
      </div>
    </div>
  </div>
</template>
