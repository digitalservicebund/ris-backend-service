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

const props = defineProps<Props>()

const emit = defineEmits<{
  "update:modelValue": [value: Metadata]
}>()

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

const announcementMedium = computed({
  get: () => inputValue.value.ANNOUNCEMENT_MEDIUM?.[0],
  set: (data) =>
    (inputValue.value.ANNOUNCEMENT_MEDIUM = data ? [data] : undefined),
})

const date = computed({
  get: () => inputValue.value.DATE?.[0],
  set: (data) => (inputValue.value.DATE = data ? [data] : undefined),
})

const edition = computed({
  get: () => inputValue.value.EDITION?.[0],
  set: (data) => (inputValue.value.EDITION = data ? [data] : undefined),
})

const year = computed({
  get: () => inputValue.value.YEAR?.[0],
  set: (data) => (inputValue.value.YEAR = data ? [data] : undefined),
})

const pageNumber = computed({
  get: () => inputValue.value.PAGE?.[0],
  set: (data) => (inputValue.value.PAGE = data ? [data] : undefined),
})

const areaOfPublication = computed({
  get: () => inputValue.value.AREA_OF_PUBLICATION?.[0],
  set: (data) =>
    (inputValue.value.AREA_OF_PUBLICATION = data ? [data] : undefined),
})

const numberOfThePublicationInTheRespectiveArea = computed({
  get: () =>
    inputValue.value.NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA?.[0],
  set: (data) =>
    (inputValue.value.NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA = data
      ? [data]
      : undefined),
})

const additionalInfo = computed({
  get: () => inputValue.value.ADDITIONAL_INFO,
  set: (data) => (inputValue.value.ADDITIONAL_INFO = data),
})

const explanation = computed({
  get: () => inputValue.value.EXPLANATION,
  set: (data) => (inputValue.value.EXPLANATION = data),
})
</script>
<template>
  <div class="ful-w flex gap-16">
    <InputField
      id="digitalAnnouncementMedium"
      v-slot="{ id }"
      aria-label="Verkündungsmedium"
      class="w-1/2"
      label="Verkündungsmedium"
    >
      <TextInput
        :id="id"
        v-model="announcementMedium"
        alt-text="Verkündungsmedium"
        aria-label="Verkündungsmedium"
      />
    </InputField>
    <InputField
      id="digitalAnnouncementDate"
      v-slot="{ id, hasError, updateValidationError }"
      aria-label="Verkündungsdatum"
      class="w-1/2"
      label="Verkündungsdatum"
    >
      <DateInput
        :id="id"
        v-model="date"
        aria-label="Verkündungsdatum"
        :has-error="hasError"
        is-future-date
        @update:validation-error="updateValidationError"
      />
    </InputField>
  </div>
  <div class="ful-w flex gap-16">
    <InputField
      id="digitalAnnouncementYear"
      v-slot="{ id, hasError, updateValidationError }"
      aria-label="Jahr"
      class="w-1/3"
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
      id="digitalAnnouncementEdition"
      v-slot="{ id }"
      aria-label="Ausgabenummer"
      class="w-1/3"
      label="Ausgabenummer"
    >
      <TextInput
        :id="id"
        v-model="edition"
        alt-text="Ausgabenummer"
        aria-label="Ausgabenummer"
      />
    </InputField>
    <InputField
      id="digitalAnnouncementPageNumber"
      v-slot="{ id }"
      aria-label="Seitenzahl"
      class="w-1/3"
      label="Seitenzahl"
    >
      <TextInput
        :id="id"
        v-model="pageNumber"
        alt-text="Seitenzahl"
        aria-label="Seitenzahl"
      />
    </InputField>
  </div>
  <div class="ful-w flex gap-16">
    <InputField
      id="digitalAnnouncementArea"
      v-slot="{ id }"
      aria-label="Bereich der Veröffentlichung"
      class="w-1/2"
      label="Bereich der Veröffentlichung"
    >
      <TextInput
        :id="id"
        v-model="areaOfPublication"
        alt-text="Bereich der Veröffentlichung"
        aria-label="Bereich der Veröffentlichung"
      />
    </InputField>
    <InputField
      id="digitalAnnouncementAreaNumber"
      v-slot="{ id }"
      aria-label="Nummer der Veröffentlichung im jeweiligen Bereich"
      class="w-1/2"
      label="Nummer der Veröffentlichung im jeweiligen Bereich"
    >
      <TextInput
        :id="id"
        v-model="numberOfThePublicationInTheRespectiveArea"
        alt-text="Nummer der Veröffentlichung im jeweiligen Bereich"
        aria-label="Nummer der Veröffentlichung im jeweiligen Bereich"
      />
    </InputField>
  </div>
  <InputField
    id="digitalAnnouncementInfo"
    v-slot="{ id }"
    aria-label="Zusatzangaben"
    label="Zusatzangaben"
  >
    <ChipsInput :id="id" v-model="additionalInfo" aria-label="Zusatzangaben" />
  </InputField>
  <InputField
    id="digitalAnnouncementExplanations"
    v-slot="{ id }"
    aria-label="Erläuterungen"
    label="Erläuterungen"
  >
    <ChipsInput :id="id" v-model="explanation" aria-label="Erläuterungen" />
  </InputField>
</template>
