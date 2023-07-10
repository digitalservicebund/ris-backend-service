<script lang="ts" setup>
import { computed } from "vue"
import { ValidationError } from "@/shared/components/input/types"
import { useInputModel } from "@/shared/composables/useInputModel"

interface Props {
  id: string
  value?: boolean
  modelValue?: boolean
  ariaLabel: string
  validationError?: ValidationError
  size?: "small" | "regular"
  disabled?: boolean
}

interface Emits {
  (event: "update:modelValue", value: boolean | undefined): void
  (event: "input", value: Event): void
}

const props = withDefaults(defineProps<Props>(), {
  value: false,
  modelValue: false,
  validationError: undefined,
  size: "regular",
})
const emit = defineEmits<Emits>()

const { inputValue, emitInputEvent } = useInputModel<boolean, Props, Emits>(
  props,
  emit
)

const isInvalid = computed(() => props.validationError !== undefined)
</script>

<template>
  <!-- A note about the wrapper and block class in the label: Currently, Angie
  positions checkboxes absolutely, which causes them to end up at the top of
  the page, which in turn causes the page to jump when clicking them. We therefore
  need to wrap them in a relatively positioned element to keep them within the
  bounds of the component. This is a workaround and should be removed once the
  issue is fixed in Angie. -->
  <div class="relative">
    <input
      :id="id"
      v-model="inputValue"
      :aria-label="ariaLabel"
      class="ds-checkbox"
      :class="{ 'has-error': isInvalid, 'ds-checkbox-small': size === 'small' }"
      :disabled="disabled"
      type="checkbox"
      @input="emitInputEvent"
    />
    <label class="!block" :for="id"></label>
  </div>
</template>
