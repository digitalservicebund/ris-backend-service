<script lang="ts" setup>
import { produce } from "immer"
import { computed } from "vue"
import { Metadata } from "@/domain/norm"
import InputField from "@/shared/components/input/InputField.vue"
import TextInput from "@/shared/components/input/TextInput.vue"

const props = defineProps<{
  modelValue: Metadata
}>()

const emit = defineEmits<{
  "update:modelValue": [value: Metadata]
}>()

const text = computed({
  get: () => props.modelValue.TEXT?.[0],
  set: (data?: string) => {
    const next = produce(props.modelValue, (draft) => {
      draft.TEXT = data ? [data] : undefined
    })
    emit("update:modelValue", next)
  },
})
</script>
<template>
  <InputField
    id="proofText"
    aria-label="Textnachweis"
    class="md:w-auto"
    label="Textnachweis"
  >
    <TextInput id="proofText" v-model="text" aria-label="Textnachweis Text" />
  </InputField>
</template>
