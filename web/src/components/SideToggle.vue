<script lang="ts" setup>
import { computed, ref, watch } from "vue"

interface Props {
  isExpanded?: boolean
  fromSide: string
}

interface Emits {
  (event: "update:isExpanded", value: boolean): void
  (event: "toggle"): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const isExpanded = ref(false)
const closeIconName =
  props.fromSide === "left" ? "chevron_left" : "chevron_right"
const openIconName =
  props.fromSide === "left" ? "chevron_right" : "chevron_left"
const iconName = computed(() =>
  isExpanded.value ? closeIconName : openIconName
)
const classes = computed(() => ({
  "toggle-right": props.fromSide == "left",
  "toggle-left": props.fromSide == "right",
}))

function toggleContentVisibility(): void {
  isExpanded.value = !isExpanded.value
  emit("toggle")
}

watch(
  () => props.isExpanded,
  () => (isExpanded.value = props.isExpanded ?? false),
  { immediate: true }
)

watch(isExpanded, () => emit("update:isExpanded", isExpanded.value))
</script>

<template>
  <div class="bg-white border-gray-400 border-r-1 border-solid z-50">
    <button
      :aria-label="isExpanded ? 'Close Side Content' : 'Open Side Content'"
      class="-mb-[3.125rem] expandable-content__header h-[1.5rem] mt-[1.625rem] w-[1.5rem]"
      :class="classes"
      @click="toggleContentVisibility"
    >
      <span
        class="bg-white border-1 border-gray-400 border-solid material-icons rounded-full text-gray-900 w-icon"
        >{{ iconName }}</span
      >
    </button>
    <div v-show="isExpanded">
      <slot />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.toggle-left {
  @apply -ml-[0.75rem] mr-[0.75rem];

  display: flex;
  width: 100%;
  align-items: center;
  justify-content: flex-start;
}

.toggle-right {
  @apply -mr-[0.75rem] ml-[0.75rem];

  display: flex;
  width: 100%;
  align-items: center;
  justify-content: flex-end;
}
</style>
