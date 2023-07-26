<script lang="ts" setup>
import dayjs from "dayjs"
import customParseFormat from "dayjs/plugin/customParseFormat"
import { vMaska, MaskaDetail } from "maska"
import { computed, ref, watch, watchEffect } from "vue"
import TextInput from "@/shared/components/input/TextInput.vue"
import { ValidationError } from "@/shared/components/input/types"

interface Props {
  id: string
  modelValue: string | undefined
  hasError?: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  "update:modelValue": [value: string | undefined]
  "update:validationError": [value?: ValidationError]
}>()

/* -------------------------------------------------- *
 * Model handling                                     *
 * -------------------------------------------------- */

// We'll need to keep an internal state for the input value since there are
// some conditions that need to be checked before we can emit the value to the
// parent component.
const localModelValue = ref(props.modelValue)

watch(
  () => props.modelValue,
  () => {
    localModelValue.value = props.modelValue
  },
)

// Use this for changing the localModelValue. This will automatically clean up
// the new value, update dependent states, and communicate the state change to
// the parent.
function emitModelValue(value: string | undefined): void {
  if (value === localModelValue.value) return

  const nextValue = onlyAllowNumbers(value)

  localModelValue.value = nextValue
  userHasFinished.value = false

  if (validateYear(nextValue)) emit("update:modelValue", nextValue)
  else if (!nextValue) emit("update:modelValue", undefined)
}

function onlyAllowNumbers(input: string | undefined): string | undefined {
  return input ? input.replace(/\D/g, "") : input
}

/* -------------------------------------------------- *
 * Validation                                         *
 * -------------------------------------------------- */

dayjs.extend(customParseFormat)

// This will be true if maska emits that the mask is completed
const inputCompleted = ref<boolean>(false)

function checkInputCompleted(event: CustomEvent<MaskaDetail>) {
  inputCompleted.value = event.detail.completed
}

// This will be true if we think that the user is no longer working on the
// input, e.g. on blur. We'll set it to false if the local value changes, i.e.
// the user is typing. We'll try not to annoy users with validation errors
// if they are still changing the value.
const userHasFinished = ref<boolean>(false)

const shouldShowValidationState = computed(() =>
  Boolean(
    localModelValue.value && (inputCompleted.value || userHasFinished.value),
  ),
)

const isValid = computed(() => validateYear(localModelValue.value))

watchEffect(() => {
  if (!isValid.value && shouldShowValidationState.value) {
    emit("update:validationError", {
      defaultMessage: "Kein valides Jahr",
      field: props.id,
    })
  } else {
    emit("update:validationError", undefined)
  }
})

// Valid years = 1000 - 9999
function validateYear(input: string | undefined): boolean {
  if (!input || input.length < 4) return false

  const date = dayjs(input, "YYYY", true)
  return date.isValid() && date.year() >= 1000 && date.year() <= 9999
}
</script>

<template>
  <TextInput
    :id="id"
    v-maska
    :aria-label="($attrs.ariaLabel as string) ?? ''"
    data-maska="####"
    :has-error="hasError || (shouldShowValidationState && !isValid)"
    maxlength="4"
    :model-value="localModelValue"
    placeholder="JJJJ"
    type="text"
    @blur="userHasFinished = true"
    @maska="checkInputCompleted"
    @update:model-value="emitModelValue"
  />
</template>
