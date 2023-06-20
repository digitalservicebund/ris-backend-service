<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { Metadata, ProofType } from "@/domain/Norm"
import DropdownInput from "@/shared/components/input/DropdownInput.vue"
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

const PROOF_TYPE_TRANSLATIONS: { [Value in ProofType]: string } = {
  [ProofType.TEXT_PROOF_FROM]: "Textnachweis ab",
  [ProofType.TEXT_PROOF_VALIDITY_FROM]: "Textnachweis Geltung ab",
}

interface DropdownItem {
  label: string
  value: string
}

const dropdownItems: DropdownItem[] = Object.entries(
  PROOF_TYPE_TRANSLATIONS
).map(([value, label]) => {
  return { label, value }
})

watch(inputValue, () => emit("update:modelValue", inputValue.value), {
  deep: true,
})

const proofType = computed({
  get: () => inputValue.value.PROOF_TYPE?.[0],
  set: (data?: ProofType) => data && (inputValue.value.PROOF_TYPE = [data]),
})

const text = computed({
  get: () => inputValue.value.TEXT?.[0],
  set: (data?: string) => (inputValue.value.TEXT = data ? [data] : undefined),
})
</script>
<template>
  <InputField
    id="proofType"
    aria-label="Textnachweis"
    class="md:w-auto"
    label="Textnachweis"
  >
    <DropdownInput
      id="proofTypeDropdown"
      v-model="proofType"
      aria-label="Textnachweis Dropdown"
      has-smaller-height
      :items="dropdownItems"
      placeholder="Bitte auswählen"
    />
  </InputField>
  <InputField id="text" aria-label="Zusatz" class="md:w-auto" label="Zusatz">
    <TextInput id="textInput" v-model="text" aria-label="Zusatz Text" />
  </InputField>
</template>
