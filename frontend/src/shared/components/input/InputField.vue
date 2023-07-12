<script lang="ts" setup>
import { computed } from "vue"

interface Props {
  id: string
  label?: string | string[]
  required?: boolean
  labelPosition?: LabelPosition
  validationError?: string
}

const props = withDefaults(defineProps<Props>(), {
  validationError: undefined,
  required: false,
  labelPosition: LabelPosition.TOP,
  label: undefined,
})

const wrapperClasses = computed(() => ({
  "flex-col":
    props.labelPosition === LabelPosition.TOP ||
    props.labelPosition === LabelPosition.BOTTOM,
  "flex-row":
    props.labelPosition === LabelPosition.RIGHT ||
    props.labelPosition === LabelPosition.LEFT,
}))

const labelConverted = computed(() =>
  Array.isArray(props.label) ? props.label : Array.of(props.label)
)
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
  <div class="flex flex-start mb-16 w-full" :class="wrapperClasses">
    <!-- slot rendered BEFORE label if the label position should be to the right or bottom -->
    <slot
      v-if="
        labelPosition === LabelPosition.RIGHT ||
        labelPosition === LabelPosition.BOTTOM
      "
      :id="id"
      :has-error="!!validationError"
    />

    <div
      class="flex flex-row items-center"
      :class="{
        'mb-4': !id.includes('nested') && labelPosition === LabelPosition.TOP,
        'min-h-[24px]': !id.includes('nested'),
      }"
    >
      <span v-if="validationError" class="material-icons pr-4 text-red-800"
        >error_outline</span
      >
      <label
        v-if="labelConverted.length !== 0"
        :aria-label="id"
        class="grid items-center label-03-reg text-gray-900"
        :class="{
          'pr-4': labelPosition === LabelPosition.LEFT,
          'pl-4': labelPosition === LabelPosition.RIGHT,
        }"
        :for="id"
      >
        <span v-for="(line, index) in labelConverted" :key="line">
          {{ line }}
          <span
            v-if="index === labelConverted.length - 1 && !!required"
            class="ml-4"
            >*</span
          >
        </span>
      </label>
    </div>

    <!-- slot rendered AFTER label, if the label position should be to the left or top -->
    <slot
      v-if="
        labelPosition === LabelPosition.LEFT ||
        labelPosition === LabelPosition.TOP
      "
      :id="id"
      :has-error="!!validationError"
    />

    <div v-if="validationError" class="h-16 label-03-reg my-8 text-red-800">
      {{ validationError }}
    </div>
  </div>
</template>
