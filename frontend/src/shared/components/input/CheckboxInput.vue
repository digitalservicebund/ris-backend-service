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
}

interface Emits {
  (event: "update:modelValue", value: boolean | undefined): void
  (event: "input", value: Event): void
}

const props = withDefaults(defineProps<Props>(), {
  value: false,
  modelValue: false,
  validationError: undefined,
})
const emit = defineEmits<Emits>()

const { inputValue, emitInputEvent } = useInputModel<boolean, Props, Emits>(
  props,
  emit
)

const isInvalid = computed(() => props.validationError !== undefined)
</script>
<template>
  <input
    :id="id"
    v-model="inputValue"
    :aria-label="ariaLabel"
    class="ds-checkbox"
    :class="{ 'has-error': isInvalid }"
    type="checkbox"
    @input="emitInputEvent"
  />
  <label></label>
</template>

<style lang="scss" scoped>
input {
  background-position: center;
  background-repeat: no-repeat;
  background-size: 60%;

  &:checked {
    background-image: url("@/assets/icons/ckeckbox_regular.svg");

    &:disabled {
      background-image: url("@/assets/icons/ckeckbox_disabled.svg");
    }
  }
}
</style>
