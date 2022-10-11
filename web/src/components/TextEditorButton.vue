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
  (e: "toggle", value: MenuButton): void
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
  <div
    :aria-label="ariaLabel"
    class="cursor-pointer editor-button flex flex-row-reverse hover:bg-blue-200 items-center text-blue-900"
    :class="{
      'bg-blue-200': isActive && !childButtons,
      'border-r-1 border-gray-400 border-solid': isLast,
    }"
    @click="onClickToggle(props)"
    @keydown.m="onClickToggle(props)"
    @mousedown.prevent=""
  >
    <span
      :id="type"
      class="leading-default px-[0.5rem]"
      :class="{ dropdown: type == 'menu', 'material-icons': icon }"
      >{{ icon }}</span
    >
    <div
      v-if="showDropdown"
      class="absolute bg-white border-1 border-blue-800 border-solid flex flex-row mt-80 pa-1 z-50"
    >
      <div
        v-for="(childButton, index) in childButtons"
        :key="index"
        :aria-label="childButton.ariaLabel"
        class="cursor-pointer dropdown-item hover:bg-blue-200 text-blue-900 z-50"
        :class="{ 'bg-blue-200': isActive, 'bg-red-800': isSecondRow }"
        @click="emits('toggle', childButton)"
        @keydown.m="emits('toggle', childButton)"
        @mousedown.prevent=""
      >
        <span class="leading-default material-icons px-2">{{
          childButton.icon
        }}</span>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.dropdown::after {
  content: "arrow_drop_down";
  font-family: "Material Icons", sans-serif;
}
</style>
