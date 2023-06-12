<script lang="ts" setup>
import { computed, ref } from "vue"
import { ValidationError } from "@/shared/components/input/types"
import { useInputModel } from "@/shared/composables/useInputModel"

interface Props {
  id: string
  value?: string
  modelValue?: string
  ariaLabel: string
  placeholder?: string
  validationError?: ValidationError
  readOnly?: boolean
  fullHeight?: boolean
}

interface Emits {
  (event: "update:modelValue", value: string | undefined): void
  (event: "input", value: Event): void
  (event: "enter-released"): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const inputRef = ref<HTMLInputElement | null>()

const { inputValue, emitInputEvent } = useInputModel<string, Props, Emits>(
  props,
  emit
)

const conditionalClasses = computed(() => ({
  input__error: props.validationError,
  input__fullheight: props.fullHeight,
}))

const tabindex = computed(() => (props.readOnly ? -1 : 0))

function focusInput() {
  inputRef.value?.focus()
}

defineExpose({ focusInput })
</script>

<template>
  <input
    :id="id"
    ref="inputRef"
    v-model="inputValue"
    :aria-label="ariaLabel"
    class="-outline-offset-4 autofill:focus:shadow-white autofill:focus:text-inherit autofill:shadow-white autofill:text-inherit bg-white block border-2 border-blue-800 focus:outline h-[3.75rem] hover:outline input outline-2 outline-blue-800 px-16 py-12 read-only:border-none read-only:hover:outline-0 readonly:focus:outline-none w-full"
    :class="conditionalClasses"
    :placeholder="placeholder"
    :readonly="readOnly"
    :tabindex="tabindex"
    type="text"
    @input="emitInputEvent"
    @keyup.enter="emit('enter-released')"
  />
</template>

<style lang="scss" scoped>
.input {
  &__error {
    @apply border-red-800 outline-red-800 bg-red-200;

    &:autofill {
      @apply shadow-error text-inherit;
    }

    &:autofill:focus {
      @apply shadow-error text-inherit;
    }
  }

  &__fullheight {
    @apply h-full;
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
