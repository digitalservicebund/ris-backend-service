<script lang="ts" setup>
import { computed, ref, watch } from "vue"

interface Props {
  isExpanded?: boolean
  openingDirection?: OpeningDirection
  label?: string
}

interface Emits {
  (event: "update:isExpanded", value: boolean): void
  (event: "toggle"): void
}

const props = withDefaults(defineProps<Props>(), {
  isExpanded: false,
  openingDirection: OpeningDirection.RIGHT,
  label: "side toggle",
})

const emit = defineEmits<Emits>()
const isExpanded = ref(false)

const closeIconNames = {
  [OpeningDirection.LEFT]: "chevron_right",
  [OpeningDirection.RIGHT]: "chevron_left",
}

const openIconNames = {
  [OpeningDirection.RIGHT]: "chevron_right",
  [OpeningDirection.LEFT]: "chevron_left",
}

const iconName = computed(() =>
  isExpanded.value
    ? closeIconNames[props.openingDirection]
    : openIconNames[props.openingDirection]
)
const postFix = computed(() => (isExpanded.value ? "schließen" : "öffnen"))
const label = computed(() => props.label + " " + postFix.value)
const classes = computed(() => ({
  "right-0": props.openingDirection == OpeningDirection.RIGHT,
  "left-0": props.openingDirection == OpeningDirection.LEFT,
  "-mr-12": props.openingDirection == OpeningDirection.RIGHT,
  "-ml-12": props.openingDirection == OpeningDirection.LEFT,
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

<script lang="ts">
export enum OpeningDirection {
  LEFT = "left",
  RIGHT = "right",
}
</script>

<template>
  <div class="bg-white pr-[1.25rem] relative">
    <button
      :aria-label="label"
      class="absolute flex items-center top-24"
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
