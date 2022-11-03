<script lang="ts" setup>
import { onBeforeUnmount, onMounted, ref } from "vue"
import { useInputModel } from "@/composables/useInputModel"
import type { DropdownItem } from "@/domain/types"
import { LookupTableEndpoint } from "@/domain/types"
import lookupTableService from "@/services/lookupTableService"

interface Props {
  id: string
  value?: string // @public
  modelValue?: string
  ariaLabel: string
  placeholder?: string
  dropdownItems: DropdownItem[] | LookupTableEndpoint
  isCombobox?: boolean
  preselectedValue?: string
}

interface Emits {
  (event: "update:modelValue", value: string | undefined): void
  (event: "input", value: Event): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const { inputValue } = useInputModel<string, Props, Emits>(props, emit)

const isShowDropdown = ref(false)
const items = ref(
  !!props.dropdownItems && Array.isArray(props.dropdownItems)
    ? props.dropdownItems
    : []
)
const itemRefs = ref([])
const filter = ref<string>()

const toggleDropdown = () => {
  // if it's the first time opening the dropdown and an endpoint is defined
  // --> fetch items from the backend
  if (
    !Array.isArray(props.dropdownItems) &&
    !isShowDropdown.value &&
    items.value.length == 0
  ) {
    lookupTableService
      .getAll(props.dropdownItems)
      .then((response) => (items.value = response))
  }
  isShowDropdown.value = !isShowDropdown.value
}

const clearSelection = () => {
  emit("update:modelValue", "")
}

const updateValue = (value: string) => {
  emit("update:modelValue", value)
  filter.value = ""
  isShowDropdown.value = false
}

const keyup = (index: number) => {
  const prev = itemRefs.value[index - 1] as HTMLElement
  if (prev) prev.focus()
}

const keydown = (index: number) => {
  const next = itemRefs.value[index + 1] as HTMLElement
  if (next) next.focus()
}

const onTextChange = () => {
  emit("update:modelValue", "")
  const textInput = document.querySelector(
    `.input-container #${props.id}`
  ) as HTMLInputElement
  isShowDropdown.value = true
  emit("update:modelValue", textInput.value)
  filter.value = textInput.value
}

const filterItems = () => {
  const filteredItem = items.value.filter((item) =>
    item.text.includes(!!filter.value ? filter.value : "")
  )

  return filteredItem.length > 0
    ? filteredItem
    : [{ text: "Kein passender Eintrag", value: "" }]
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

onMounted(() => {
  if (props.preselectedValue) inputValue.value = props.preselectedValue
  window.addEventListener("click", closeDropDownWhenClickOutSide)
})

onBeforeUnmount(() => {
  window.removeEventListener("click", closeDropDownWhenClickOutSide)
})
</script>

<template>
  <div :id="id" class="dropdown-container" @keydown.esc="closeDropdown">
    <div
      class="dropdown-container__open-dropdown"
      @keydown.enter="toggleDropdown"
    >
      <div class="bg-white input-container">
        <input
          :id="id"
          :aria-label="ariaLabel"
          autocomplete="off"
          class="text-input"
          :placeholder="placeholder"
          :readonly="!props.isCombobox"
          tabindex="0"
          :value="inputValue"
          @click="selectAllText"
          @input="onTextChange"
        />
        <button
          v-if="isCombobox"
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
      class="dropdown-container__dropdown-items"
      tabindex="-1"
    >
      <div
        v-for="(item, index) in isCombobox ? filterItems() : items"
        :key="index"
        ref="itemRefs"
        class="dropdown-container__dropdown-item"
        tabindex="0"
        @click="updateValue(item.value)"
        @keypress.enter="updateValue(item.value)"
        @keyup.down="keydown(index)"
        @keyup.up="keyup(index)"
      >
        <span> {{ item.text }}</span>
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
    @apply border-2 border-solid border-blue-900;

    .input-container {
      @apply hover:shadow-hover hover:shadow-blue-900 focus:shadow-focus focus:shadow-blue-900;

      display: flex;
      flex: row nowrap;
      justify-content: space-between;
      padding: 17px 24px;

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
  }
}
</style>
