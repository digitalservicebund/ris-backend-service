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

const announcementGazette = computed({
  get: () => props.modelValue.ANNOUNCEMENT_GAZETTE?.[0],
  set: (data?: string) => {
    const next = produce(props.modelValue, (draft) => {
      draft.ANNOUNCEMENT_GAZETTE = data ? [data] : undefined
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
  <div class="ful-w flex justify-between gap-16">
    <InputField
      id="printAnnouncementGazette"
      aria-label="Verkündungsblatt"
      class="w-1/3"
      label="Verkündungsblatt"
    >
      <TextInput
        id="printAnnouncementGazette"
        v-model="announcementGazette"
        alt-text="Verkündungsblatt"
        aria-label="Verkündungsblatt"
      />
    </InputField>
    <InputField
      id="printAnnouncementYear"
      v-slot="{ id, hasError, updateValidationError }"
      aria-label="Jahr"
      class="md:w-auto"
      label="Jahr"
    >
      <YearInput
        :id="id"
        v-model="year"
        alt-text="Jahr"
        aria-label="Jahr"
        :has-error="hasError"
        @update:validation-error="updateValidationError"
      />
    </InputField>
    <InputField
      id="printAnnouncementNumber"
      aria-label="Nummer"
      class="md:w-auto"
      label="Nummer"
    >
      <TextInput
        id="printAnnouncementNumber"
        v-model="number"
        alt-text="Nummer"
        aria-label="Nummer"
      />
    </InputField>
    <InputField
      id="printAnnouncementPage"
      aria-label="Seitenzahl"
      class="md:w-auto"
      label="Seitenzahl"
    >
      <TextInput
        id="printAnnouncementPage"
        v-model="pageNumber"
        alt-text="Seitenzahl"
        aria-label="Seitenzahl"
      />
    </InputField>
  </div>
  <InputField
    id="printAnnouncementInfo"
    aria-label="Zusatzangaben"
    label="Zusatzangaben"
  >
    <ChipsInput
      id="printAnnouncementInfo"
      v-model="additionalInfo"
      aria-label="Zusatzangaben"
    />
  </InputField>

  <InputField
    id="printAnnouncementExplanations"
    aria-label="Erläuterungen"
    label="Erläuterungen"
  >
    <ChipsInput
      id="printAnnouncementExplanations"
      v-model="explanation"
      aria-label="Erläuterungen"
    />
  </InputField>
</template>
