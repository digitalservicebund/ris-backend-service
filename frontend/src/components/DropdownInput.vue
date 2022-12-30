<script lang="ts" setup>
import { onBeforeUnmount, onMounted, ref, computed } from "vue"
import { useInputModel } from "@/composables/useInputModel"
import { DropdownInputModelType, DropdownItem } from "@/domain/types"

interface Props {
  id: string
  items: DropdownItem[]
  modelValue?: DropdownInputModelType
  value?: DropdownInputModelType
  ariaLabel: string
  placeholder?: string
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
const ariaLabelDropdownIcon = computed(() =>
  showDropdown.value ? "Dropdown schließen" : "Dropdown öffnen"
)
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
</script>

<template>
  <div
    ref="dropdownContainerRef"
    class="dropdown-container"
    @keydown.esc="closeDropdown"
  >
    <div
      class="cursor-pointer dropdown-container__open-dropdown"
      @click="toggleDropdown"
      @keydown.enter="toggleDropdown"
    >
      <div class="bg-white input-container">
        <input
          :id="id"
          v-model="selectedLabel"
          :aria-label="ariaLabel"
          autocomplete="off"
          class="cursor-pointer text-input"
          :placeholder="placeholder"
          :readonly="true"
          tabindex="0"
          @keyup.down="focusNextItem"
        />
        <button
          :aria-label="ariaLabelDropdownIcon"
          class="input-expand-icon"
          tabindex="0"
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
  }
}
</style>
