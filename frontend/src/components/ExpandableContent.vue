<script lang="ts" setup>
import { computed, ref, watch, onMounted } from "vue"

interface Props {
  header?: string
  isExpanded?: boolean
  openIconName?: string
  closeIconName?: string
  headerId?: string
}
interface Emits {
  (event: "update:isExpanded", value: boolean): void
}
const props = withDefaults(defineProps<Props>(), {
  header: undefined,
  isExpanded: false,
  openIconName: "add",
  closeIconName: "horizontal_rule",
  headerId: "",
})
const emit = defineEmits<Emits>()
const expandableContainer = ref()
const containerHeight = ref(0)
const isExpanded = ref(false)
const iconName = computed(() =>
  isExpanded.value ? props.closeIconName : props.openIconName
)
function toggleContentVisibility(): void {
  isExpanded.value = !isExpanded.value
}
watch(
  () => props.isExpanded,
  () => (isExpanded.value = props.isExpanded ?? false),
  { immediate: true }
)
watch(isExpanded, () => emit("update:isExpanded", isExpanded.value))

onMounted(() => {
  const expandableContainer = document.querySelector(".expandable")
  if (expandableContainer != null) resizeObserver.observe(expandableContainer)
})

const resizeObserver = new ResizeObserver((entries) => {
  for (const entry of entries) {
    containerHeight.value = entry.contentRect.width
  }
})
</script>
<template>
  <div class="mb-[4rem]">
    <button
      :aria-labelledby="headerId"
      class="flex justify-between w-full"
      @click="toggleContentVisibility"
    >
      <slot name="header">
        <span>{{ header }}</span>
      </slot>

      <span class="icon material-icons">{{ iconName }}</span>
    </button>

    <transition
      ref="expandableContainer"
      class="expandable"
      :class="{ expanded: isExpanded }"
      name="expand"
      :style="{ height: containerHeight.valueOf + 'px' }"
    >
      <div v-show="isExpanded">
        <slot />
      </div>
    </transition>
  </div>
</template>

<!-- Transitions are difficult to handle with dynamic heights. To use the max-height as
  transition parameter is a known workaround for this issue, the max-height doesn't have
  an effect on the actual height, but is just used to get the transition effect. -->

<style lang="scss" scoped>
.expand-enter-from {
  max-height: 0;
}

.expand-enter-to {
  max-height: 1000px;
}

.expand-enter-active {
  overflow: hidden;
  transition: all 0.5s ease-in-out;
}

.expand-leave-from {
  max-height: 1000px;
}

.expand-leave-to {
  max-height: 0;
}

.expand-leave-active {
  overflow: hidden;
  transition: all 0.5s ease-in-out;
}
</style>
