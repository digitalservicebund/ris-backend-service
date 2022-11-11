<script lang="ts" setup>
import { computed, ref, watch } from "vue"

interface Props {
  isExpanded?: boolean
  fromSide: string
  label: string
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
const postFix = computed(() => (isExpanded.value ? "schließen" : "öffnen"))
const label = computed(() => props.label + " " + postFix.value)
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
  <div class="bg-white pr-[1.25rem] relative">
    <button
      :aria-label="label"
      class="absolute align-center flex mt-[1.625rem] w-full"
      :class="classes"
      @click="toggleContentVisibility"
    >
      <span
        class="bg-white border-1 border-gray-400 border-solid material-icons rounded-full text-22 text-gray-900 w-icon"
        >{{ iconName }}</span
      >
    </button>
    <div v-show="isExpanded" class="-mr-[1.25rem]">
      <slot />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.toggle-left {
  @apply justify-start -ml-[0.75rem] mr-[0.75rem];
}

.toggle-right {
  @apply justify-end -mr-[0.75rem] ml-[0.75rem];
}
</style>
