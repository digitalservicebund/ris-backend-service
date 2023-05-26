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

const jurisdiction = computed({
  get: () => inputValue.value.LEAD_JURISDICTION?.[0],
  set: (data?: string) =>
    (inputValue.value.LEAD_JURISDICTION = data ? [data] : undefined),
})

const unit = computed({
  get: () => inputValue.value.LEAD_UNIT?.[0],
  set: (data?: string) =>
    (inputValue.value.LEAD_UNIT = data ? [data] : undefined),
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
      id="leadJurisdiction"
      aria-label="Ressort"
      class="w-1/2"
      label="Ressort"
    >
      <TextInput
        id="leadJurisdiction"
        v-model="jurisdiction"
        aria-label="Ressort"
      />
    </InputField>

    <InputField
      id="leadUnit"
      aria-label="Organisationseinheit"
      class="w-1/2"
      label="Organisationseinheit"
    >
      <TextInput
        id="leadUnit"
        v-model="unit"
        aria-label="Organisationseinheit"
        class="[&:not(:hover,:focus)]:border-l-0"
      />
    </InputField>
  </div>
</template>
