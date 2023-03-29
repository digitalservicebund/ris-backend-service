<script lang="ts" setup>
import { computed } from "vue"

interface Props {
  id: string
  label?: string
  errorMessage?: string
  required?: boolean
  labelPosition?: LabelPosition
}

const props = withDefaults(defineProps<Props>(), {
  errorMessage: undefined,
  required: false,
  labelPosition: LabelPosition.TOP,
  label: undefined,
})

const wrapperClasses = computed(() => ({
  "flex-col":
    props.labelPosition === LabelPosition.TOP ||
    props.labelPosition === LabelPosition.BOTTOM,
  "flex-row gap-4":
    props.labelPosition === LabelPosition.RIGHT ||
    props.labelPosition === LabelPosition.LEFT,
}))
</script>

<script lang="ts">
export enum LabelPosition {
  TOP = "top",
  BOTTOM = "bottom",
  RIGHT = "right",
  LEFT = "left",
}
</script>

<template>
  <div class="flex flex-start" :class="wrapperClasses">
    <!-- slot rendered BEFORE label if the label position should be to the right or bottom -->
    <slot
      v-if="
        labelPosition === LabelPosition.RIGHT ||
        labelPosition === LabelPosition.BOTTOM
      "
      :id="id"
    />

    <label
      v-if="label"
      :aria-label="id"
      class="flex gap-4 items-center label-03-reg mb-2 text-gray-900"
      :for="id"
    >
      {{ label }}
      <span v-if="!!required">*</span>
    </label>

    <!-- slot rendered AFTER label, if the label position should be to the left or top -->
    <slot
      v-if="
        labelPosition === LabelPosition.LEFT ||
        labelPosition === LabelPosition.TOP
      "
      :id="id"
    />

    <div class="h-16 label-03-reg text-red-800">
      {{ errorMessage }}
    </div>
  </div>
</template>
