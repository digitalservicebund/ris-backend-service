<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { Metadata, OtherType } from "@/domain/Norm"
import DropdownInput from "@/shared/components/input/DropdownInput.vue"
import InputField from "@/shared/components/input/InputField.vue"

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

const OTHER_TYPE_TRANSLATIONS: { [Value in OtherType]: string } = {
  [OtherType.TEXT_IN_PROGRESS]: "Text in Bearbeitung",
  [OtherType.TEXT_PROOFED_BUT_NOT_DONE]:
    "Nachgewiesener Text dokumentarisch noch nicht abschließend bearbeitet",
}

interface DropdownItem {
  label: string
  value: string
}

const dropdownItems: DropdownItem[] = Object.entries(
  OTHER_TYPE_TRANSLATIONS
).map(([value, label]) => {
  return { label, value }
})

watch(inputValue, () => emit("update:modelValue", inputValue.value), {
  deep: true,
})

const otherType = computed({
  get: () => inputValue.value.OTHER_TYPE?.[0],
  set: (data?: OtherType) => data && (inputValue.value.OTHER_TYPE = [data]),
})
</script>
<template>
  <InputField
    id="otherType"
    aria-label="Zusatz"
    class="md:w-auto"
    label="Sonstiger Hinweis"
  >
    <DropdownInput
      id="otherTypeDropdown"
      v-model="otherType"
      aria-label="Sonstiger Hinweis Dropdown"
      has-smaller-height
      :items="dropdownItems"
      placeholder="Bitte auswählen"
    />
  </InputField>
</template>
