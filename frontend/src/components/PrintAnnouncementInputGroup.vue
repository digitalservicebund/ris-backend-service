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

const dateValue = computed({
  get: () => inputValue.value?.date,
  set: (value) => (inputValue.value = { ...inputValue.value, date: value }),
})

const yearValue = computed({
  get: () => inputValue.value?.year,
  set: (value) => (inputValue.value = { ...inputValue.value, year: value }),
})
</script>

<template>
  <div class="pb-32">
    <div class="flex gap-24">
      <div class="flex flex-wrap radio-group">
        <label class="form-control pb-24 w-1/3">
          <input
            v-model="selectedInputType"
            aria-label=""
            name="OfficialAnnouncement"
            type="radio"
            :value="InputType.PRINT"
          />
          Papierverkündungsblatt
        </label>
        <label class="form-control pb-24 shrink-1 w-2/3">
          <input
            v-model="selectedInputType"
            aria-label=""
            name="OfficialAnnouncement"
            type="radio"
            :value="InputType.DIGITAL"
          />
          Elektronisches Verkündungsblatt
        </label>
        <label class="form-control pb-24 w-1/3">
          <input
            v-model="selectedInputType"
            aria-label=""
            name="OfficialAnnouncement"
            type="radio"
            :value="InputType.EU"
          />
          Amtsblatt der EU
        </label>
        <label class="form-control pb-24 shrink-1 w-2/3">
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
      <div class="flex gap-24">
        <InputField
          id="printAnnouncementGazette"
          aria-label="Verkündungsblatt"
          class="basis-full"
          label="Verkündungsblatt"
        >
          <TextInput
            id="printAnnouncementGazette"
            alt-text="Verkündungsblatt"
            aria-label="Verkündungsblatt"
            value=""
          />
        </InputField>
        <InputField id="printAnnouncementYear" aria-label="Jahr" label="Jahr">
          <TextInput
            id="printAnnouncementYear"
            alt-text="Jahr"
            aria-label="Jahr"
            value=""
          />
        </InputField>
        <InputField
          id="printAnnouncementNumber"
          aria-label="Nummer"
          label="Nummer"
        >
          <TextInput
            id="printAnnouncementNumber"
            alt-text="Nummer"
            aria-label="Nummer"
            value=""
          />
        </InputField>
        <InputField
          id="printAnnouncementPage"
          aria-label="Seitenzahl"
          label="Seitenzahl"
        >
          <TextInput
            id="printAnnouncementPage"
            alt-text="Seitenzahl"
            aria-label="Seitenzahl"
            value=""
          />
        </InputField>
      </div>
      <div>
        <InputField
          id="printAnnouncementInfo"
          aria-label="Zusatzangaben"
          class="flex-grow-1"
          label="Zusatzangaben"
        >
          <textarea
            id="printAnnouncementInfo"
            alt-text="Zusatzangaben"
            aria-label="Zusatzangaben"
            class="outline outline-2 outline-blue-900 overflow-y-auto p-10"
            rows="4"
            value=""
          />
        </InputField>

        <InputField
          id="printAnnouncementExplanations"
          aria-label="Erläuterungen"
          class="flex-grow-1"
          label="Erläuterungen"
        >
          <textarea
            id="printAnnouncementExplanations"
            alt-text="Erläuterungen"
            aria-label="Erläuterungen"
            class="outline outline-2 outline-blue-900 overflow-y-auto p-10"
            rows="4"
            value=""
          />
        </InputField>
      </div>
    </div>
    <div v-if="selectedInputType === InputType.DIGITAL">
      <div class="flex flex-wrap">
        <InputField
          id="digitalAnnouncementMedium"
          aria-label="Verkündungsmedium"
          class="basis-full"
          label="Verkündungsmedium"
        >
          <TextInput
            id="citationYear"
            alt-text="Verkündungsmedium"
            aria-label="Verkündungsmedium"
            value=""
          />
        </InputField>
        <InputField
          id="digitalAnnouncementDate"
          aria-label="Verkündungsdatum"
          class="pr-24 w-1/2"
          label="Verkündungsdatum"
        >
          <TextInput
            id="digitalAnnouncementDate"
            alt-text="Verkündungsdatum"
            aria-label="Verkündungsdatum"
            value=""
          />
        </InputField>
        <InputField
          id="digitalAnnouncementEdition"
          aria-label="Ausgabenummer"
          class="w-1/2"
          label="Ausgabenummer"
        >
          <TextInput
            id="digitalAnnouncementEdition"
            alt-text="Ausgabenummer"
            aria-label="Ausgabenummer"
            value=""
          />
        </InputField>
        <InputField
          id="digitalAnnouncementYear"
          aria-label="Jahr"
          class="pr-24 w-1/2"
          label="Jahr"
        >
          <TextInput
            id="digitalAnnouncementYear"
            alt-text="Jahr"
            aria-label="Jahr"
            value=""
          />
        </InputField>
        <InputField
          id="digitalAnnouncementPage"
          aria-label="Seitenzahlen"
          class="w-1/2"
          label="Seitenzahlen"
        >
          <TextInput
            id="digitalAnnouncementYear"
            alt-text="Seitenzahlen"
            aria-label="Seitenzahlen"
            value=""
          />
        </InputField>
        <InputField
          id="digitalAnnouncementArea"
          aria-label="Bereich der Veröffentlichung"
          class="pr-24 w-1/2"
          label="Bereich der Veröffentlichung"
        >
          <TextInput
            id="digitalAnnouncementArea"
            alt-text="Bereich der Veröffentlichung"
            aria-label="Bereich der Veröffentlichung"
            value=""
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
            alt-text="Nummer der Veröffentlichung im jeweiligen Bereich"
            aria-label="Nummer der Veröffentlichung im jeweiligen Bereich"
            value=""
          />
        </InputField>
      </div>
      <div>
        <InputField
          id="digitalAnnouncementInfo"
          aria-label="Zusatzangaben"
          class="basis-full"
          label="Zusatzangaben"
        >
          <textarea
            id="digitalAnnouncementInfo"
            alt-text="Nummer der Veröffentlichung im jeweiligen Bereich"
            aria-label="Nummer der Veröffentlichung im jeweiligen Bereich"
            class="outline outline-2 outline-blue-900 overflow-y-auto p-10"
            rows="4"
            value=""
          />
        </InputField>
        <InputField
          id="digitalAnnouncementExplanations"
          aria-label="Erläuterungen"
          class="basis-full"
          label="Erläuterungen"
        >
          <textarea
            id="digitalAnnouncementExplanations"
            alt-text="Erläuterungen"
            aria-label="Erläuterungen"
            class="outline outline-2 outline-blue-900 overflow-y-auto p-10"
            rows="4"
            value=""
          />
        </InputField>
      </div>
    </div>
    <div v-if="selectedInputType === InputType.EU">
      <div class="flex flex-wrap">
        <InputField
          id="euAnnouncementGazette"
          aria-label="Amtsblatt der EU"
          class="basis-full"
          label="Amtsblatt der EU"
        >
          <TextInput
            id="euAnnouncementGazette"
            v-model="yearValue"
            alt-text="Amtsblatt der EU"
            aria-label="Amtsblatt der EU"
            value=""
          />
        </InputField>

        <InputField
          id="euAnnouncementYear"
          aria-label="Jahr"
          class="pr-24 w-1/4"
          label="Jahr"
        >
          <TextInput
            id="euAnnouncementYear"
            alt-text="Jahr"
            aria-label="Jahr"
            value=""
          />
        </InputField>
        <InputField
          id="euAnnouncementSeries"
          aria-label="Reihe"
          class="pr-24 w-1/4"
          label="Reihe"
        >
          <TextInput
            id="euAnnouncementSeries"
            alt-text="Reihe"
            aria-label="Reihe"
            value=""
          />
        </InputField>
        <InputField
          id="euAnnouncementNumber"
          aria-label="Nummer des Amtsblatts"
          class="pr-24 w-1/4"
          label="Nummer des Amtsblatts"
        >
          <TextInput
            id="euAnnouncementNumber"
            alt-text="Nummer des Amtsblatts"
            aria-label="Nummer des Amtsblatts"
            value=""
          />
        </InputField>
        <InputField
          id="euAnnouncementPage"
          aria-label="Seitenzahl"
          class="w-1/4"
          label="Seitenzahl"
        >
          <TextInput
            id="euAnnouncementPage"
            alt-text="Seitenzahl"
            aria-label="Seitenzahl"
            value=""
          />
        </InputField>
      </div>
      <div>
        <InputField
          id="digitalAnnouncementInfo"
          aria-label="Zusatzangaben"
          class="basis-full"
          label="Zusatzangaben"
        >
          <textarea
            id="digitalAnnouncementInfo"
            alt-text="Nummer der Veröffentlichung im jeweiligen Bereich"
            aria-label="Nummer der Veröffentlichung im jeweiligen Bereich"
            class="outline outline-2 outline-blue-900 overflow-y-auto p-10"
            rows="4"
            value=""
          />
        </InputField>
        <InputField
          id="digitalAnnouncementExplanations"
          aria-label="Erläuterungen"
          class="basis-full"
          label="Erläuterungen"
        >
          <textarea
            id="digitalAnnouncementExplanations"
            alt-text="Erläuterungen"
            aria-label="Erläuterungen"
            class="outline outline-2 outline-blue-900 overflow-y-auto p-10"
            rows="4"
            value=""
          />
        </InputField>
      </div>
    </div>
    <div v-if="selectedInputType === InputType.OTHER">
      <InputField
        id="otherOfficialAnnouncement"
        aria-label="Sonstige amtliche Fundstelle"
        class="basis-full"
        label="Sonstige amtliche Fundstelle"
      >
        <textarea
          id="otherOfficialAnnouncement"
          alt-text="Sonstige amtliche Fundstelle"
          aria-label="Sonstige amtliche Fundstelle"
          class="outline outline-2 outline-blue-900 overflow-y-auto p-10"
          rows="4"
          value=""
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
