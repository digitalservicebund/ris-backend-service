<script lang="ts" setup>
import { onMounted, onUnmounted, ref } from "vue"

const props = defineProps<{
  id: string
  value: string
  dropdownValues: string[] | undefined
}>()
const emit = defineEmits<{
  (e: "updateValue", id: string, textVal: string): void
}>()
const forcusedItemIndex = ref(-1)
const isShowDropdown = ref(false)
const items = ref(!!props.dropdownValues ? props.dropdownValues : [])

const setTextVal = (value: string) => {
  emit("updateValue", props.id, value)
  closeDropdown()
}
const filterItems = () => {
  const filteredItems = items.value.filter((item) => item.includes(props.value))
  return filteredItems.length > 0 ? filteredItems : items.value
}

const selectItems = (event: KeyboardEvent) => {
  const isArrowDownPressed = event.key === "ArrowDown"
  const isArrowUpPressed = event.key === "ArrowUp"
  const isTabPressed = event.key === "Tab"
  if (!isShowDropdown.value) return
  if (isTabPressed) {
    closeDropdown()
    return
  }
  if (!isArrowDownPressed && !isArrowUpPressed) return
  const dropdownItems = document.querySelectorAll(
    `#${props.id} .dropdown-container__dropdown-item`
  )
  forcusedItemIndex.value = isArrowDownPressed
    ? forcusedItemIndex.value + 1
    : forcusedItemIndex.value - 1
  if (forcusedItemIndex.value < 0) forcusedItemIndex.value = 0
  if (forcusedItemIndex.value > dropdownItems.length - 1)
    forcusedItemIndex.value = dropdownItems.length - 1
  if (!!dropdownItems[forcusedItemIndex.value]) {
    ;(dropdownItems[forcusedItemIndex.value] as HTMLElement).focus()
  }
}

const closeDropdownWhenClickOutSide = (event: MouseEvent) => {
  const dropdown = document.querySelector(
    `#${props.id} .dropdown-container__open-dropdown`
  )
  if (
    event.target !== dropdown &&
    (event.target as HTMLElement).parentElement !== dropdown
  ) {
    closeDropdown()
  }
}

const closeDropdown = () => {
  forcusedItemIndex.value = -1
  isShowDropdown.value = false
}

onMounted(async () => {
  window.addEventListener("keydown", selectItems, false)
  window.addEventListener("click", closeDropdownWhenClickOutSide)
})
onUnmounted(() => {
  window.removeEventListener("keydown", selectItems)
  window.removeEventListener("click", closeDropdownWhenClickOutSide)
})
</script>

<template>
  <div :id="id" class="dropdown-container" style="width: 100%">
    <div
      class="dropdown-container__open-dropdown"
      @click="isShowDropdown = true"
      @keydown.enter="isShowDropdown = true"
    >
      <slot></slot>
    </div>
    <div v-if="isShowDropdown" class="dropdown-container__dropdown-items">
      <div
        v-for="(item, index) in filterItems()"
        :key="index"
        class="dropdown-container__dropdown-item"
        tabindex="-1"
        @click="setTextVal(item)"
        @keypress.enter="setTextVal(item)"
      >
        <span> {{ item }}</span>
      </div>
    </div>
  </div>
</template>

<style lang="scss">
.dropdown-container {
  &__dropdown-items {
    display: flex;
    flex-direction: column;
    max-height: 300px;
    overflow-y: scroll;
    position: absolute;
    border: 2px solid $text-tertiary;
    border-top: none;
    z-index: 99;
    /*position the autocomplete items to be the same width as the container:*/
    top: 100%;
    left: 0;
    right: 0;
    /*hide scroll bar */
    -ms-overflow-style: none; /* Internet Explorer 10+ */
    scrollbar-width: none; /* Firefox */
    &::-webkit-scrollbar {
      display: none; /* Chrome */
    }
  }
  &__dropdown-item {
    padding: 10px;
    cursor: pointer;
    background-color: #fff;
    border-bottom: 2px solid $text-tertiary;
    &:last-of-type {
      border-bottom: none;
    }
    &:hover {
      background-color: #e9e9e9;
    }
    &:focus {
      background-color: #e9e9e9;
      outline: none;
    }
  }
}
</style>
