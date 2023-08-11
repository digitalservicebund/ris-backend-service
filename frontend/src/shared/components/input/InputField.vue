<script lang="ts" setup>
import { computed, ref, watchEffect } from "vue"
import { ValidationError } from "@/shared/components/input/types"
import { useGlobalValidationErrorStore } from "@/stores/globalValidationErrorStore"

interface Props {
  id: string
  label: string | string[]
  required?: boolean
  labelPosition?: LabelPosition
  validationError?: ValidationError
  visuallyHideLabel?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  validationError: undefined,
  required: false,
  labelPosition: LabelPosition.TOP,
})

defineSlots<{
  default(props: {
    id: Props["id"]
    hasError: boolean
    updateValidationError: typeof updateValidationError
  }): unknown
}>()

/* -------------------------------------------------- *
 * Label                                              *
 * -------------------------------------------------- */

const wrapperClasses = computed(() => ({
  "flex-col": props.labelPosition === LabelPosition.TOP,
  "flex-row": props.labelPosition === LabelPosition.RIGHT,
}))

const labelConverted = computed(() => {
  if (props.label) {
    return Array.isArray(props.label) ? props.label : [props.label]
  } else return []
})

/* -------------------------------------------------- *
 * Validation error handling                          *
 * -------------------------------------------------- */
const { getByInstance } = useGlobalValidationErrorStore()
const localValidationError = ref(props.validationError)

function updateValidationError(newValidationError?: ValidationError) {
  localValidationError.value = newValidationError
}

const storeValidationError = computed(() => getByInstance(props.id).value[0])

watchEffect(() => {
  if (props.validationError) {
    localValidationError.value = props.validationError
  }

  if (getByInstance(props.id).value[0]) {
    localValidationError.value = storeValidationError.value
  }

  if (!props.validationError && !storeValidationError.value) {
    localValidationError.value = undefined
  }
})
</script>

<script lang="ts">
export enum LabelPosition {
  TOP = "top",
  RIGHT = "right",
}
</script>

<template>
  <div class="flex-start mb-16 flex w-full gap-4" :class="wrapperClasses">
    <div
      v-if="labelConverted.length !== 0"
      class="flex flex-row items-center"
      :class="{
        'order-1': labelPosition === LabelPosition.RIGHT,
        'sr-only': visuallyHideLabel,
      }"
      data-testid="label-wrapper"
    >
      <span
        v-if="localValidationError"
        class="material-icons pr-4 text-red-800"
      >
        error_outline
      </span>

      <label
        class="ds-label-02-reg grid items-center"
        :class="{ 'pl-4': labelPosition === LabelPosition.RIGHT }"
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

    <div class="flex flex-row items-center">
      <slot
        :id="id"
        :has-error="!!validationError"
        :update-validation-error="updateValidationError"
      />
    </div>

    <div
      v-if="localValidationError"
      class="ds-label-03-reg h-16 text-red-800"
      :data-testid="id + '-validationError'"
    >
      {{ localValidationError.message }}
    </div>
  </div>
</template>
