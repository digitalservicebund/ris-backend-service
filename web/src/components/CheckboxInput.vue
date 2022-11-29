<script lang="ts" setup>
import { computed } from "vue"
import { useInputModel } from "@/composables/useInputModel"
import { ValidationError } from "@/domain"

interface Props {
  id: string
  value?: boolean | undefined
  modelValue?: boolean | undefined
  ariaLabel: string
  validationError?: ValidationError
}

interface Emits {
  (event: "update:modelValue", value: boolean | undefined): void
  (event: "input", value: Event): void
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  modelValue: undefined,
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
    class="appearance-none border-2 border-blue-800 disabled:border-gray-600 disabled:hover:border-2 focus:border-4 h-40 hover:border-4 outline-none w-40"
    :class="{ 'border-red-800': isInvalid }"
    type="checkbox"
    @input="emitInputEvent"
  />
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
