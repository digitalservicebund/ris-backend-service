<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { Metadata, ProofIndication } from "@/domain/Norm"
import ChipsInput from "@/shared/components/input/ChipsInput.vue"
import DateInput from "@/shared/components/input/DateInput.vue"
import DropdownInput from "@/shared/components/input/DropdownInput.vue"
import InputField from "@/shared/components/input/InputField.vue"
import TextInput from "@/shared/components/input/TextInput.vue"
import YearInput from "@/shared/components/input/YearInput.vue"

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

watch(inputValue, () => emit("update:modelValue", inputValue.value), {
  deep: true,
})

const PROOF_INDICATION_TRANSLATIONS: { [Value in ProofIndication]: string } = {
  [ProofIndication.NOT_YET_CONSIDERED]: "noch nicht berücksichtigt",
  [ProofIndication.CONSIDERED]: "ist berücksichtigt",
}

interface DropdownItem {
  label: string
  value: string
}

const dropdownItems: DropdownItem[] = Object.entries(
  PROOF_INDICATION_TRANSLATIONS
).map(([value, label]) => {
  return { label, value }
})

const workNote = computed({
  get: () => inputValue.value.WORK_NOTE,
  set: (data?: string[]) => data && (inputValue.value.WORK_NOTE = data),
})

const description = computed({
  get: () => inputValue.value.DESCRIPTION?.[0],
  set: (data?: string) =>
    (inputValue.value.DESCRIPTION = data ? [data] : undefined),
})

const date = computed({
  get: () => inputValue.value.DATE?.[0],
  set: (data?: string) => (inputValue.value.DATE = data ? [data] : undefined),
})

const year = computed({
  get: () => inputValue.value.YEAR?.[0],
  set: (data?: string) => (inputValue.value.YEAR = data ? [data] : undefined),
})

const reference = computed({
  get: () => inputValue.value.REFERENCE?.[0],
  set: (data?: string) =>
    (inputValue.value.REFERENCE = data ? [data] : undefined),
})

const entryIntoForceDateNote = computed({
  get: () => inputValue.value.ENTRY_INTO_FORCE_DATE_NOTE,
  set: (data?: string[]) =>
    data && (inputValue.value.ENTRY_INTO_FORCE_DATE_NOTE = data),
})

const proofIndication = computed({
  get: () => inputValue.value.PROOF_INDICATION?.[0],
  set: (data?: ProofIndication) =>
    data && (inputValue.value.PROOF_INDICATION = [data]),
})
</script>
<template>
  <div>
    <InputField
      id="workNote"
      aria-label="Bearbeitungshinweis"
      class="w-1/3"
      label="Bearbeitungshinweis"
    >
      <ChipsInput
        id="workNoteChips"
        v-model="workNote"
        aria-label="Bearbeitungshinweis Chips"
      />
    </InputField>
    <InputField
      id="description"
      aria-label="Bezeichnung der Änderungsvorschrift"
      class="w-1/3"
      label="Bezeichnung der Änderungsvorschrift"
    >
      <TextInput
        id="descriptionText"
        v-model="description"
        aria-label="Bezeichnung der Änderungsvorschrift Description"
      />
    </InputField>
    <div class="flex gap-24 items-center">
      <InputField id="date" aria-label="Datum" class="md:w-auto" label="Datum">
        <DateInput
          id="documentStatusDate"
          v-model="date"
          aria-label="Dokument Datum"
        />
      </InputField>
      <p>oder</p>
      <InputField id="year" aria-label="Jahr" class="md:w-auto" label="Jahr">
        <YearInput
          id="documentStatusYear"
          v-model="year"
          aria-label="Dokument Jahr"
        />
      </InputField>
    </div>
    <InputField
      id="reference"
      aria-label="Fundstelle der Änderungsvorschrift"
      class="md:w-auto"
      label="Fundstelle der Änderungsvorschrift"
    >
      <TextInput
        id="referenceText"
        v-model="reference"
        aria-label="Fundstelle der Änderungsvorschrift Text"
      />
    </InputField>
    <InputField
      id="entryIntoForceDateNote"
      aria-label="Datum des Inkrafttretens der Änderung"
      class="md:w-auto"
      label="Datum des Inkrafttretens der Änderung"
    >
      <ChipsInput
        id="entryIntoForceDateNoteChips"
        v-model="entryIntoForceDateNote"
        aria-label="Datum des Inkrafttretens der Änderung Chips"
      />
    </InputField>
    <InputField
      id="proofIndication"
      aria-label="Angaben zum textlichen und/oder dokumentarischen Nachweis"
      class="md:w-auto"
      label="Angaben zum textlichen und/oder dokumentarischen Nachweis"
    >
      <DropdownInput
        id="proofIndicationDropdown"
        v-model="proofIndication"
        aria-label="Angaben zum textlichen und/oder dokumentarischen Nachweis Dropdown"
        has-smaller-height
        :items="dropdownItems"
        placeholder="Bitte auswählen"
      />
    </InputField>
  </div>
</template>
