<script lang="ts" setup>
import { onBeforeUnmount, onMounted, ref, watch } from "vue"
import { useInputModel } from "@/composables/useInputModel"

interface Props {
  id: string
  value?: string
  modelValue?: string
  ariaLabel: string
  placeholder?: string
  dropdownValue: string[] | undefined
}

interface Emits {
  (event: "update:modelValue", value: string | undefined): void
  (event: "input", value: Event): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const { inputValue, emitInputEvent } = useInputModel<string, Props, Emits>(
  props,
  emit
)

const isShowDropdown = ref(false)
const items = ref(!!props.dropdownValue ? props.dropdownValue : [])

const toggleDropdown = () => {
  isShowDropdown.value = !isShowDropdown.value
}

const filterItems = () => {
  const filteredItem = items.value.filter((item) =>
    item.includes(!!props.modelValue ? props.modelValue : "")
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

watch(
  () => props.modelValue,
  () => {
    if (!isShowDropdown.value) isShowDropdown.value = true
  }
)

onMounted(() => {
  window.addEventListener("click", closeDropDownWhenClickOutSide)
})
onBeforeUnmount(() => {
  window.removeEventListener("click", closeDropDownWhenClickOutSide)
})
</script>

<template>
  <div :id="id" class="dropdown-container" style="width: 100%">
    <div
      class="dropdown-container__open-dropdown"
      @keydown.enter="toggleDropdown"
    >
      <div class="input-container">
        <input
          :id="id"
          v-model="inputValue"
          :aria-label="ariaLabel"
          class="text-input"
          autocomplete="off"
          tabindex="0"
          :placeholder="placeholder"
          @input="emitInputEvent"
        />
        <button
          class="toggle_dropdown_button"
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
        @click="$emit('update:modelValue', item)"
        @keypress.enter="$emit('update:modelValue', item)"
      >
        <span> {{ item }}</span>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
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
      flex-direction: row;
      flex-wrap: nowrap;
      justify-content: space-between;
      padding: 17px 24px;
      border: 2px solid $text-tertiary;
      &:hover {
        border: 4px solid $text-tertiary;
      }
      &:focus {
        border: 4px solid $text-tertiary;
        outline: none;
      }
      .text-input {
        width: 100%;
        &:focus {
          outline: none;
        }
      }
      .toggle_dropdown_button {
        height: 5px;
      }
    }
  }
  &__dropdown-items {
    display: flex;
    flex-direction: column;
    max-height: 300px;
    overflow-y: scroll;
    position: absolute;
    border: 2px solid #ececec;
    border-top: none;
    z-index: 99;
    filter: drop-shadow(0px 1px 3px rgba(0, 0, 0, 0.25));
    width: 100%;
    /*hide scroll bar */
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
