<script lang="ts" setup>
import { computed, ref, watch } from "vue"

interface Props {
  header?: string
  isExpanded?: boolean
  openIconName?: string
  closeIconName?: string
}
interface Emits {
  (event: "update:isExpanded", value: boolean): void
}
const props = withDefaults(defineProps<Props>(), {
  header: undefined,
  isExpanded: false,
  openIconName: "add",
  closeIconName: "horizontal_rule",
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
  containerHeight.value =
    expandableContainer.value.getBoundingClientRect().height
}
watch(
  () => props.isExpanded,
  () => (isExpanded.value = props.isExpanded ?? false),
  { immediate: true }
)
watch(isExpanded, () => emit("update:isExpanded", isExpanded.value))
</script>
<template>
  <div class="expandable-content">
    <button
      aria-label="Toggle Content Visibility"
      class="expandable-content__header"
      @click="toggleContentVisibility"
    >
      <slot name="header">
        <span>{{ header }}</span>
      </slot>
      <span class="icon material-icons">{{ iconName }}</span>
    </button>
    <transition
      ref="expandableContainer"
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
<style lang="scss" scoped>
.expand-enter-from {
  max-height: 0;
}

.expand-enter-to {
  max-height: 600px;
}

.expand-enter-active {
  overflow: hidden;
  transition: all 0.5s ease-in-out;
}

.expand-leave-from {
  max-height: 600px;
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
    display: flex;
    width: 100%;
    align-items: center;
    justify-content: space-between;
  }

  .icon {
    cursor: pointer;
  }
}
</style>
