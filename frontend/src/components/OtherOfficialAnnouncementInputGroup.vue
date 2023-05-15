<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { Metadata } from "@/domain/Norm"
import InputField from "@/shared/components/input/InputField.vue"
import TextInput from "@/shared/components/input/TextInput.vue"

interface Props {
  modelValue: Metadata
}

interface Emits {
  (event: "update:modelValue", value: Metadata): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const inputValue = ref(props.modelValue)

watch(
  () => props.modelValue,
  (newValue) => {
    if (newValue !== undefined) {
      inputValue.value = newValue
    }
  },
  { immediate: true }
)

watch(inputValue, () => emit("update:modelValue", inputValue.value), {
  deep: true,
})

const otherOfficialReference = computed({
  get: () => inputValue.value.OTHER_OFFICIAL_REFERENCE?.[0],
  set: (data?: string) =>
    data && (inputValue.value.OTHER_OFFICIAL_REFERENCE = [data]),
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
