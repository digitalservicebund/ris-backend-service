<script lang="ts" setup>
import { computed, ref, toValue, watch } from "vue"
import { ValidationError } from "@/shared/components/input/types"
import errors from "@/shared/i18n/errors.json"
import { isErrorCode } from "@/shared/i18n/utils"
import { useGlobalValidationErrorStore } from "@/stores/globalValidationErrorStore"
import IconErrorOutline from "~icons/ic/baseline-error-outline"

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
const errorsFromStore = getByInstance(props.id)
const storeValidationError = computed(() => errorsFromStore.value[0])

function updateValidationError(newValidationError?: ValidationError) {
  localValidationError.value = newValidationError
}

const localValidationError = ref<ValidationError | undefined>(
  props.validationError ?? toValue(storeValidationError),
)

const errorMessage = computed(() => {
  if (!localValidationError.value) return undefined

  const { code, message } = localValidationError.value
  if (code && isErrorCode(code)) return errors[code].title
  else return message
})

watch(
  () => props.validationError,
  (is, was) => {
    if (is) {
      localValidationError.value = is
    } else if (!is && was && storeValidationError.value) {
      localValidationError.value = storeValidationError.value
    } else {
      localValidationError.value = undefined
    }
  },
)

watch(storeValidationError, (is, was) => {
  if (is) {
    localValidationError.value = is
  } else if (!is && was && props.validationError) {
    localValidationError.value = props.validationError
  } else {
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
      v-if="labelConverted && labelConverted.length !== 0"
      class="flex flex-row items-center"
      :class="{
        'order-1': labelPosition === LabelPosition.RIGHT,
        'sr-only': visuallyHideLabel,
      }"
      data-testid="label-wrapper"
    >
      <IconErrorOutline
        v-if="localValidationError"
        class="pr-4 text-20 text-red-800"
      />

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
        :has-error="!!localValidationError"
        :update-validation-error="updateValidationError"
      />
    </div>

    <div
      v-if="localValidationError"
      class="ds-label-03-reg min-h-[1rem] text-red-800"
      :data-testid="id + '-validationError'"
    >
      {{ errorMessage }}
    </div>
  </div>
</template>
