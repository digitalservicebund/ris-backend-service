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
    class="ds-input"
    :class="conditionalClasses"
    placeholder="HH:MM"
    type="time"
  />
</template>
