<script lang="ts" setup>
import { onBeforeUnmount, onMounted, ref } from "vue"
import { useInputModel } from "@/composables/useInputModel"
import type { DropdownItem } from "@/domain/types"

interface Props {
  id: string
  value?: string
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

onMounted(() => {
  window.addEventListener("click", closeDropDownWhenClickOutSide)
})
onBeforeUnmount(() => {
  window.removeEventListener("click", closeDropDownWhenClickOutSide)
})
</script>

<template>
  <div :id="id" class="dropdown-container">
    <div
      class="dropdown-container__open-dropdown"
      @keydown.enter="toggleDropdown"
    >
      <div class="input-container">
        <input
          :id="id"
          :value="inputValue"
          :aria-label="ariaLabel"
          class="text-input"
          autocomplete="off"
          tabindex="0"
          :placeholder="placeholder"
          @input="onTextChange"
        />
        <button
          class="toggle-dropdown-button"
          tabindex="0"
          @click="toggleDropdown"
          @keydown.enter="toggleDropdown"
        >
          <span v-if="!isShowDropdown" class="material-icons icon">
            expand_more
          </span>
          <span v-else class="material-icons icon"> expand_less </span>
        </button>
      </div>
    </div>
    <div
      v-if="isShowDropdown"
      tabindex="-1"
      class="dropdown-container__dropdown-items"
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
@import "@/styles/variables";

.dropdown-container {
  width: 100%;
  position: relative;
  display: inline-block;

  /** Disable user select text */
  -webkit-user-select: none; /* Chrome all / Safari all */
  -moz-user-select: none; /* Firefox all */
  -ms-user-select: none; /* IE 10+ */
  user-select: none; /* Likely future */
  &__open-dropdown {
    .input-container {
      display: flex;
      flex: row nowrap;
      justify-content: space-between;
      padding: 17px 24px;
      border: 2px solid $text-tertiary;

      &:hover {
        outline: 4px solid $text-tertiary;
        border: none;
        margin-bottom: 4px;
      }

      &:focus {
        outline: 4px solid $text-tertiary;
        border: none;
        margin-bottom: 4px;
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
    display: flex;
    flex-direction: column;
    border: 2px solid #ececec;
    border-top: none;
    filter: drop-shadow(0 1px 3px rgb(0 0 0 / 25%));

    /** Always show on top after textbox and width equal to textbox */
    position: absolute;
    z-index: 99;
    top: 100%;
    left: 0;
    right: 0;
    max-height: 300px;

    /* hide scroll bar but still scroll */
    overflow-y: scroll;
    -ms-overflow-style: none; /* Internet Explorer 10+ */
    scrollbar-width: none; /* Firefox */
    &::-webkit-scrollbar {
      display: none; /* Chrome */
    }
  }

  &__dropdown-item {
    padding: 17px 24px;
    cursor: pointer;
    background-color: #fff;
    border-bottom: 2px solid #ececec;

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
