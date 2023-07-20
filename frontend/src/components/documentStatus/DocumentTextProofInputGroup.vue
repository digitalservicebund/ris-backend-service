<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { Metadata } from "@/domain/Norm"
import InputField from "@/shared/components/input/InputField.vue"
import TextInput from "@/shared/components/input/TextInput.vue"

interface Props {
  modelValue: Metadata
}

type Emits = (event: "update:modelValue", value: Metadata) => void

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
  { immediate: true },
)

watch(inputValue, () => emit("update:modelValue", inputValue.value), {
  deep: true,
})

const text = computed({
  get: () => inputValue.value.TEXT?.[0],
  set: (data?: string) => (inputValue.value.TEXT = data ? [data] : undefined),
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
