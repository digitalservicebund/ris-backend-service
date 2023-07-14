<script lang="ts" setup>
import { ref, watch } from "vue"

interface SuggestionItem {
  label: string
  id?: string
}
// Interface dictated by the TipTap editor extension called Mention.
interface Props {
  items: SuggestionItem[]
  command: (options: SuggestionItem) => void
}
const props = defineProps<Props>()
const selectedIndex = ref(0)
watch(() => props.items, resetSelection)
function resetSelection(): void {
  selectedIndex.value = 0
}
function selectNext(): void {
  selectedIndex.value = (selectedIndex.value + 1) % props.items.length
}
function selectPrevious(): void {
  selectedIndex.value =
    (selectedIndex.value + props.items.length - 1) % props.items.length
}
function chooseSelectedItem(): void {
  const item = props.items[selectedIndex.value]
  chooseItem(item)
}
function chooseItem(item: SuggestionItem): void {
  props.command(item)
}
/**
 * Handle keys forwarded by the TipTap editor extension.
 */
function onKeyDown({ event }: { event: KeyboardEvent }) {
  switch (event.key) {
    case "ArrowUp":
      selectPrevious()
      return true
    case "ArrowDown":
      selectNext()
      return true
    case "Enter":
      chooseSelectedItem()
      event.stopPropagation()
      return true
    default:
      return false
  }
}
defineExpose({ onKeyDown })
</script>

<template>
  <div class="flex flex-col border-1 border-gray-400 bg-white drop-shadow-xl">
    <template v-if="items.length">
      <button
        v-for="(item, index) in items"
        :key="item.id ?? index"
        class="item rounded-none border-b-1 border-gray-400 py-10 pl-16 pr-64 text-left hover:bg-gray-400"
        :class="{ '!bg-blue-200': index == selectedIndex }"
        @click="chooseItem(item)"
      >
        {{ item.label }}
      </button>
    </template>
    <div
      v-else
      class="item rounded-none !bg-gray-100 py-10 pl-16 pr-[65px] text-left"
    >
      Keine Ergebnisse
    </div>
  </div>
</template>
