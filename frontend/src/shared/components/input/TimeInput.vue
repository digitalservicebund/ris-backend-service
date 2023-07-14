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

const props = defineProps<Props>()

const emit = defineEmits<{
  "update:modelValue": [value?: string]
  "update:validationError": [value?: ValidationError]
}>()

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
    class="input readonly:focus:outline-none flex h-[3.75rem] w-full flex-wrap border-2 border-blue-800 px-16 outline-2 -outline-offset-4 outline-blue-800 autofill:text-inherit autofill:shadow-white read-only:border-none hover:outline read-only:hover:outline-0 focus:outline autofill:focus:text-inherit autofill:focus:shadow-white"
    :class="conditionalClasses"
    placeholder="HH:MM"
    type="time"
  />
</template>
