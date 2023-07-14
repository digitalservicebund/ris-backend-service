<script lang="ts" setup>
import { onBeforeUnmount, onMounted, ref, computed } from "vue"
import {
  DropdownInputModelType,
  DropdownItem,
} from "@/shared/components/input/types"
import { useInputModel } from "@/shared/composables/useInputModel"

interface Props {
  id: string
  items: DropdownItem[]
  modelValue?: DropdownInputModelType
  value?: DropdownInputModelType
  ariaLabel: string
  placeholder?: string
  hasSmallerHeight?: boolean
}

interface Emits {
  (event: "update:modelValue", value?: DropdownInputModelType): void
  (event: "input", value: Event): void
}

const props = defineProps<Props>()
const emits = defineEmits<Emits>()

const { inputValue } = useInputModel<string, Props, Emits>(props, emits)
const selectedLabel = computed(() => getLabel(inputValue.value))

const showDropdown = ref(false)
const dropdownContainerRef = ref<HTMLElement>()
const dropdownItemsRef = ref<HTMLElement>()
const focusedItemIndex = ref(0)

function getLabel(value?: DropdownInputModelType) {
  return props.items.find((item) => item.value === value)?.label
}

function toggleDropdown() {
  showDropdown.value = !showDropdown.value
  focusedItemIndex.value = 0
}

function choseItem(value: DropdownInputModelType) {
  closeDropdown()
  inputValue.value = value
}

function focusPreviousItem() {
  focusedItemIndex.value -= 1
  const prev = dropdownItemsRef.value?.childNodes[
    focusedItemIndex.value
  ] as HTMLElement
  if (prev) prev.focus()
}

function focusNextItem() {
  focusedItemIndex.value += 1
  const next = dropdownItemsRef.value?.childNodes[
    focusedItemIndex.value
  ] as HTMLElement
  if (next) next.focus()
}

function closeDropDownWhenClickOutside(event: MouseEvent) {
  const dropdown = dropdownContainerRef.value
  if (
    !dropdown ||
    (event.target as HTMLElement) === dropdown ||
    event.composedPath().includes(dropdown)
  )
    return
  closeDropdown()
}

function closeDropdown() {
  showDropdown.value = false
}

onMounted(() => {
  window.addEventListener("click", closeDropDownWhenClickOutside)
})

onBeforeUnmount(() => {
  window.removeEventListener("click", closeDropDownWhenClickOutside)
})

const computedStyle = computed(() => {
  return props.hasSmallerHeight
    ? { padding: "9px 16px" }
    : { padding: "12px 16px" }
})
</script>

<template>
  <div
    ref="dropdownContainerRef"
    class="dropdown-container relative w-full"
    @keydown.esc="closeDropdown"
  >
    <div
      class="dropdown-container__open-dropdown cursor-pointer"
      @click="toggleDropdown"
      @keydown.enter="toggleDropdown"
    >
      <div class="input-container bg-white" :style="computedStyle">
        <input
          :id="id"
          v-model="selectedLabel"
          :aria-label="ariaLabel"
          autocomplete="off"
          class="w-full cursor-pointer outline-none"
          :placeholder="placeholder"
          :readonly="true"
          tabindex="0"
          @keyup.down="focusNextItem"
        />
        <button
          :aria-labelledby="`labelDropdownIcon` + id"
          class="input-expand-icon flex items-center outline-none outline-0 outline-blue-800 focus:outline-2"
          tabindex="0"
          @keydown.enter="toggleDropdown"
        >
          <span v-if="!showDropdown" class="icon material-icons">
            expand_more
            <span
              :id="`labelDropdownIcon` + id"
              class="block h-[1px] w-[1px] overflow-hidden"
              >Dropdown öffnen</span
            >
          </span>

          <span v-else class="icon material-icons">
            expand_less
            <span
              :id="`labelDropdownIcon` + id"
              class="block h-[1px] w-[1px] overflow-hidden"
              >Dropdown schließen</span
            >
          </span>
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
        v-for="(item, index) in items"
        :key="index"
        aria-label="dropdown-option"
        class="dropdown-container__dropdown-item"
        tabindex="0"
        @click="choseItem(item.value)"
        @keypress.enter="choseItem(item.value)"
        @keyup.down="focusNextItem"
        @keyup.up="focusPreviousItem"
      >
        <span>
          {{ item.label }}
        </span>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.dropdown-container {
  display: inline-block;
  user-select: none;

  &__open-dropdown {
    .input-container {
      @apply flex h-64 border-2 border-blue-800 px-16 outline-none outline-0 outline-offset-[-4px] outline-blue-800 focus-within:outline-2 hover:outline-2 disabled:focus:outline-0;
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
    @apply cursor-pointer border-b-1 border-b-gray-400 bg-white px-[1.5rem] py-[1.063rem];

    &:last-of-type {
      @apply border-b-0;
    }

    &:hover {
      @apply bg-blue-300;
    }

    &:focus {
      @apply bg-blue-300;
    }
  }
}
</style>
