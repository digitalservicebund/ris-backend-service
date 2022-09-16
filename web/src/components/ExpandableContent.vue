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

      <v-icon>{{ iconName }}</v-icon>
    </button>

    <v-expand-transition>
      <div v-show="isExpanded">
        <slot />
      </div>
    </v-expand-transition>
  </div>
</template>

<style lang="scss" scoped>
.expandable-content {
  width: 100%;

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
  }
}
</style>
