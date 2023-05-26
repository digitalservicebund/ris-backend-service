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

const fna = computed({
  get: () => inputValue.value.SUBJECT_FNA?.[0],
  set: (data?: string) =>
    (inputValue.value.SUBJECT_FNA = data ? [data] : undefined),
})

const previousFna = computed({
  get: () => inputValue.value.SUBJECT_PREVIOUS_FNA?.[0],
  set: (data?: string) =>
    (inputValue.value.SUBJECT_PREVIOUS_FNA = data ? [data] : undefined),
})

const gesta = computed({
  get: () => inputValue.value.SUBJECT_GESTA?.[0],
  set: (data?: string) =>
    (inputValue.value.SUBJECT_GESTA = data ? [data] : undefined),
})

const bgb3 = computed({
  get: () => inputValue.value.SUBJECT_BGB_3?.[0],
  set: (data?: string) =>
    (inputValue.value.SUBJECT_BGB_3 = data ? [data] : undefined),
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
  <div class="flex flex-col">
    <div class="flex">
      <InputField
        id="subjectFna"
        aria-label="FNA-Nummer"
        class="w-1/2"
        label="FNA-Nummer"
      >
        <TextInput id="subjectFna" v-model="fna" aria-label="FNA-Nummer" />
      </InputField>
      <InputField
        id="subjectPreviousFna"
        aria-label="Frühere FNA-Nummer"
        class="w-1/2"
        label="Frühere FNA-Nummer"
      >
        <TextInput
          id="subjectPreviousFna"
          v-model="previousFna"
          aria-label="Frühere FNA-Nummer"
          class="[&:not(:hover,:focus)]:border-l-0"
        />
      </InputField>
    </div>
    <div class="flex mt-24">
      <InputField
        id="subjectGesta"
        aria-label="GESTA-Nummer"
        class="w-1/2"
        label="GESTA-Nummer"
      >
        <TextInput
          id="subjectGesta"
          v-model="gesta"
          aria-label="GESTA-Nummer"
        />
      </InputField>
      <InputField
        id="subjectBgb3"
        aria-label="Bundesgesetzblatt Teil III"
        class="w-1/2"
        label="Bundesgesetzblatt Teil III"
      >
        <TextInput
          id="subjectBgb3"
          v-model="bgb3"
          aria-label="Bundesgesetzblatt Teil III"
          class="[&:not(:hover,:focus)]:border-l-0"
        />
      </InputField>
    </div>
  </div>
</template>
