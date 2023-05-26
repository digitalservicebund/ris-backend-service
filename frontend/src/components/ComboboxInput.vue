<script setup lang="ts" generic="T extends InputModelProps">
import { onBeforeUnmount, onMounted, ref, watch, computed } from "vue"
import {
  ComboboxAttributes,
  ComboboxInputModelType,
  ComboboxItem,
} from "@/shared/components/input/types"

interface Props {
  id: string
  itemService: ComboboxAttributes["itemService"]
  modelValue: T
  ariaLabel: string
  placeholder?: string
  clearOnChoosingItem?: boolean
}

interface Emits {
  (event: "update:modelValue", value: ComboboxInputModelType | undefined): void
  (event: "input", value: Event): void
}

const props = defineProps<Props>()

const emit = defineEmits<Emits>()

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

<!-- <script lang="ts">
export type InputModelProps = {
  label: string
}
</script> -->

<template>
  <div
    ref="dropdownContainerRef"
    class="dropdown-container"
    role="button"
    tabindex="0"
    @keydown.esc="closeDropdownAndRevertToLastSavedValue"
  >
    <div
      class="dropdown-container__open-dropdown"
      role="button"
      tabindex="0"
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
          'bg-blue-200': candidateForSelection === item,
          'dropdown-container__dropdown-item__currently-selected':
            inputText === item.label,
          'dropdown-container__dropdown-item__no-matching-entry':
            item.label === NO_MATCHING_ENTRY,
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
`

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

    &__currently-selected {
      @apply border-l-4 border-solid border-l-blue-800;
    }
  }
}
</style>
