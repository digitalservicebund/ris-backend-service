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

enum InputType {
  PRINT = "printAnnouncement",
  DIGITAL = "digitalAnnouncement",
  EU = "euAnnouncement",
  OTHER = "otherAnnouncement",
}

const inputValue = ref<Metadata>(props.modelValue)
const selectedInputType = ref<InputType | undefined>(undefined)

watch(props, () => (inputValue.value = props.modelValue), {
  immediate: true,
  deep: true,
})

watch(inputValue, () => emit("update:modelValue", inputValue.value), {
  deep: true,
})

watch(inputValue, () => {
  if (!selectedInputType.value) {
    selectedInputType.value = InputType.PRINT
  }
}, {
  deep: true,
  immediate: true
})

function createComputedProperty(key: string) {
  return computed({
    get: () => inputValue.value[key]?.[0],
    set: (data?: string) => data && (inputValue.value[key] = [data]),
  });
}

const printGazette = createComputedProperty("PRINT_GAZETTE");
const printYear = createComputedProperty("PRINT_YEAR");
const printNumber = createComputedProperty("PRINT_NUMBER");
const printPage = createComputedProperty("PRINT_PAGE");
const printInfo = createComputedProperty("PRINT_INFO");
const printExplanations = createComputedProperty("PRINT_EXPLANATIONS");
const digitalMedium = createComputedProperty("DIGITAL_MEDIUM");
const digitalDate = createComputedProperty("DIGITAL_DATE");
const digitalEdition = createComputedProperty("DIGITAL_EDITION");
const digitalYear = createComputedProperty("DIGITAL_YEAR");
const digitalArea = createComputedProperty("DIGITAL_AREA");
const digitalAreaNumber = createComputedProperty("DIGITAL_AREA_NUMBER");
const digitalInfo = createComputedProperty("DIGITAL_INFO");
const digitalExplanations = createComputedProperty("DIGITAL_EXPLANATIONS");
const euYear = createComputedProperty("EU_YEAR");
const euSeries = createComputedProperty("EU_SERIES");
const euNumber = createComputedProperty("EU_NUMBER");
const euPage = createComputedProperty("EU_PAGE");
const euInfo = createComputedProperty("EU_INFO");
const euExplanations = createComputedProperty("EU_EXPLANATIONS");
const otherAnnouncement = createComputedProperty("OTHER_ANNOUNCEMENT");




</script>

