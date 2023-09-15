<script lang="ts" setup>
import { produce } from "immer"
import { computed } from "vue"
import { Metadata } from "@/domain/norm"
import ChipsInput from "@/shared/components/input/ChipsInput.vue"
import InputField from "@/shared/components/input/InputField.vue"
import TextInput from "@/shared/components/input/TextInput.vue"
import YearInput from "@/shared/components/input/YearInput.vue"

const props = defineProps<{
  modelValue: Metadata
}>()

const emit = defineEmits<{
  "update:modelValue": [value: Metadata]
}>()

const defaultValueEuGovernmentGazette = "Amtsblatt der EU"

const year = computed({
  get: () => props.modelValue.YEAR?.[0],
  set: (data?: string) => {
    const next = produce(props.modelValue, (draft) => {
      draft.YEAR = data ? [data] : undefined
    })
    emit("update:modelValue", next)
  },
})

const series = computed({
  get: () => props.modelValue.SERIES?.[0],
  set: (data?: string) => {
    const next = produce(props.modelValue, (draft) => {
      draft.SERIES = data ? [data] : undefined
    })
    emit("update:modelValue", next)
  },
})

const number = computed({
  get: () => props.modelValue.NUMBER?.[0],
  set: (data?: string) => {
    const next = produce(props.modelValue, (draft) => {
      draft.NUMBER = data ? [data] : undefined
    })
    emit("update:modelValue", next)
  },
})

const pageNumber = computed({
  get: () => props.modelValue.PAGE?.[0],
  set: (data?: string) => {
    const next = produce(props.modelValue, (draft) => {
      draft.PAGE = data ? [data] : undefined
    })
    emit("update:modelValue", next)
  },
})

const additionalInfo = computed({
  get: () => props.modelValue.ADDITIONAL_INFO,
  set: (data?: string[]) => {
    const next = produce(props.modelValue, (draft) => {
      draft.ADDITIONAL_INFO = data
    })
    emit("update:modelValue", next)
  },
})

const explanation = computed({
  get: () => props.modelValue.EXPLANATION,
  set: (data?: string[]) => {
    const next = produce(props.modelValue, (draft) => {
      draft.EXPLANATION = data
    })
    emit("update:modelValue", next)
  },
})
</script>

<template>
  <InputField
    id="euAnnouncementGazette"
    v-slot="{ id }"
    aria-label="Amtsblatt der EU"
    label="Amtsblatt der EU"
  >
    <TextInput
      :id="id"
      alt-text="Amtsblatt der EU"
      aria-label="Amtsblatt der EU"
      class="border-solid border-gray-800"
      :model-value="defaultValueEuGovernmentGazette"
      read-only
    />
  </InputField>
  <div class="flex justify-between gap-16">
    <InputField
      id="euAnnouncementYear"
      v-slot="{ id, hasError, updateValidationError }"
      aria-label="Jahresangabe"
      class="w-full"
      label="Jahresangabe"
    >
      <YearInput
        :id="id"
        v-model="year"
        alt-text="Jahresangabe"
        aria-label="Jahresangabe"
        :has-error="hasError"
        @update:validation-error="updateValidationError"
      />
    </InputField>
    <InputField
      id="euAnnouncementSeries"
      aria-label="Reihe"
      class="w-full"
      label="Reihe"
    >
      <TextInput
        id="euAnnouncementSeries"
        v-model="series"
        alt-text="Reihe"
        aria-label="Reihe"
      />
    </InputField>
    <InputField
      id="euAnnouncementNumber"
      aria-label="Nummer des Amtsblatts"
      class="w-full"
      label="Nummer des Amtsblatts"
    >
      <TextInput
        id="euAnnouncementNumber"
        v-model="number"
        alt-text="Nummer des Amtsblatts"
        aria-label="Nummer des Amtsblatts"
      />
    </InputField>
    <InputField
      id="euAnnouncementPage"
      aria-label="Seitenzahl"
      class="w-full"
      label="Seitenzahl"
    >
      <TextInput
        id="euAnnouncementPage"
        v-model="pageNumber"
        alt-text="Seitenzahl"
        aria-label="Seitenzahl"
      />
    </InputField>
  </div>
  <InputField
    id="euAnnouncementInfo"
    aria-label="Zusatzangaben"
    label="Zusatzangaben"
  >
    <ChipsInput
      id="euAnnouncementInfo"
      v-model="additionalInfo"
      aria-label="Zusatzangaben"
    />
  </InputField>
  <InputField
    id="euAnnouncementExplanations"
    aria-label="Erläuterungen"
    label="Erläuterungen"
  >
    <ChipsInput
      id="euAnnouncementExplanations"
      v-model="explanation"
      aria-label="Erläuterungen"
    />
  </InputField>
</template>
