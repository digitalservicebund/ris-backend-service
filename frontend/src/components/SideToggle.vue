<script lang="ts" setup>
import { computed } from "vue"
import IconChevronLeft from "~icons/ic/baseline-chevron-left"
import IconChevronRight from "~icons/ic/baseline-chevron-right"

interface Props {
  isExpanded?: boolean
  openingDirection?: OpeningDirection
  label?: string
  size?: "small" | "medium"
}

const props = withDefaults(defineProps<Props>(), {
  isExpanded: false,
  openingDirection: OpeningDirection.RIGHT,
  label: "side toggle",
  size: "medium",
})

const emit = defineEmits<{
  "update:isExpanded": [value: boolean]
}>()

const postFix = computed(() => (props.isExpanded ? "schließen" : "öffnen"))

const classes = computed(() => ({
  "right-0": props.openingDirection == OpeningDirection.RIGHT,
  "left-0": props.openingDirection == OpeningDirection.LEFT,
  "-mr-12":
    props.openingDirection == OpeningDirection.RIGHT && props.size === "small",
  "-ml-12":
    props.openingDirection == OpeningDirection.LEFT && props.size === "small",
  "-mr-20":
    props.openingDirection == OpeningDirection.RIGHT && props.size === "medium",
  "-ml-20":
    props.openingDirection == OpeningDirection.LEFT && props.size === "medium",
}))

const iconClasses = computed(() => ({
  "w-icon rounded-full border-1 border-solid border-gray-400 bg-white text-gray-900 text-16":
    props.size === "small",
  "w-icon rounded-full border-1 border-solid border-gray-400 bg-white text-gray-900 p-6 text-20":
    props.size === "medium",
}))

const toggle = () => {
  emit("update:isExpanded", !props.isExpanded)
}
</script>

<script lang="ts">
export enum OpeningDirection {
  LEFT = "left",
  RIGHT = "right",
}
</script>

<template>
  <div
    class="relative bg-white"
    :class="[props.size === 'small' ? 'pr-[1.25rem]' : 'pr-[2.25rem]']"
  >
    <button
      :aria-label="props.label + ' ' + postFix"
      class="absolute top-28 z-20 flex items-center"
      :class="classes"
    >
      <span :class="iconClasses">
        <IconChevronLeft
          v-if="
            props.openingDirection === OpeningDirection.LEFT
              ? !isExpanded
              : isExpanded
          "
          @click="toggle"
        />
        <IconChevronRight v-else @click="toggle" />
      </span>
    </button>
    <div v-show="isExpanded" class="-mr-[1.25rem]">
      <slot />
    </div>
  </div>
</template>
