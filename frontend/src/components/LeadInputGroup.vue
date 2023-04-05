<script lang="ts" setup>
import { ref, watch } from "vue"
import InputField from "@/shared/components/input/InputField.vue"
import TextInput from "@/shared/components/input/TextInput.vue"

type Lead = { jurisdiction: string; unit: string }

interface Props {
  modelValue: Lead
}

interface Emits {
  (event: "update:modelValue", value: Lead): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const inputValue = ref<Lead>(props.modelValue)

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
        v-model="inputValue.jurisdiction"
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
        v-model="inputValue.unit"
        aria-label="Organisationseinheit"
        class="[&:not(:hover,:focus)]:border-l-0"
      />
    </InputField>
  </div>
</template>
