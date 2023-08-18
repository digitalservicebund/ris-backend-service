<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { Metadata } from "@/domain/norm"
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
  set: (data?: string) => data && (inputValue.value.TEXT = [data]),
})
</script>
<template>
  <InputField
    id="otherText"
    aria-label="Zusatz"
    class="md:w-auto"
    label="Sonstiger Hinweis"
  >
    <TextInput
      id="otherText"
      v-model="text"
      aria-label="Sonstiger Hinweis Text"
    />
  </InputField>
</template>
