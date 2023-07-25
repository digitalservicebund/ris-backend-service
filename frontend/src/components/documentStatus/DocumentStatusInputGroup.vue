<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { Metadata } from "@/domain/Norm"
import ChipsInput from "@/shared/components/input/ChipsInput.vue"
import DateInput from "@/shared/components/input/DateInput.vue"
import InputField from "@/shared/components/input/InputField.vue"
import TextInput from "@/shared/components/input/TextInput.vue"
import YearInput from "@/shared/components/input/YearInput.vue"

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
  set: (data?: string) => data && (inputValue.value.PROOF_INDICATION = [data]),
})

const dateEnabled = computed(() => !inputValue.value.YEAR?.[0])
const yearEnabled = computed(() => !inputValue.value.DATE?.[0])
</script>
<template>
  <div>
    <InputField
      id="workNoteChips"
      v-slot="{ id }"
      aria-label="Bearbeitungshinweis"
      class="w-1/3"
      label="Bearbeitungshinweis"
    >
      <ChipsInput
        :id="id"
        v-model="workNote"
        aria-label="Bearbeitungshinweis Chips"
      />
    </InputField>
    <InputField
      id="descriptionText"
      v-slot="{ id }"
      aria-label="Bezeichnung der Änderungsvorschrift"
      class="w-1/3"
      label="Bezeichnung der Änderungsvorschrift"
    >
      <TextInput
        :id="id"
        v-model="description"
        aria-label="Bezeichnung der Änderungsvorschrift Description"
      />
    </InputField>
    <div class="flex gap-24">
      <InputField
        id="documentStatusDate"
        v-slot="{ id, hasError, updateValidationError }"
        aria-label="Datum"
        class="md:w-auto"
        label="Datum der Änderungsvorschrift"
      >
        <DateInput
          :id="id"
          v-model="date"
          aria-label="Dokument Datum"
          :disabled="!dateEnabled"
          :has-error="hasError"
          is-future-date
          @update:validation-error="updateValidationError"
        />
      </InputField>
      <p class="my-auto">oder</p>
      <InputField
        id="documentStatusYear"
        v-slot="{ id, hasError, updateValidationError }"
        aria-label="Jahr"
        class="md:w-auto"
        label="Jahr"
      >
        <YearInput
          :id="id"
          v-model="year"
          aria-label="Dokument Jahr"
          :disabled="!yearEnabled"
          :has-error="hasError"
          @update:validation-error="updateValidationError"
        />
      </InputField>
    </div>
    <InputField
      id="referenceText"
      v-slot="{ id }"
      aria-label="Fundstelle der Änderungsvorschrift"
      class="md:w-auto"
      label="Fundstelle der Änderungsvorschrift"
    >
      <TextInput
        :id="id"
        v-model="reference"
        aria-label="Fundstelle der Änderungsvorschrift Text"
      />
    </InputField>
    <InputField
      id="entryIntoForceDateNoteChips"
      v-slot="{ id }"
      aria-label="Datum des Inkrafttretens der Änderung"
      class="md:w-auto"
      label="Datum des Inkrafttretens der Änderung"
    >
      <ChipsInput
        :id="id"
        v-model="entryIntoForceDateNote"
        aria-label="Datum des Inkrafttretens der Änderung Chips"
      />
    </InputField>
    <InputField
      id="proofIndicationText"
      v-slot="{ id }"
      aria-label="Angaben zum textlichen und/oder dokumentarischen Nachweis"
      class="md:w-auto"
      label="Angaben zum textlichen und/oder dokumentarischen Nachweis"
    >
      <TextInput
        :id="id"
        v-model="proofIndication"
        aria-label="Angaben zum textlichen und/oder dokumentarischen Nachweis Text"
      />
    </InputField>
  </div>
</template>
