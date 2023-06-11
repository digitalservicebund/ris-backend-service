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

const type = computed({
  get: () => inputValue.value.PARTICIPATION_TYPE?.[0],
  set: (data?: string) =>
    (inputValue.value.PARTICIPATION_TYPE = data ? [data] : undefined),
})

const institution = computed({
  get: () => inputValue.value.PARTICIPATION_INSTITUTION?.[0],
  set: (data?: string) =>
    (inputValue.value.PARTICIPATION_INSTITUTION = data ? [data] : undefined),
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
      id="participationType"
      aria-label="Art der Mitwirkung"
      class="w-1/2"
      label="Art der Mitwirkung"
    >
      <TextInput
        id="participationType"
        v-model="type"
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
        v-model="institution"
        aria-label="Mitwirkendes Organ"
        class="[&:not(:hover,:focus)]:border-l-0"
      />
    </InputField>
  </div>
</template>
