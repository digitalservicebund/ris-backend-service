<script lang="ts" setup>
import { onBeforeUnmount, onMounted, ref } from "vue"
import { useInputModel } from "@/composables/useInputModel"
import type { DropdownItem } from "@/domain/types"

interface Props {
  id: string
  value?: string // @public
  modelValue?: string
  ariaLabel: string
  placeholder?: string
  dropdownItems: DropdownItem[] | undefined
}

interface Emits {
  (event: "update:modelValue", value: string | undefined): void
  (event: "input", value: Event): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const { inputValue } = useInputModel<string, Props, Emits>(props, emit)

const isShowDropdown = ref(false)
const items = ref(!!props.dropdownItems ? props.dropdownItems : [])

const toggleDropdown = () => {
  isShowDropdown.value = !isShowDropdown.value
}

const updateValue = (value: string) => {
  emit("update:modelValue", value)
  isShowDropdown.value = false
}

const onTextChange = () => {
  const textInput = document.querySelector(
    `.input-container #${props.id}`
  ) as HTMLInputElement
  isShowDropdown.value = true
  emit("update:modelValue", textInput.value)
}

const filterItems = () => {
  const filteredItem = items.value.filter((item) =>
    item.text.includes(!!props.modelValue ? props.modelValue : "")
  )
  return filteredItem.length > 0 ? filteredItem : items.value
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
          tabindex="0"
          :value="inputValue"
          @click="selectAllText"
          @input="onTextChange"
        />
        <button
          class="toggle-dropdown-button"
          tabindex="0"
          @click="toggleDropdown"
          @keydown.enter="toggleDropdown"
        >
          <span v-if="!isShowDropdown" class="icon material-icons">
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
        v-for="(item, index) in filterItems()"
        :key="index"
        class="dropdown-container__dropdown-item"
        tabindex="0"
        @click="updateValue(item.value)"
        @keypress.enter="updateValue(item.value)"
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
      display: flex;
      flex: row nowrap;
      justify-content: space-between;
      padding: 17px 24px;

      &:hover {
        @apply hover:outline-4 hover:outline hover:outline-blue-900;
      }

      &:focus {
        @apply focus:outline-4 focus:outline focus:outline-blue-900;
      }

      .text-input {
        width: 100%;

        &:focus {
          outline: none;
        }
      }

      .toggle-dropdown-button {
        height: 5px;
      }
    }
  }

  &__dropdown-items {
    /** Always show on top after textbox and width equal to textbox */
    position: absolute;
    z-index: 99;
    top: 100%;
    right: 0;
    left: 0;
    display: flex;
    max-height: 300px;
    flex-direction: column;
    border: 2px solid #ececec;
    border-top: none;
    filter: drop-shadow(0 1px 3px rgb(0 0 0 / 25%));
    overflow-y: scroll;
    scrollbar-width: none;
  }

  &__dropdown-item {
    padding: 17px 24px;
    border-bottom: 2px solid #ececec;
    background-color: #fff;
    cursor: pointer;

    &:last-of-type {
      border-bottom: none;
    }

    &:hover {
      background-color: #ececec;
    }

    &:focus {
      background-color: #ececec;
      outline: none;
    }
  }
}
</style>
