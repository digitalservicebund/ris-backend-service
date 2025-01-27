<script lang="ts" setup>
import TextInput from "@/components/input/TextInput.vue"

defineProps<{
  id: string
  modelValue?: string
  disabled?: boolean
}>()

const emit = defineEmits<{
  "update:modelValue": [value?: string]
}>()
</script>

<template>
  <TextInput
    :id="id"
    :aria-label="($attrs.ariaLabel as string) ?? ''"
    class="ds-input"
    data-test-id="time-input"
    :disabled="disabled"
    :model-value="modelValue"
    placeholder="HH:MM"
    type="time"
    @update:model-value="emit('update:modelValue', $event)"
  />
</template>
