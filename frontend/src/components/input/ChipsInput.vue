<script lang="ts" setup>
import { RisChipsInput } from "@digitalservicebund/ris-ui/components"
import { computed } from "vue"

interface Props {
  id: string
  modelValue?: string[]
  ariaLabel: string
  hasError?: boolean
  readOnly?: boolean
  testId?: string
}

const props = defineProps<Props>()

const emit = defineEmits<{
  "update:modelValue": [value?: string[]]
}>()

const localChips = computed<string[]>(() => props.modelValue ?? [])

const chips = computed<string[]>({
  get: () => {
    return props.modelValue ? props.modelValue : []
  },

  set: (newValue: string[]) => {
    if (!newValue || newValue.length === 0) {
      emit("update:modelValue", [])
      return
    }
    const lastChip = newValue.at(-1)
    if (
      localChips.value.length > newValue.length ||
      (lastChip && !localChips.value.includes(lastChip))
    ) {
      emit("update:modelValue", newValue)
    }
  },
})
</script>

<template>
  <RisChipsInput
    v-model="chips"
    :aria-label="ariaLabel"
    :data-testid="testId"
    :has-error="hasError"
    :input-id="id"
    :read-only="readOnly"
  />
</template>
