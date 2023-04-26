<script lang="ts" setup>
import { computed } from "vue"
import { ValidationError } from "@/shared/components/input/types"
import { useInputModel } from "@/shared/composables/useInputModel"

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

function updateValue() {
  if (inputValue.value === undefined) {
    inputValue.value = true
    console.log("I am here. Value: " + true)
  } else {
    inputValue.value = !inputValue.value
    console.log("I am here. Value: " + inputValue.value)
  }
}

const isInvalid = computed(() => props.validationError !== undefined)
</script>
<template>
  <input
    :id="id"
    v-model="inputValue"
    :aria-label="ariaLabel"
    class="appearance-none border-2 border-blue-800 disabled:border-gray-600 disabled:focus:outline-0 disabled:hover:outline-0 focus:outline-2 h-40 hover:outline-2 outline-0 outline-blue-800 outline-none outline-offset-[-4px] w-40"
    :class="{ 'border-red-800': isInvalid, 'outline-red-800': isInvalid }"
    type="checkbox"
    @input="emitInputEvent"
    @keydown.enter="updateValue"
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
