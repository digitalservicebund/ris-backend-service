<script lang="ts" setup>
import { produce } from "immer"
import { computed } from "vue"
import { Metadata } from "@/domain/norm"
import ChipsInput from "@/shared/components/input/ChipsInput.vue"
import DateInput from "@/shared/components/input/DateInput.vue"
import InputField from "@/shared/components/input/InputField.vue"
import TextInput from "@/shared/components/input/TextInput.vue"
import YearInput from "@/shared/components/input/YearInput.vue"

const props = defineProps<{
  modelValue: Metadata
}>()

const emit = defineEmits<{
  "update:modelValue": [value: Metadata]
}>()

const workNote = computed({
  get: () => props.modelValue.WORK_NOTE?.[0],
  set: (data?: string) => {
    const next = produce(props.modelValue, (draft) => {
      draft.WORK_NOTE = data ? [data] : undefined
    })
    emit("update:modelValue", next)
  },
})

const description = computed({
  get: () => props.modelValue.DESCRIPTION?.[0],
  set: (data?: string) => {
    const next = produce(props.modelValue, (draft) => {
      draft.DESCRIPTION = data ? [data] : undefined
    })
    emit("update:modelValue", next)
  },
})

const date = computed({
  get: () => props.modelValue.DATE?.[0],
  set: (data?: string) => {
    const next = produce(props.modelValue, (draft) => {
      draft.DATE = data ? [data] : undefined
    })
    emit("update:modelValue", next)
  },
})

const year = computed({
  get: () => props.modelValue.YEAR?.[0],
  set: (data?: string) => {
    const next = produce(props.modelValue, (draft) => {
      draft.YEAR = data ? [data] : undefined
    })
    emit("update:modelValue", next)
  },
})

const reference = computed({
  get: () => props.modelValue.REFERENCE?.[0],
  set: (data?: string) => {
    const next = produce(props.modelValue, (draft) => {
      draft.REFERENCE = data ? [data] : undefined
    })
    emit("update:modelValue", next)
  },
})

const entryIntoForceDateNote = computed({
  get: () => props.modelValue.ENTRY_INTO_FORCE_DATE_NOTE,
  set: (data?: string[]) => {
    const next = produce(props.modelValue, (draft) => {
      draft.ENTRY_INTO_FORCE_DATE_NOTE = data
    })
    emit("update:modelValue", next)
  },
})

const proofIndication = computed({
  get: () => props.modelValue.PROOF_INDICATION?.[0],
  set: (data?: string) => {
    const next = produce(props.modelValue, (draft) => {
      draft.PROOF_INDICATION = data ? [data] : undefined
    })
    emit("update:modelValue", next)
  },
})

const dateEnabled = computed(() => !props.modelValue.YEAR?.[0])

const yearEnabled = computed(() => !props.modelValue.DATE?.[0])
</script>
<template>
  <div>
    <InputField
      id="workNoteText"
      v-slot="{ id }"
      class="w-1/3"
      label="Bearbeitungshinweis"
    >
      <TextInput :id="id" v-model="workNote" aria-label="Bearbeitungshinweis" />
    </InputField>
    <InputField
      id="descriptionText"
      v-slot="{ id }"
      class="w-1/3"
      label="Bezeichnung der Änderungsvorschrift"
    >
      <TextInput
        :id="id"
        v-model="description"
        aria-label="Bezeichnung der Änderungsvorschrift"
      />
    </InputField>
    <div class="flex gap-24">
      <InputField
        id="documentStatusDate"
        v-slot="{ id, hasError, updateValidationError }"
        class="md:w-auto"
        label="Datum der Änderungsvorschrift"
      >
        <DateInput
          :id="id"
          v-model="date"
          aria-label="Datum der Änderungsvorschrift"
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
        class="md:w-auto"
        label="Jahr"
      >
        <YearInput
          :id="id"
          v-model="year"
          aria-label="Jahr"
          :disabled="!yearEnabled"
          :has-error="hasError"
          @update:validation-error="updateValidationError"
        />
      </InputField>
    </div>
    <InputField
      id="referenceText"
      v-slot="{ id }"
      class="md:w-auto"
      label="Fundstelle der Änderungsvorschrift"
    >
      <TextInput
        :id="id"
        v-model="reference"
        aria-label="Fundstelle der Änderungsvorschrift"
      />
    </InputField>
    <InputField
      id="entryIntoForceDateNoteChips"
      v-slot="{ id }"
      class="md:w-auto"
      label="Datum des Inkrafttretens der Änderung"
    >
      <ChipsInput :id="id" v-model="entryIntoForceDateNote" />
    </InputField>
    <InputField
      id="proofIndicationText"
      v-slot="{ id }"
      class="md:w-auto"
      label="Angaben zum textlichen und/oder dokumentarischen Nachweis"
    >
      <TextInput
        :id="id"
        v-model="proofIndication"
        aria-label="Angaben zum textlichen und/oder dokumentarischen Nachweis"
      />
    </InputField>
  </div>
</template>
