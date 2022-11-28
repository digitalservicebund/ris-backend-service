<script lang="ts" setup>
import { computed } from "vue"
import { useInputModel } from "@/composables/useInputModel"
import { InputField, ValidationError } from "@/domain"

interface Props {
  id: string
  value?: string
  modelValue?: string
  ariaLabel: string
  placeholder?: string
  validationError?: ValidationError
  readOnly?: boolean
  subField?: InputField
}

interface Emits {
  (event: "update:modelValue", value: string | undefined): void
  (event: "input", value: Event): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const { inputValue, emitInputEvent } = useInputModel<string, Props, Emits>(
  props,
  emit
)

const conditionalClasses = computed(() => ({
  input__error: props.validationError,
  input__readonly: props.readOnly,
}))
</script>

<template>
  <input
    :id="id"
    v-model.lazy="inputValue"
    :aria-label="ariaLabel"
    class="bg-white input"
    :class="conditionalClasses"
    :placeholder="placeholder"
    :readonly="$props.readOnly"
    type="text"
    @input="emitInputEvent"
  />
</template>

<style lang="scss" scoped>
.input {
  display: flex;
  width: 100%;
  height: 3.75rem;
  flex-wrap: wrap;
  align-content: space-between;
  padding: 12px 16px;
  @apply border-2 border-solid border-blue-800;

  &:focus {
    outline: none;
  }

  &:autofill {
    @apply shadow-white text-inherit;
  }

  &:autofill:focus {
    @apply shadow-white text-inherit;
  }

  &__error {
    @apply border-red-800 bg-red-200;

    &:autofill {
      @apply shadow-error text-inherit;
    }

    &:autofill:focus {
      @apply shadow-error text-inherit;
    }
  }

  &__readonly {
    @apply border-none bg-white;
  }
}

.expand-enter-from {
  max-height: 0;
}

.expand-enter-to {
  max-height: 1000px;
}

.expand-enter-active {
  overflow: hidden;
  transition: all 0.5s ease-in-out;
}

.expand-leave-from {
  max-height: 1000px;
}

.expand-leave-to {
  max-height: 0;
}

.expand-leave-active {
  overflow: hidden;
  transition: all 0.5s ease-in-out;
}

.expandable-content {
  width: 100%;

  &__header {
    display: flex;
    width: 100%;
    align-items: center;
    justify-content: space-between;
  }

  .icon {
    cursor: pointer;
  }
}
</style>
