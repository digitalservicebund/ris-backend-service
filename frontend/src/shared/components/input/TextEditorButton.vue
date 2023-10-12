<script lang="ts" setup>
import { onMounted, onUnmounted, ref } from "vue"

interface MenuButton {
  type: string
  icon: string
  ariaLabel: string
  childButtons?: MenuButton[]
  isLast?: boolean
  isActive?: boolean
  isSecondRow?: boolean
  isCollapsable?: boolean
  callback?: () => void
}
const props = defineProps<MenuButton>()

const emits = defineEmits<{
  toggle: [value: MenuButton]
}>()

const showDropdown = ref(false)

function onClickToggle(button: MenuButton) {
  if (!button.childButtons || button.type === "more") emits("toggle", button)
  else if (button.type === "menu") showDropdown.value = !showDropdown.value
}

const closeDropDownWhenClickOutSide = (event: MouseEvent) => {
  if (props.type) {
    const button = document.querySelector(`#${props.type}`)
    if (button == null) return
    if ((event.target as HTMLElement) === button) return
    if ((event.target as HTMLElement).id === "menu") return
    showDropdown.value = false
  }
}

onMounted(() => {
  document.addEventListener("click", closeDropDownWhenClickOutSide)
})
onUnmounted(() => {
  document.removeEventListener("click", closeDropDownWhenClickOutSide)
})
</script>

<template>
  <button
    :aria-label="ariaLabel"
    class="editor-button flex cursor-pointer flex-row-reverse items-center text-blue-900 hover:bg-blue-200"
    :class="{
      'bg-blue-200': isActive && !childButtons,
      'border-r-1 border-solid border-gray-400': isLast,
    }"
    @click="onClickToggle(props)"
    @keydown.m="onClickToggle(props)"
    @mousedown.prevent=""
  >
    <!-- TODO: render dynamic vue icon component based on props -->
    <span
      class="px-[0.5rem] leading-default"
      :class="{ dropdown: type == 'menu', 'material-icons': icon }"
      >{{ icon }}</span
    >
    <div
      v-if="showDropdown"
      class="pa-1 absolute z-50 mt-80 flex flex-row border-1 border-solid border-blue-800 bg-white"
    >
      <button
        v-for="(childButton, index) in childButtons"
        :key="index"
        :aria-label="childButton.ariaLabel"
        class="dropdown-item z-50 cursor-pointer text-blue-900 hover:bg-blue-200"
        :class="{ 'bg-blue-200': isActive, 'bg-red-800': isSecondRow }"
        @click="emits('toggle', childButton)"
        @keydown.m="emits('toggle', childButton)"
        @mousedown.prevent=""
      >
        <span class="material-icons px-2 leading-default">{{
          childButton.icon
        }}</span>
      </button>
    </div>
  </button>
</template>

<style lang="scss" scoped>
.dropdown::after {
  content: "arrow_drop_down";
  font-family: "Material Icons", sans-serif;
}
</style>
