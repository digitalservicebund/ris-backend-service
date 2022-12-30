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

const selectedValue = ref<ComboboxInputModelType>()
const inputText = ref<string>()
const currentlyDisplayedItems = ref<ComboboxItem[]>()

watch(
  props,
  () => {
    selectedValue.value = props.modelValue ?? props.value
    checkInputValueType()
  },
  {
    immediate: true,
  }
)

watch(selectedValue, () => {
  emit("update:modelValue", selectedValue.value)
  checkInputValueType()
})

function isCourt(input?: ComboboxInputModelType): input is Court {
  return typeof input === "object" && "location" in input && "type" in input
}

function checkInputValueType() {
  if (typeof selectedValue.value === "object" && "label" in selectedValue.value)
    inputText.value = selectedValue.value.label
  else {
    inputText.value = selectedValue.value
  }
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
    updateCurrentItems()
    inputFieldRef.value?.focus()
  }
}

const clearSelection = () => {
  emit("update:modelValue", undefined)
  filter.value = ""
  inputText.value = ""
  if (showDropdown.value) {
    updateCurrentItems()
  }
  inputFieldRef.value?.focus()
}

const setChosenItem = (value: ComboboxInputModelType) => {
  showDropdown.value = false
  emit("update:modelValue", value)
  filter.value = ""
}

const keyup = () => {
  focusedItemIndex.value -= 1
  const prev = dropdownItemsRef.value?.childNodes[
    focusedItemIndex.value
  ] as HTMLElement
  if (prev) prev.focus()
}

const keydown = () => {
  focusedItemIndex.value += 1
  const next = dropdownItemsRef.value?.childNodes[
    focusedItemIndex.value
  ] as HTMLElement
  if (next) next.focus()
}

const onTextChange = () => {
  focusedItemIndex.value = 0
  showDropdown.value = true
  filter.value = inputText.value
  updateCurrentItems()
}

const updateCurrentItems = async () => {
  const response = await props.itemService(filter.value)
  if (response.data) {
    currentlyDisplayedItems.value = response.data
    insertItemIfEmpty()
  } else {
    console.error(response.error)
  }
}

const insertItemIfEmpty = () => {
  if (
    !currentlyDisplayedItems.value ||
    currentlyDisplayedItems.value.length === 0
  ) {
    currentlyDisplayedItems.value = [
      { label: "Kein passender Eintrag", value: "" },
    ]
  }
}

const closeDropDownWhenClickOutSide = (event: MouseEvent) => {
  const dropdown = dropdownContainerRef.value
  if (
    !dropdown ||
    (event.target as HTMLElement) === dropdown ||
    event.composedPath().includes(dropdown)
  )
    return
  showDropdown.value = false
}

const selectAllText = () => {
  inputFieldRef.value?.select()
}

const closeDropdown = () => {
  showDropdown.value = false
}

const isRevokedCourt = (item: ComboboxItem) => {
  return !!(isCourt(item.value) && item.value.revoked)
}

const getRevokedCourtString = (item: ComboboxItem) => {
  return (item.value as Court).revoked
}

onMounted(() => {
  window.addEventListener("click", closeDropDownWhenClickOutSide)
})

onBeforeUnmount(() => {
  window.removeEventListener("click", closeDropDownWhenClickOutSide)
})
</script>

<template>
  <div
    ref="dropdownContainerRef"
    class="dropdown-container"
    @keydown.esc="closeDropdown"
  >
    <div
      class="dropdown-container__open-dropdown"
      @keydown.enter="toggleDropdown"
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
          tabindex="0"
          @click="clearSelection"
          @keydown.enter="clearSelection"
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
          tabindex="0"
          @click="toggleDropdown"
          @keydown.enter="toggleDropdown"
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
        }"
        tabindex="0"
        @click="setChosenItem(item.value)"
        @keypress.enter="setChosenItem(item.value)"
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

    &:hover {
      @apply bg-blue-300;
    }

    &:focus {
      @apply bg-blue-300;
    }

    &__with-additional-info {
      @apply bg-gray-100;
    }

    &__additional-info {
      @apply text-gray-900;

      float: right;
    }
  }
}
</style>
