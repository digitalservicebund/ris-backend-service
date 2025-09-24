<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { ValidationError } from "@/components/input/types"
import errors from "@/i18n/errors.json"
import { isErrorCode } from "@/i18n/utils"
import IconErrorOutline from "~icons/ic/baseline-error-outline"

interface Props {
  id: string
  label: string | string[]
  required?: boolean
  labelPosition?: LabelPosition
  labelClass?: string
  validationError?: ValidationError
  visuallyHideLabel?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  required: false,
  labelPosition: LabelPosition.TOP,
  labelClass: undefined,
  validationError: undefined,
})

const emit = defineEmits<{
  "update:validationError": [value?: ValidationError]
}>()

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

function updateValidationError(newValidationError?: ValidationError) {
  localValidationError.value = newValidationError
  if (newValidationError)
    emit("update:validationError", {
      message: newValidationError.message,
      instance: newValidationError.instance,
    })
  else emit("update:validationError", undefined)
}

const localValidationError = ref<ValidationError | undefined>(
  props.validationError,
)

const errorMessage = computed(() => {
  if (!localValidationError.value) return undefined

  const { code, message } = localValidationError.value
  if (code && isErrorCode(code)) return errors[code].title
  else return message
})

watch(
  () => props.validationError,
  (is) => {
    if (is) {
      localValidationError.value = is
    } else {
      localValidationError.value = undefined
    }
  },
)
</script>

<script lang="ts">
export enum LabelPosition {
  TOP = "top",
  RIGHT = "right",
}
</script>

<template>
  <div class="flex-start flex w-full gap-4" :class="wrapperClasses">
    <div
      v-if="labelConverted && labelConverted.length !== 0"
      class="flex flex-row items-center"
      :class="{
        'order-1': labelPosition === LabelPosition.RIGHT,
        'sr-only': visuallyHideLabel,
      }"
      data-testid="label-wrapper"
    >
      <label
        class="grid items-center"
        :class="[
          { 'pl-4': labelPosition === LabelPosition.RIGHT },
          labelClass ? labelClass : 'ris-label2-regular',
        ]"
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
        :has-error="!!localValidationError"
        :update-validation-error="updateValidationError"
      />
    </div>

    <div v-if="localValidationError" class="flex flex-row items-center">
      <IconErrorOutline class="pr-4 text-red-800" />

      <div
        class="ris-label3-regular mt-2 flex-row text-red-800"
        :data-testid="id + '-validationError'"
      >
        {{ errorMessage }}
      </div>
    </div>
  </div>
</template>
