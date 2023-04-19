<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { Metadata } from "@/domain/Norm"
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

watch(inputValue, () => emit("update:modelValue", inputValue.value), {
  deep: true,
})

function createComputedProperty(key: string) {
  return computed({
    get: () => inputValue.value[key]?.[0],
    set: (data?: string) => data && (inputValue.value[key] = [data]),
  })
}

const digitalMedium = createComputedProperty("MEDIUM")
const digitalDate = createComputedProperty("DATE")
const digitalEdition = createComputedProperty("DIGITAL_EDITION")
const digitalYear = createComputedProperty("YEAR")
const digitalArea = createComputedProperty("AREA_OF_PUBLICATION")
const digitalAreaNumber = createComputedProperty(
  "NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA"
)
const digitalInfo = createComputedProperty("ADDITIONAL_INFO")
const digitalExplanations = createComputedProperty("EXPLANATION")
</script>
<template>
  <div>
    <div class="gap-16 grid grid-cols-2">
      <InputField
        id="digitalAnnouncementMedium"
        aria-label="Verkündungsmedium"
        label="Verkündungsmedium"
      >
        <TextInput
          id="citationYear"
          v-model="digitalMedium"
          alt-text="Verkündungsmedium"
          aria-label="Verkündungsmedium"
        />
      </InputField>
      <InputField
        id="digitalAnnouncementDate"
        aria-label="Verkündungsdatum"
        label="Verkündungsdatum"
      >
        <TextInput
          id="digitalAnnouncementDate"
          v-model="digitalDate"
          alt-text="Verkündungsdatum"
          aria-label="Verkündungsdatum"
        />
      </InputField>
      <InputField
        id="digitalAnnouncementEdition"
        aria-label="Ausgabenummer"
        label="Ausgabenummer"
      >
        <TextInput
          id="digitalAnnouncementEdition"
          v-model="digitalEdition"
          alt-text="Ausgabenummer"
          aria-label="Ausgabenummer"
        />
      </InputField>
      <InputField id="digitalAnnouncementYear" aria-label="Jahr" label="Jahr">
        <TextInput
          id="digitalAnnouncementYear"
          v-model="digitalYear"
          alt-text="Jahr"
          aria-label="Jahr"
        />
      </InputField>
      <InputField
        id="digitalAnnouncementArea"
        aria-label="Bereich der Veröffentlichung"
        label="Bereich der Veröffentlichung"
      >
        <TextInput
          id="digitalAnnouncementArea"
          v-model="digitalArea"
          alt-text="Bereich der Veröffentlichung"
          aria-label="Bereich der Veröffentlichung"
        />
      </InputField>
      <InputField
        id="digitalAnnouncementAreaNumber"
        aria-label="Nummer der Veröffentlichung im jeweiligen Bereich"
        label="Nummer der Veröffentlichung im jeweiligen Bereich"
      >
        <TextInput
          id="digitalAnnouncementAreaNumber"
          v-model="digitalAreaNumber"
          alt-text="Nummer der Veröffentlichung im jeweiligen Bereich"
          aria-label="Nummer der Veröffentlichung im jeweiligen Bereich"
        />
      </InputField>
    </div>
  </div>
  <div>
    <InputField
      id="digitalAnnouncementInfo"
      aria-label="Zusatzangaben"
      label="Zusatzangaben"
    >
      <textarea
        id="digitalAnnouncementInfo"
        v-model="digitalInfo"
        aria-label="Zusatzangaben"
        class="mt-2 outline outline-2 outline-blue-900 overflow-y-auto"
        rows="4"
      />
    </InputField>
    <InputField
      id="digitalAnnouncementExplanations"
      aria-label="Erläuterungen"
      label="Erläuterungen"
    >
      <textarea
        id="digitalAnnouncementExplanations"
        v-model="digitalExplanations"
        aria-label="Erläuterungen"
        class="mt-2 outline outline-2 outline-blue-900 overflow-y-auto"
        rows="4"
      />
    </InputField>
  </div>
</template>
