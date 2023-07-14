<script lang="ts" setup>
import { computed, ref, watch } from "vue"

interface Props {
  isExpanded?: boolean
  openingDirection?: OpeningDirection
  label?: string
}

const props = withDefaults(defineProps<Props>(), {
  isExpanded: false,
  openingDirection: OpeningDirection.RIGHT,
  label: "side toggle",
})

const emit = defineEmits<{
  "update:isExpanded": [value: boolean]
  toggle: [void]
}>()

const localIsExpanded = ref(false)

const closeIconNames = {
  [OpeningDirection.LEFT]: "chevron_right",
  [OpeningDirection.RIGHT]: "chevron_left",
}

const openIconNames = {
  [OpeningDirection.RIGHT]: "chevron_right",
  [OpeningDirection.LEFT]: "chevron_left",
}

const iconName = computed(() =>
  localIsExpanded.value
    ? closeIconNames[props.openingDirection]
    : openIconNames[props.openingDirection],
)

const postFix = computed(() => (localIsExpanded.value ? "schließen" : "öffnen"))
const label = computed(() => props.label + " " + postFix.value)

const classes = computed(() => ({
  "right-0": props.openingDirection == OpeningDirection.RIGHT,
  "left-0": props.openingDirection == OpeningDirection.LEFT,
  "-mr-12": props.openingDirection == OpeningDirection.RIGHT,
  "-ml-12": props.openingDirection == OpeningDirection.LEFT,
}))

function toggleContentVisibility(): void {
  localIsExpanded.value = !localIsExpanded.value
  emit("toggle")
}

watch(
  () => props.isExpanded,
  () => (localIsExpanded.value = props.isExpanded ?? false),
  { immediate: true },
)

watch(localIsExpanded, () => emit("update:isExpanded", localIsExpanded.value))
</script>

<script lang="ts">
export enum OpeningDirection {
  LEFT = "left",
  RIGHT = "right",
}
</script>

<template>
  <div class="relative bg-white pr-[1.25rem]">
    <button
      :aria-label="label"
      class="absolute top-28 z-20 flex items-center"
      :class="classes"
      @click="toggleContentVisibility"
    >
      <span
        class="material-icons w-icon rounded-full border-1 border-solid border-gray-400 bg-white text-22 text-gray-900"
        >{{ iconName }}</span
      >
    </button>
    <div v-show="localIsExpanded" class="-mr-[1.25rem]">
      <slot />
    </div>
  </div>
</template>
