<script lang="ts" setup>
import { ref, watch } from "vue"
import InputField from "@/shared/components/input/InputField.vue"
import TextInput from "@/shared/components/input/TextInput.vue"

type subjectArea = {
  fna: "string"
  previousFna: "string"
  gesta: "string"
  bgb3: "string"
}
interface Props {
  modelValue: subjectArea
}

interface Emits {
  (event: "update:modelValue", value: subjectArea): void
}
const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const inputValue = ref<subjectArea>(props.modelValue)

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
  <div class="flex flex-col">
    <div class="flex">
      <InputField
        id="subjectFna"
        aria-label="FNA-Nummer"
        class="w-1/2"
        label="FNA-Nummer"
      >
        <TextInput
          id="subjectFna"
          v-model="inputValue.fna"
          aria-label="FNA-Nummer"
        />
      </InputField>
      <InputField
        id="subjectPreviousFna"
        aria-label="Frühere FNA-Nummer"
        class="w-1/2"
        label="Frühere FNA-Nummer"
      >
        <TextInput
          id="subjectPreviousFna"
          v-model="inputValue.previousFna"
          aria-label="Frühere FNA-Nummer"
          class="[&:not(:hover,:focus)]:border-l-0"
        />
      </InputField>
    </div>
    <div class="flex">
      <InputField
        id="subjectGesta"
        aria-label="GESTA-Nummer"
        class="w-1/2"
        label="GESTA-Nummer"
      >
        <TextInput
          id="subjectGesta"
          v-model="inputValue.gesta"
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
          v-model="inputValue.bgb3"
          aria-label="Bundesgesetzblatt Teil III"
          class="[&:not(:hover,:focus)]:border-l-0"
        />
      </InputField>
    </div>
  </div>
</template>
