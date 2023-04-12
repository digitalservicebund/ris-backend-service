<script lang="ts" setup>
import { computed, ref, watch } from "vue"

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

    <div v-if="isExpanded">
      <slot />
    </div>
  </div>
</template>
