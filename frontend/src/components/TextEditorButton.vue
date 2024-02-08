<script lang="ts" setup>
import { onMounted, onUnmounted, ref } from "vue"
import { EditorButton } from "./utils/types"
import IconDropdown from "~icons/ic/baseline-arrow-drop-down"

const props = defineProps<EditorButton>()

const emits = defineEmits<{
  toggle: [value: EditorButton]
}>()

const showDropdown = ref(false)

function onClickToggle(button: EditorButton) {
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
  <div>
    <button
      :aria-label="ariaLabel"
      class="flex cursor-pointer p-8 text-blue-900 hover:bg-blue-200"
      :class="{
        'bg-blue-200': isActive && !childButtons,
        'border-r-1 border-solid border-gray-400': isLast,
      }"
      @click="onClickToggle(props)"
      @keydown.m="onClickToggle(props)"
      @mousedown.prevent=""
    >
      <component :is="icon" />
      <IconDropdown v-if="type === 'menu'" class="-mr-8" />
    </button>
    <div
      v-if="showDropdown"
      class="absolute z-50 mt-1 flex flex-row items-center border-1 border-solid border-blue-800 bg-white"
    >
      <button
        v-for="(childButton, index) in childButtons"
        :key="index"
        :aria-label="childButton.ariaLabel"
        class="z-50 cursor-pointer items-center p-8 text-blue-900 hover:bg-blue-200"
        :class="{
          'bg-blue-200': isActive,
          'border-r-1 border-solid border-gray-400': isLast,
        }"
        @click="emits('toggle', childButton)"
        @keydown.m="emits('toggle', childButton)"
        @mousedown.prevent=""
      >
        <component :is="childButton.icon" />
      </button>
    </div>
  </div>
</template>
