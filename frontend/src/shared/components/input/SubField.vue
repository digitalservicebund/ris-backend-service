<script lang="ts" setup>
import { computed, ref, watch, onMounted } from "vue"

interface Props {
  ariaLabel: string
  isExpanded?: boolean
  iconExpanding?: string
  iconClosing?: string
}
interface Emits {
  (event: "update:isExpanded", value: boolean): void
}
const props = withDefaults(defineProps<Props>(), {
  isExpanded: false,
  iconExpanding: "add",
  iconClosing: "horizontal_rule",
})
const emit = defineEmits<Emits>()
const expandableContainer = ref()
const containerHeight = ref(0)
const isExpanded = ref(false)
const iconName = computed(() =>
  isExpanded.value ? props.iconClosing : props.iconExpanding
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
  <div class="expandable-content">
    <button
      class="-mt-[4.5rem] expandable-content__header h-[1.25rem] text-white"
      @click="toggleContentVisibility"
    >
      <span
        :aria-label="
          isExpanded ? ariaLabel + ' schließen' : ariaLabel + ' anzeigen'
        "
        class="bg-blue-800 material-icons rounded-full w-icon"
        >{{ iconName }}</span
      >
    </button>
    <div
      ref="expandableContainer"
      class="expandable"
      :class="{ expanded: isExpanded }"
      :style="{ height: containerHeight.valueOf + 'px' }"
    >
      <transition name="expand">
        <div v-show="isExpanded">
          <slot />
        </div>
      </transition>
    </div>
  </div>
</template>

<!-- Transitions are difficult to handle with dynamic heights in vue. To use the max-height as 
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

.expandable-content {
  width: 100%;

  &__header {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    margin-left: 100%;

    .w-icon {
      position: absolute;
      right: -11px;
    }
  }
}
</style>
