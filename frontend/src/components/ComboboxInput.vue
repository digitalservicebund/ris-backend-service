<script lang="ts" setup>
import { onBeforeUnmount, onMounted, ref, watch, computed } from "vue"
import { Court } from "@/domain/documentUnit"
import {
  ComboboxItem,
  ComboboxInputModelType,
  ComboboxAttributes,
} from "@/domain/types"

interface Props {
  id: string
  itemService: ComboboxAttributes["itemService"]
  value?: ComboboxInputModelType
  modelValue?: ComboboxInputModelType
  ariaLabel: string
  placeholder?: string
}

interface Emits {
  (event: "update:modelValue", value?: ComboboxInputModelType): void
  (event: "input", value: Event): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const NO_MATCHING_ENTRY = "Kein passender Eintrag"

const candidateForSelection = ref<ComboboxItem>() // <-- the top search result
const selectedValue = ref<ComboboxInputModelType>()
const inputText = ref<string>()
const currentlyDisplayedItems = ref<ComboboxItem[]>()

const getLabelFromSelectedValue = (): string | undefined => {
  if (
    typeof selectedValue.value === "object" &&
    "label" in selectedValue.value
  ) {
    return selectedValue.value.label
  } else {
    return selectedValue.value
  }
}

watch(
  props,
  () => {
    selectedValue.value = props.modelValue ?? props.value
    updateInputText()
  },
  {
    immediate: true,
  }
)

watch(selectedValue, () => {
  emit("update:modelValue", selectedValue.value)
  updateInputText()
})

function isCourt(input?: ComboboxInputModelType): input is Court {
  return typeof input === "object" && "location" in input && "type" in input
}

function updateInputText() {
  inputText.value = getLabelFromSelectedValue()
}

const showDropdown = ref(false)

const filter = ref<string>()
const dropdownContainerRef = ref<HTMLElement>()
const dropdownItemsRef = ref<HTMLElement>()
const inputFieldRef = ref<HTMLInputElement>()
const focusedItemIndex = ref<number>(0)
const ariaLabelDropdownIcon = computed(() =>
  showDropdown.value ? "Dropdown schließen" : "Dropdown öffnen"
)

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
  focusedItemIndex.value = 0
  if (showDropdown.value) {
    if (inputText.value) {
      filter.value = inputText.value
    }
    updateCurrentItems()
    inputFieldRef.value?.focus()
  }
}

const clearSelection = () => {
  emit("update:modelValue", undefined)
  filter.value = ""
  inputText.value = ""
  focusedItemIndex.value = 0
  if (showDropdown.value) {
    updateCurrentItems()
  }
  inputFieldRef.value?.focus()
}

const setChosenItem = (item: ComboboxItem) => {
  if (item.label === NO_MATCHING_ENTRY) return
  showDropdown.value = false
  emit("update:modelValue", item.value)
  filter.value = item.label
  candidateForSelection.value = undefined
}

