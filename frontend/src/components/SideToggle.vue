<script lang="ts" setup>
import { computed } from "vue"
import IconChevronLeft from "~icons/ic/baseline-chevron-left"
import IconChevronRight from "~icons/ic/baseline-chevron-right"

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
}>()

const postFix = computed(() => (props.isExpanded ? "schließen" : "öffnen"))

const classes = computed(() => ({
  "right-0": props.openingDirection == OpeningDirection.RIGHT,
  "left-0": props.openingDirection == OpeningDirection.LEFT,
  "-mr-12": props.openingDirection == OpeningDirection.RIGHT,
  "-ml-12": props.openingDirection == OpeningDirection.LEFT,
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
  <div class="relative bg-white pr-[1.25rem]">
    <button
      :aria-label="props.label + ' ' + postFix"
      class="absolute top-28 z-20 flex items-center"
      :class="classes"
    >
      <span
        class="w-icon rounded-full border-1 border-solid border-gray-400 bg-white text-16 text-gray-900"
      >
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
