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

const otherOfficialReference = computed({
  get: () => props.modelValue.OTHER_OFFICIAL_REFERENCE?.[0],
  set: (data?: string) => {
    const next = produce(props.modelValue, (draft) => {
      draft.OTHER_OFFICIAL_REFERENCE = data ? [data] : undefined
    })
    emit("update:modelValue", next)
  },
})
</script>

<template>
  <InputField
    id="otherOfficialAnnouncement"
    aria-label="Sonstige amtliche Fundstelle"
    label="Sonstige amtliche Fundstelle"
  >
    <TextInput
      id="otherOfficialAnnouncement"
      v-model="otherOfficialReference"
      aria-label="Sonstige amtliche Fundstelle"
    />
  </InputField>
</template>
