<!-- eslint-disable vue/multi-word-component-names -->
<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from "vue"

interface Props {
  isVisible?: boolean
  text: string
  shortcut?: string
}
const props = withDefaults(defineProps<Props>(), {
  isVisible: false,
  shortcut: undefined,
})

const handleKeyDown = (e: KeyboardEvent) => {
  if (e.key === "Escape") {
    closeTooltip()
  }
}

const tooltipVisible = ref(props.isVisible)

onMounted(() => {
  tooltipVisible.value = props.isVisible

  watch(
    () => props.isVisible,
    (newValue) => {
      tooltipVisible.value = newValue
    },
  )

  // Add the keydown event listener when the component is mounted
  window.addEventListener("keydown", handleKeyDown)
})

// Cleanup the event listener when the component is unmounted
onUnmounted(() => {
  window.removeEventListener("keydown", handleKeyDown)
})

const openTooltip = () => {
  tooltipVisible.value = true
}

const closeTooltip = () => {
  tooltipVisible.value = false
}
</script>

<template>
  <!-- eslint-disable-next-line vuejs-accessibility/no-static-element-interactions -->
  <div
    class="relative"
    @blur="closeTooltip"
    @focus="openTooltip"
    @focusin="openTooltip"
    @focusout="closeTooltip"
    @mouseenter="openTooltip"
    @mouseleave="closeTooltip"
    @touchstart="openTooltip"
  >
    <slot />
    <div
      v-if="tooltipVisible"
      class="ds-label-03-reg absolute left-[50%] top-[110%] z-20 w-max max-w-[18em] translate-x-[-50%] rounded bg-gray-900 px-8 py-4 text-center text-white"
      role="tooltip"
    >
      {{ props.text }}<br v-if="props.shortcut" />
      {{ props.shortcut }}
    </div>
  </div>
</template>
