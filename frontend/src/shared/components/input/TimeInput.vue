<script lang="ts" setup>
import { computed } from "vue"
import { ValidationError } from "@/shared/components/input/types"

interface Props {
  id: string
  value?: string
  modelValue: string
  ariaLabel: string
  hasError?: boolean
}

interface Emits {
  (event: "update:modelValue", value?: string): void
  (event: "update:validationError", value?: ValidationError): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const inputValue = computed({
  get: () => props.modelValue,
  set: (value) => {
    emit("update:modelValue", value)
  },
})

const conditionalClasses = computed(() => ({
  "border-red-800 bg-red-200": props.hasError,
}))
</script>

<template>
  <input
    :id="id"
    v-model="inputValue"
    :aria-label="ariaLabel"
    class="-outline-offset-4 autofill:focus:shadow-white autofill:focus:text-inherit autofill:shadow-white autofill:text-inherit border-2 border-blue-800 flex flex-wrap focus:outline h-[3.75rem] hover:outline input outline-2 outline-blue-800 px-16 read-only:border-none read-only:hover:outline-0 readonly:focus:outline-none w-full"
    :class="conditionalClasses"
    placeholder="HH:MM"
    type="time"
  />
</template>