const onEnter = () => {
  if (candidateForSelection.value) {
    setChosenItem(candidateForSelection.value)
    return
  }
  updateInputText()
  toggleDropdown()
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

const onTextChange = () => {
  focusedItemIndex.value = 0
  showDropdown.value = true
  filter.value = inputText.value
  updateCurrentItems()
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
    currentlyDisplayedItems.value = [{ label: NO_MATCHING_ENTRY, value: "" }]
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
  updateInputText()
  filter.value = inputText.value
}

const isRevokedCourt = (item: ComboboxItem) => {
  return !!(isCourt(item.value) && item.value.revoked)
}

const getRevokedCourtString = (item: ComboboxItem) => {
  return (item.value as Court).revoked
}

onMounted(() => {
  window.addEventListener("click", handleClickOutside)
})

onBeforeUnmount(() => {
  window.removeEventListener("click", handleClickOutside)
})
</script>

<template>
  <div
    ref="dropdownContainerRef"
    class="dropdown-container"
    @keydown.esc="closeDropdownAndRevertToLastSavedValue"
  >
    <div
      class="dropdown-container__open-dropdown"
      @keydown.enter="onEnter"
      @keydown.tab="closeDropdownAndRevertToLastSavedValue"
    >
      <div class="bg-white input-container">
        <input
          :id="id"
          ref="inputFieldRef"
          v-model="inputText"
          :aria-label="ariaLabel"
          autocomplete="off"
          class="text-input"
          :placeholder="placeholder"
          :readonly="false"
          tabindex="0"
          @click="selectAllText"
          @input="onTextChange"
          @keyup.down="keydown"
        />
        <button
          v-if="inputText"
          class="input-close-icon"
          tabindex="-1"
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
          class="input-expand-icon"
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
      class="dropdown-container__dropdown-items"
      tabindex="-1"
    >
      <div
        v-for="(item, index) in currentlyDisplayedItems"
        :key="index"
        aria-label="dropdown-option"
        class="dropdown-container__dropdown-item"
        :class="{
          'dropdown-container__dropdown-item__with-additional-info':
            isRevokedCourt(item),
          'dropdown-container__dropdown-item__candidate-for-selection':
            candidateForSelection === item,
          'dropdown-container__dropdown-item__currently-selected':
            getLabelFromSelectedValue() === item.label,
          'dropdown-container__dropdown-item__no-matching-entry':
            item.label === NO_MATCHING_ENTRY,
        }"
        tabindex="0"
        @click="setChosenItem(item)"
        @keypress.enter="setChosenItem(item)"
        @keyup.down="keydown"
        @keyup.up="keyup"
      >
        <span>
          {{ item.label }}
          <span
            v-if="isRevokedCourt(item)"
            aria-label="additional-dropdown-info"
            class="body-02-reg dropdown-container__dropdown-item__additional-info"
            >{{ getRevokedCourtString(item) }}</span
          ></span
        >
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.dropdown-container {
  position: relative;
  display: inline-block;
  width: 100%;
  user-select: none;

  &__open-dropdown {
    .input-container {
      @apply border-2 border-solid border-blue-800 hover:shadow-hover hover:shadow-blue-800 focus:shadow-focus focus:shadow-blue-800;

      display: flex;
      height: 3.75rem;
      flex: row nowrap;
      justify-content: space-between;
      padding: 12px 16px;

      .text-input {
        width: 100%;

        &:focus {
          outline: none;
        }
      }

      .input-close-icon,
      .input-expand-icon {
        height: 5px;
        margin-top: 3px;
      }
    }
  }

  &__dropdown-items {
    /** Always show on top after textbox and width equal to textbox */
    position: absolute;
    z-index: 1;
    top: 100%;
    right: 0;
    left: 0;
    display: flex;
    max-height: 300px;
    flex-direction: column;
    filter: drop-shadow(0 1px 3px rgb(0 0 0 / 25%));
    overflow-y: scroll;
    scrollbar-width: none;
  }

  &__dropdown-item {
    @apply bg-white border-b-1 border-b-gray-400 cursor-pointer py-[1.063rem] px-[1.5rem];

    &:last-of-type {
      @apply border-b-0;
    }

    &:not(&__no-matching-entry):hover {
      @apply bg-gray-400;
    }

    &:not(&__no-matching-entry):focus {
      @apply bg-blue-200;

      outline: none;
    }

    &__no-matching-entry {
      cursor: default !important;
    }

    &__with-additional-info {
      @apply text-gray-900;

      font-style: italic;
    }

    &__additional-info {
      @apply text-neutral-700;
      @apply bg-neutral-20;

      padding: 6px 22px;
      border-radius: 100px;
      float: right;
      font-size: 14px;
      font-style: normal;
    }

    &__candidate-for-selection {
      @apply bg-blue-200;
    }

    &__currently-selected {
      @apply border-l-4 border-solid border-l-blue-800;
    }
  }
}
</style>
