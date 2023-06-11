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

const rangeStart = computed({
  get: () => inputValue.value.RANGE_START?.[0],
  set: (data?: string) =>
    (inputValue.value.RANGE_START = data ? [data] : undefined),
})

const rangeEnd = computed({
  get: () => inputValue.value.RANGE_END?.[0],
  set: (data?: string) =>
    (inputValue.value.RANGE_END = data ? [data] : undefined),
})

watch(props, () => (inputValue.value = props.modelValue), {
  immediate: true,
  deep: true,
})

watch(inputValue, () => emit("update:modelValue", inputValue.value), {
  deep: true,
})
</script>

<template>
  <div class="flex">
    <InputField
      id="ageIndicationStart"
      aria-label="Anfang"
      class="w-1/2"
      label="Anfang"
    >
      <TextInput
        id="ageIndicationStart"
        v-model="rangeStart"
        aria-label="Anfang"
      />
    </InputField>
    <InputField
      id="ageIndicationEnd"
      aria-label="Ende"
      class="w-1/2"
      label="Ende"
    >
      <TextInput id="ageIndicationEnd" v-model="rangeEnd" aria-label="Ende" />
    </InputField>
  </div>
</template>