<template>
  <div class="pb-32">
    <div class="flex gap-176 flex-wrap">
      <div class="flex flex-col gap-24 radio-group">
        <label class="form-control">
          <input
            v-model="selectedInputType"
            aria-label=""
            name="OfficialAnnouncement"
            type="radio"
            :value="InputType.PRINT"
          />
          Papierverkündungsblatt
        </label>
        <label class="form-control">
          <input
            v-model="selectedInputType"
            aria-label=""
            name="OfficialAnnouncement"
            type="radio"
            :value="InputType.EU"
          />
          Amtsblatt der EU
        </label>
      </div>
      <div class="flex flex-col gap-24 radio-group">
        <label class="flex form-control items-start">
          <input
            v-model="selectedInputType"
            aria-label=""
            name="OfficialAnnouncement"
            type="radio"
            :value="InputType.DIGITAL"
          />
          Elektronisches Verkündungsblatt
        </label>
        <label class="form-control">
          <input
            v-model="selectedInputType"
            aria-label=""
            name="OfficialAnnouncement"
            type="radio"
            :value="InputType.OTHER"
          />
          Sonstige amtliche Fundstelle
        </label>
      </div>
    </div>
    <div v-if="selectedInputType === InputType.PRINT">
      <div class="flex justify-between ful-w gap-16">
        <InputField
          id="printAnnouncementGazette"
          aria-label="Verkündungsblatt"
          class="w-1/3"
          label="Verkündungsblatt"
        >
          <TextInput
            id="printAnnouncementGazette"
            alt-text="Verkündungsblatt"
            aria-label="Verkündungsblatt"
            v-model="printGazette"
          />
        </InputField>
        <InputField
          id="printAnnouncementYear"
          aria-label="Jahr"
          class="md:w-auto"
          label="Jahr"
        >
          <TextInput
            id="printAnnouncementYear"
            alt-text="Jahr"
            aria-label="Jahr"
            v-model="printYear"
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
            alt-text="Nummer"
            aria-label="Nummer"
            v-model="printNumber"
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
            alt-text="Seitenzahl"
            aria-label="Seitenzahl"
            v-model="printPage"
          />
        </InputField>
      </div>
      <div>
        <InputField
          id="printAnnouncementInfo"
          aria-label="Zusatzangaben"
          label="Zusatzangaben"
        >
          <textarea
              id="printAnnouncementInfo"
              aria-label="Zusatzangaben"
              class="outline outline-2 outline-blue-900 overflow-y-auto mt-4"
              rows="4"
              v-model="printInfo"
          />
        </InputField>

        <InputField
          id="printAnnouncementExplanations"
          aria-label="Erläuterungen"
          label="Erläuterungen"
        >
         <textarea
             id="printAnnouncementExplanations"
             aria-label="Erläuterungen"
             class="outline outline-2 outline-blue-900 overflow-y-auto mt-4"
             rows="4"
             v-model="printExplanations"
         />
        </InputField>
      </div>
    </div>
    <div v-if="selectedInputType === InputType.DIGITAL">
      <div>
        <div class="grid grid-cols-2 gap-16">
        <InputField
          id="digitalAnnouncementMedium"
          aria-label="Verkündungsmedium"

          label="Verkündungsmedium"
        >
          <TextInput
            id="citationYear"
            alt-text="Verkündungsmedium"
            aria-label="Verkündungsmedium"
            v-model="digitalMedium"
          />
        </InputField>
        <InputField
          id="digitalAnnouncementDate"
          aria-label="Verkündungsdatum"

          label="Verkündungsdatum"
        >
          <TextInput
            id="digitalAnnouncementDate"
            alt-text="Verkündungsdatum"
            aria-label="Verkündungsdatum"
            v-model="digitalDate"
          />
        </InputField>
        <InputField
          id="digitalAnnouncementEdition"
          aria-label="Ausgabenummer"

          label="Ausgabenummer"
        >
          <TextInput
            id="digitalAnnouncementEdition"
            alt-text="Ausgabenummer"
            aria-label="Ausgabenummer"
            v-model="digitalEdition"
          />
        </InputField>
        <InputField
          id="digitalAnnouncementYear"
          aria-label="Jahr"

          label="Jahr"
        >
          <TextInput
            id="digitalAnnouncementYear"
            alt-text="Jahr"
            aria-label="Jahr"
            v-model="digitalYear"
          />
        </InputField>
        <InputField
          id="digitalAnnouncementArea"
          aria-label="Bereich der Veröffentlichung"

          label="Bereich der Veröffentlichung"
        >
          <TextInput
            id="digitalAnnouncementArea"
            alt-text="Bereich der Veröffentlichung"
            aria-label="Bereich der Veröffentlichung"
            v-model="digitalArea"
          />
        </InputField>
        <InputField
          id="digitalAnnouncementAreaNumber"
          aria-label="Nummer der Veröffentlichung im jeweiligen Bereich"

          label="Nummer der Veröffentlichung im jeweiligen Bereich"
        >
          <TextInput
            id="digitalAnnouncementAreaNumber"
            alt-text="Nummer der Veröffentlichung im jeweiligen Bereich"
            aria-label="Nummer der Veröffentlichung im jeweiligen Bereich"
            v-model="digitalAreaNumber"
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
              aria-label="Zusatzangaben"
              class="outline outline-2 outline-blue-900 overflow-y-auto mt-2"
              rows="4"
              v-model="digitalInfo"
          />
        </InputField>
        <InputField
          id="digitalAnnouncementExplanations"
          aria-label="Erläuterungen"
          label="Erläuterungen"
        >
          <textarea
              id="digitalAnnouncementExplanations"
              aria-label="Erläuterungen"
              class="outline outline-2 outline-blue-900 overflow-y-auto mt-2"
              rows="4"
              v-model="digitalExplanations"
          />
        </InputField>
      </div>
    </div>
    <div v-if="selectedInputType === InputType.EU">
      <div class="w-full">
      <InputField
        id="euAnnouncementGazette"
        aria-label="Amtsblatt der EU"
        label="Amtsblatt der EU"
      >
        <TextInput
          id="euAnnouncementGazette"
          alt-text="Amtsblatt der EU"
          aria-label="Amtsblatt der EU"
        />
      </InputField>
      </div>
      <div class="flex justify-between gap-16">
      <InputField
          id="euAnnouncementYear"
          aria-label="Jahresangabe"
          class="w-full"
          label="Jahresangabe"
      >
        <TextInput
            id="euAnnouncementYear"
            alt-text="Jahresangabe"
            aria-label="Jahresangabe"
            v-model="euYear"
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
            alt-text="Reihe"
            aria-label="Reihe"
            v-model="euSeries"
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
            alt-text="Nummer des Amtsblatts"
            aria-label="Nummer des Amtsblatts"
            v-model="euNumber"
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
            alt-text="Seitenzahl"
            aria-label="Seitenzahl"
            v-model="euPage"
        />
      </InputField>
      </div>
      <InputField
          id="euAnnouncementInfo"
          aria-label="Zusatzangaben"
          label="Zusatzangaben"
      >
        <textarea
            id="euAnnouncementInfo"
            aria-label="Zusatzangaben"
            class="outline outline-2 outline-blue-900 overflow-y-auto mt-4"
            rows="4"
            v-model="euInfo"
        />
      </InputField>
      <InputField
          id="euAnnouncementExplanations"
          aria-label="Erläuterungen"
          label="Erläuterungen"
      >
        <textarea
            id="euAnnouncementExplanations"
            aria-label="Erläuterungen"
            class="outline outline-2 outline-blue-900 overflow-y-auto mt-4"
            rows="4"
            v-model="euExplanations"
        />
      </InputField>

    </div>
    <div v-if="selectedInputType === InputType.OTHER">
      <InputField
        id="otherOfficialAnnouncement"
        aria-label="Sonstige amtliche Fundstelle"
        label="Sonstige amtliche Fundstelle"
      >
        <textarea
          id="otherOfficialAnnouncement"
          aria-label="Sonstige amtliche Fundstelle"
          class="outline outline-2 outline-blue-900 overflow-y-auto mt-4"
          rows="4"
          v-model="otherAnnouncement"
        />
      </InputField>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.radio-group {
  display: flex;
  justify-content: space-between;
  margin-bottom: 24px;
}

.form-control {
  display: flex;
  flex-direction: row;
  align-items: center;
}

input[type="radio"] {
  display: grid;
  width: 1.5em;
  height: 1.5em;
  border: 0.15em solid currentcolor;
  border-radius: 50%;
  margin-right: 10px;
  appearance: none;
  background-color: white;
  color: #004b76;
  place-content: center;
}

input[type="radio"]::before {
  width: 0.75em;
  height: 0.75em;
  border-radius: 50%;
  background-color: #004b76;
  content: "";
  transform: scale(0);
}

input[type="radio"]:checked::before {
  transform: scale(1);
}
</style>
