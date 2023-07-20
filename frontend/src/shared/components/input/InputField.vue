<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { ValidationError } from "@/shared/components/input/types"

interface Props {
  id: string
  label?: string | string[]
  required?: boolean
  labelPosition?: LabelPosition
  validationError?: ValidationError
}

const props = withDefaults(defineProps<Props>(), {
  validationError: undefined,
  required: false,
  labelPosition: LabelPosition.TOP,
  label: undefined,
})

defineSlots<{
  default(props: {
    id: Props["id"]
    hasError: boolean
    updateValidationError: typeof updateValidationError
  }): any // eslint-disable-line @typescript-eslint/no-explicit-any
}>()

const localValidationError = ref()

const wrapperClasses = computed(() => ({
  "flex-col":
    props.labelPosition === LabelPosition.TOP ||
    props.labelPosition === LabelPosition.BOTTOM,
  "flex-row":
    props.labelPosition === LabelPosition.RIGHT ||
    props.labelPosition === LabelPosition.LEFT,
}))

const labelConverted = computed(() =>
  Array.isArray(props.label) ? props.label : Array.of(props.label),
)

function updateValidationError(newValidationError?: ValidationError) {
  localValidationError.value = newValidationError
}

watch(
  props,
  () => {
    localValidationError.value = props.validationError ?? undefined
  },
  { immediate: true },
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
  <div class="flex-start mb-16 flex w-full" :class="wrapperClasses">
    <!-- slot rendered BEFORE label if the label position should be to the right or bottom -->
    <slot
      v-if="
        labelPosition === LabelPosition.RIGHT ||
        labelPosition === LabelPosition.BOTTOM
      "
      :id="id"
      :has-error="!!validationError"
      :update-validation-error="updateValidationError"
    />

    <div
      class="flex flex-row items-center"
      :class="{
        'mb-4': !id.includes('nested') && labelPosition === LabelPosition.TOP,
        'min-h-[24px]': !id.includes('nested'),
      }"
    >
      <span v-if="localValidationError" class="material-icons pr-4 text-red-800"
        >error_outline</span
      >
      <label
        v-if="labelConverted.length !== 0"
        class="ds-label-02-reg grid items-center"
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
      :update-validation-error="updateValidationError"
    />

    <div
      v-if="localValidationError"
      class="ds-label-03-reg my-6 h-16 text-red-800"
    >
      {{ localValidationError.defaultMessage }}
    </div>
  </div>
</template>
