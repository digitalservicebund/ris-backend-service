<template>
  <input
    :id="id"
    v-model="inputValue"
    class="input"
    :class="conditionalClasses"
    type="text"
    :placeholder="placeholder"
    :aria-label="ariaLabel"
    @input="emitInputEvent"
  />
</template>

<script lang="ts" setup>
import { computed } from "vue"
import { useInputModel } from "@/composables/useInputModel"

interface Props {
  id: string
  value?: string
  modelValue?: string
  ariaLabel: string
  placeholder?: string
  hasError?: boolean
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
  input__error: props.hasError,
}))
</script>

<style lang="scss" scoped>
.input {
  width: 100%;
  padding: 17px 24px;
  outline: 2px solid $text-tertiary;

  &:-webkit-autofill {
    -webkit-box-shadow: 0 0 0 50px white inset;
    box-shadow: 0 0 0 50px white inset;
    -webkit-text-fill-color: $black;
  }

  &:-webkit-autofill:focus {
    -webkit-box-shadow: 0 0 0 50px white inset;
    box-shadow: 0 0 0 50px white inset;
    -webkit-text-fill-color: $black;
  }

  &__error {
    outline: 2px solid $error;
    background-color: $error-background;
    padding: 17px 24px;
    width: 100%;

    &:-webkit-autofill {
      -webkit-box-shadow: 0 0 0 50px $error-background inset;
      box-shadow: 0 0 0 50px $error-background inset;
      -webkit-text-fill-color: $black;
    }

    &:-webkit-autofill:focus {
      -webkit-box-shadow: 0 0 0 50px $error-background inset;
      box-shadow: 0 0 0 50px $error-background inset;
      -webkit-text-fill-color: $black;
    }
  }
}
</style>
