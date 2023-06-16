<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { Metadata } from "@/domain/Norm"
import DateInput from "@/shared/components/input/DateInput.vue"
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

const announcementMedium = computed({
  get: () => inputValue.value.ANNOUNCEMENT_MEDIUM?.[0],
  set: (data?: string) =>
    (inputValue.value.ANNOUNCEMENT_MEDIUM = data ? [data] : undefined),
})

const date = computed({
  get: () => inputValue.value.DATE?.[0],
  set: (data?: string) => (inputValue.value.DATE = data ? [data] : undefined),
})

const edition = computed({
  get: () => inputValue.value.EDITION?.[0],
  set: (data?: string) =>
    (inputValue.value.EDITION = data ? [data] : undefined),
})

const year = computed({
  get: () => inputValue.value.YEAR?.[0],
  set: (data?: string) => (inputValue.value.YEAR = data ? [data] : undefined),
})

const pageNumber = computed({
  get: () => inputValue.value.PAGE?.[0],
  set: (data?: string) => (inputValue.value.PAGE = data ? [data] : undefined),
})

const areaOfPublication = computed({
  get: () => inputValue.value.AREA_OF_PUBLICATION?.[0],
  set: (data?: string) =>
    (inputValue.value.AREA_OF_PUBLICATION = data ? [data] : undefined),
})

const numberOfThePublicationInTheRespectiveArea = computed({
  get: () =>
    inputValue.value.NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA?.[0],
  set: (data?: string) =>
    (inputValue.value.NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA = data
      ? [data]
      : undefined),
})

const additionalInfo = computed({
  get: () => inputValue.value.ADDITIONAL_INFO?.[0],
  set: (data?: string) =>
    (inputValue.value.ADDITIONAL_INFO = data ? [data] : undefined),
})

const explanation = computed({
  get: () => inputValue.value.EXPLANATION?.[0],
  set: (data?: string) =>
    (inputValue.value.EXPLANATION = data ? [data] : undefined),
})
</script>
<template>
  <div class="flex ful-w gap-16">
    <InputField
      id="digitalAnnouncementMedium"
      aria-label="Verkündungsmedium"
      class="w-1/2"
      label="Verkündungsmedium"
    >
      <TextInput
        id="digitalAnnouncementMedium"
        v-model="announcementMedium"
        alt-text="Verkündungsmedium"
        aria-label="Verkündungsmedium"
      />
    </InputField>
    <InputField
      id="digitalAnnouncementDate"
      aria-label="Verkündungsdatum"
      class="w-1/2"
      label="Verkündungsdatum"
    >
      <DateInput
        id="digitalAnnouncementDate"
        v-model="date"
        alt-text="Verkündungsdatum"
        aria-label="Verkündungsdatum"
        is-future-date
      />
    </InputField>
  </div>
  <div class="flex ful-w gap-16">
    <InputField
      id="digitalAnnouncementEdition"
      aria-label="Ausgabenummer"
      class="w-1/3"
      label="Ausgabenummer"
    >
      <TextInput
        id="digitalAnnouncementEdition"
        v-model="edition"
        alt-text="Ausgabenummer"
        aria-label="Ausgabenummer"
      />
    </InputField>
    <InputField
      id="digitalAnnouncementYear"
      aria-label="Jahr"
      class="w-1/3"
      label="Jahr"
    >
      <YearInput
        id="digitalAnnouncementYear"
        v-model="year"
        alt-text="Jahr"
        aria-label="Jahr"
      />
    </InputField>
    <InputField
      id="digitalAnnouncementPageNumber"
      aria-label="Seitenzahl"
      class="w-1/3"
      label="Seitenzahl"
    >
      <TextInput
        id="digitalAnnouncementPageNumber"
        v-model="pageNumber"
        alt-text="Seitenzahl"
        aria-label="Seitenzahl"
      />
    </InputField>
  </div>
  <div class="flex ful-w gap-16">
    <InputField
      id="digitalAnnouncementArea"
      aria-label="Bereich der Veröffentlichung"
      class="w-1/2"
      label="Bereich der Veröffentlichung"
    >
      <TextInput
        id="digitalAnnouncementArea"
        v-model="areaOfPublication"
        alt-text="Bereich der Veröffentlichung"
        aria-label="Bereich der Veröffentlichung"
      />
    </InputField>
    <InputField
      id="digitalAnnouncementAreaNumber"
      aria-label="Nummer der Veröffentlichung im jeweiligen Bereich"
      class="w-1/2"
      label="Nummer der Veröffentlichung im jeweiligen Bereich"
    >
      <TextInput
        id="digitalAnnouncementAreaNumber"
        v-model="numberOfThePublicationInTheRespectiveArea"
        alt-text="Nummer der Veröffentlichung im jeweiligen Bereich"
        aria-label="Nummer der Veröffentlichung im jeweiligen Bereich"
      />
    </InputField>
  </div>
  <InputField
    id="digitalAnnouncementInfo"
    aria-label="Zusatzangaben"
    label="Zusatzangaben"
  >
    <TextInput
      id="digitalAnnouncementInfo"
      v-model="additionalInfo"
      aria-label="Zusatzangaben"
    />
  </InputField>
  <InputField
    id="digitalAnnouncementExplanations"
    aria-label="Erläuterungen"
    label="Erläuterungen"
  >
    <TextInput
      id="digitalAnnouncementExplanations"
      v-model="explanation"
      aria-label="Erläuterungen"
    />
  </InputField>
</template>
