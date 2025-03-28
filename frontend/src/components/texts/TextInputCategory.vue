<script lang="ts" setup>
import InputText from "primevue/inputtext"
import { nextTick } from "vue"
import CategoryWrapper from "@/components/CategoryWrapper.vue"

interface Props {
  id: string
  label: string
  shouldShowButton: boolean
  modelValue?: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: undefined,
})

defineEmits<{
  "update:modelValue": [value: string | undefined]
}>()

async function focusInput() {
  await nextTick()
  const inputElement = document.getElementById(props.id)
  inputElement?.focus()
}
</script>

<template>
  <CategoryWrapper
    :label="label"
    :should-show-button="shouldShowButton"
    @toggled="focusInput"
  >
    <div class="flex flex-col">
      <label class="ris-label2-regular mb-4" :for="id">{{ label }}</label>

      <InputText
        :id="id"
        :aria-label="label"
        fluid
        :model-value="modelValue"
        size="small"
        @update:model-value="$emit('update:modelValue', $event)"
      />
    </div>
  </CategoryWrapper>
</template>
