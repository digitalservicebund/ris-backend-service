<script lang="ts" setup>
import { ref, watch } from "vue"
import InputField from "@/shared/components/input/InputField.vue"
import TextInput from "@/shared/components/input/TextInput.vue"

type ParticipationInstitutions = { type: string; institution: string }
interface Props {
  modelValue: ParticipationInstitutions
}

interface Emits {
  (event: "update:modelValue", value: ParticipationInstitutions): void
}
const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const inputValue = ref<ParticipationInstitutions>(props.modelValue)

watch(
  () => props,
  () => (inputValue.value = props.modelValue),
  { immediate: true, deep: true }
)

watch(
  inputValue,
  () => {
    emit("update:modelValue", inputValue.value)
  },
  { deep: true }
)
</script>

<template>
  <div class="flex">
    <InputField
      id="participationType"
      aria-label="Art der Mitwirkung"
      class="w-1/2"
      label="Art der Mitwirkung"
    >
      <TextInput
        id="participationType"
        v-model="inputValue.type"
        aria-label="Art der Mitwirkung"
      />
    </InputField>

    <InputField
      id="participationInstitution"
      aria-label="Mitwirkendes Organ"
      class="w-1/2"
      label="Mitwirkendes Organ"
    >
      <TextInput
        id="participationInstitution"
        v-model="inputValue.institution"
        aria-label="Mitwirkendes Organ"
        class="[&:not(:hover,:focus)]:border-l-0"
      />
    </InputField>
  </div>
</template>
