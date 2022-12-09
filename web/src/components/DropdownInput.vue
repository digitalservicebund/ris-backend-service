<script lang="ts" setup>
import { onBeforeUnmount, onMounted, ref, watch } from "vue"
import { Court } from "@/domain/documentUnit"
import type { DropdownItem } from "@/domain/types"
import { DropdownInputModelType, LookupTableEndpoint } from "@/domain/types"
import dropdownItemService from "@/services/dropdownItemService"

interface Props {
  id: string
  value?: DropdownInputModelType // TODO do we need this?
  modelValue?: DropdownInputModelType
  ariaLabel: string
  placeholder?: string
  dropdownItems?: DropdownItem[]
  endpoint?: LookupTableEndpoint
  isCombobox?: boolean
}

interface Emits {
  (event: "update:modelValue", value: DropdownInputModelType | undefined): void
  (event: "input", value: Event): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const inputValue = ref<DropdownInputModelType>()
const inputText = ref<string>()

watch(
  props,
  () => {
    inputValue.value = props.modelValue ?? props.value
    checkInputValueType()
  },
  {
    immediate: true,
  }
)

watch(inputValue, () => {
  emit("update:modelValue", inputValue.value)
  checkInputValueType()
})

function isCourt(input?: DropdownInputModelType): input is Court {
  return typeof input === "object" && "location" in input && "type" in input
}

function checkInputValueType() {
  if (isCourt(inputValue.value)) {
    inputText.value = inputValue.value.label
  } else {
    inputText.value = inputValue.value as string
  }
}

const isShowDropdown = ref(false)
const items = ref(props.dropdownItems ?? [])
const currentItems = ref<DropdownItem[]>([]) // the items currently displayed in the dropdown
const filter = ref<string>()
const dropdownContainerRef = ref<HTMLElement>()
const inputFieldRef = ref<HTMLInputElement>()
const focusedItemIndex = ref<number>(0)

const toggleDropdown = () => {
  isShowDropdown.value = !isShowDropdown.value
  focusedItemIndex.value = 0
  if (isShowDropdown.value) {
    updateCurrentItems()
    inputFieldRef.value?.focus()
  }
}

const clearSelection = () => {
  emit("update:modelValue", undefined)
  filter.value = ""
  inputText.value = ""
  if (isShowDropdown.value) {
    updateCurrentItems()
  }
  inputFieldRef.value?.focus()
}

const setChosenItem = (value: DropdownInputModelType) => {
  isShowDropdown.value = false
  emit("update:modelValue", value)
  filter.value = ""
}

const keyup = () => {
  focusedItemIndex.value -= 1
  const prev = dropdownContainerRef.value?.childNodes[
    focusedItemIndex.value
  ] as HTMLElement
  if (prev) prev.focus()
}

const keydown = () => {
  focusedItemIndex.value += 1
  const next = dropdownContainerRef.value?.childNodes[
    focusedItemIndex.value
  ] as HTMLElement
  if (next) next.focus()
}

const onTextChange = () => {
  focusedItemIndex.value = 0
  isShowDropdown.value = true
  filter.value = inputText.value
  updateCurrentItems()
}

const updateCurrentItems = async () => {
  if (!props.endpoint) {
    currentItems.value = items.value.filter((item) =>
      item.text.includes(!!filter.value ? filter.value : "")
    )
    insertItemIfEmpty()
    return
  }

  const response = await dropdownItemService.fetch(props.endpoint, filter.value)
  if (response.data) {
    currentItems.value = response.data
    insertItemIfEmpty()
  } else {
    console.error(response.error)
  }
}

const insertItemIfEmpty = () => {
  if (currentItems.value.length === 0) {
    currentItems.value = [{ text: "Kein passender Eintrag", value: "" }]
  }
}

const closeDropDownWhenClickOutSide = (event: MouseEvent) => {
  const dropdown = document.querySelector(`#${props.id}.dropdown-container`)
  if (dropdown == null) return
  if (
    (event.target as HTMLElement) === dropdown ||
    event.composedPath().includes(dropdown)
  )
    return
  isShowDropdown.value = false
}

const selectAllText = () => {
  const inputField = document.querySelector(
    `input#${props.id}`
  ) as HTMLInputElement
  if (!!props.modelValue) inputField.select()
}

const closeDropdown = () => {
  isShowDropdown.value = false
}

const isRevokedCourt = (item: DropdownItem) => {
  return !!(isCourt(item.value) && item.value.revoked)
}

const getRevokedCourtString = (item: DropdownItem) => {
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
  <div class="dropdown-container" @keydown.esc="closeDropdown">
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
          :readonly="!props.isCombobox"
          tabindex="0"
          @click="selectAllText"
          @input="onTextChange"
          @keyup.down="keydown"
        />
        <button
          v-if="isCombobox && inputText"
          class="input-close-icon"
          tabindex="0"
          @click="clearSelection"
          @keydown.enter="clearSelection"
        >
          <span class="icon material-icons pr-[1.5rem] text-blue-800">
            close
          </span>
        </button>
        <button
          class="input-expand-icon"
          tabindex="0"
          @click="toggleDropdown"
          @keydown.enter="toggleDropdown"
        >
          <span
            v-if="!isShowDropdown"
            class="icon material-icons text-blue-800"
          >
            expand_more
          </span>
          <span v-else class="icon material-icons"> expand_less </span>
        </button>
      </div>
    </div>
    <div
      v-if="isShowDropdown"
      ref="dropdownContainerRef"
      class="dropdown-container__dropdown-items"
      tabindex="-1"
    >
      <div
        v-for="(item, index) in currentItems"
        :key="index"
        class="dropdown-container__dropdown-item"
        tabindex="0"
        @click="setChosenItem(item.value)"
        @keypress.enter="setChosenItem(item.value)"
        @keyup.down="keydown"
        @keyup.up="keyup"
      >
        <span>
          {{ item.text }}
          <span
            v-if="isRevokedCourt(item)"
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
      @apply bg-blue-200;
    }

    &:focus {
      @apply bg-blue-200;
    }

    &__additional-info {
      @apply text-gray-900;

      float: right;
    }
  }
}
</style>
