<script lang="ts" setup>
import { computed } from "vue"
import { Metadata } from "@/domain/Norm"
import DateInput from "@/shared/components/input/DateInput.vue"
import TimeInput from "@/shared/components/input/TimeInput.vue"
import YearInput from "@/shared/components/input/YearInput.vue"

interface Props {
  modelValue: Metadata
}

interface Emits {
  (event: "update:modelValue", value: Metadata): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

enum InputType {
  DATE_TIME = "date_time",
  YEAR = "year",
}

const selectedInputType = computed({
  get: () => (props.modelValue.YEAR ? InputType.YEAR : InputType.DATE_TIME),
  set: (value) => {
    emit(
      "update:modelValue",
      value === InputType.DATE_TIME ? { DATE: [], TIME: [] } : { YEAR: [] }
    )
  },
})

const dateValue = computed({
  get: () => props.modelValue.DATE?.[0] ?? "",
  set: (value) => {
    const next: Metadata = {
      DATE: value ? [value] : undefined,
      TIME: props.modelValue.TIME,
    }
    emit("update:modelValue", next)
  },
})

const timeValue = computed({
  get: () => props.modelValue.TIME?.[0] ?? "",
  set: (value) => {
    const next: Metadata = {
      TIME: value ? [value] : undefined,
      DATE: props.modelValue.DATE,
    }
    emit("update:modelValue", next)
  },
})

const yearValue = computed({
  get: () => props.modelValue.YEAR?.[0] ?? "",
  set: (value) => {
    const next: Metadata = { YEAR: value ? [value] : [] }
    emit("update:modelValue", next)
  },
})
</script>

<template>
  <div>
    <div class="flex gap-96 mb-24">
      <label class="form-control">
        <input
          id="announcementDateSelection"
          v-model="selectedInputType"
          aria-label="Wählen Sie ein Datum"
          name="announcementDateSelection"
          type="radio"
          :value="InputType.DATE_TIME"
        />
        Datum
      </label>
      <label class="form-control">
        <input
          id="announcementYearSelection"
          v-model="selectedInputType"
          aria-label="Wählen Sie ein Jahr"
          name="announcementDateSelection"
          type="radio"
          :value="InputType.YEAR"
        />
        Jahresangabe
      </label>
    </div>
    <div v-if="selectedInputType === InputType.DATE_TIME" class="flex gap-24">
      <div class="w-288">
        <label class="label-03-reg" for="announcementDateInput">Datum</label>
        <DateInput
          id="announcementDateInput"
          v-model="dateValue"
          aria-label="Datum"
          is-future-date
        />
      </div>
      <div class="w-288">
        <label class="label-03-reg" for="announcementDateTime">Uhrzeit</label>
        <TimeInput
          id="announcementDateTime"
          v-model="timeValue"
          aria-label="Uhrzeit"
        />
      </div>
    </div>
    <div v-if="selectedInputType === InputType.YEAR" class="w-112">
      <label class="label-03-reg" for="announcementDateYearInput">
        Jahresangabe
      </label>
      <YearInput
        id="announcementDateYearInput"
        v-model="yearValue"
        aria-label="Jahresangabe"
      />
    </div>
  </div>
</template>

<style lang="scss" scoped>
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

input[type="radio"]:hover,
input[type="radio"]:focus {
  border: 4px solid #004b76;
  outline: none;
}

input[type="radio"]::before {
  width: 0.9em;
  height: 0.9em;
  border-radius: 50%;
  background-color: #004b76;
  content: "";
  transform: scale(0);
}

input[type="radio"]:checked::before {
  transform: scale(1);
}
</style>
