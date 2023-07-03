<script lang="ts" setup>
import { computed, ref, watch } from "vue"
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

const inputValue = ref(props.modelValue)
const selectedInputType = ref<InputType>(InputType.DATE_TIME)
function detectSelectedInputType(): void {
  if (inputValue.value.YEAR && inputValue.value.YEAR.length > 0) {
    selectedInputType.value = InputType.YEAR
  } else selectedInputType.value = InputType.DATE_TIME
}

watch(
  () => props.modelValue,
  (newValue) => {
    if (newValue !== undefined) {
      if (inputValue.value) inputValue.value = newValue
    }
  },
  { immediate: true }
)

watch(inputValue, () => emit("update:modelValue", inputValue.value), {
  deep: true,
})

watch(inputValue, detectSelectedInputType, { immediate: true, deep: true })

const dateValue = computed({
  get: () => inputValue.value.DATE?.[0],
  set: (value) => {
    inputValue.value.DATE = value ? [value] : undefined
    inputValue.value.YEAR = undefined
  },
})

const timeValue = computed({
  get: () => inputValue.value.TIME?.[0] ?? "",
  set: (value) => {
    inputValue.value.TIME = value ? [value] : undefined
    inputValue.value.YEAR = undefined
  },
})

const yearValue = computed({
  get: () => inputValue.value.YEAR?.[0],
  set: (value) => {
    inputValue.value.YEAR = value ? [value] : undefined
    inputValue.value.DATE = undefined
    inputValue.value.TIME = undefined
  },
})
</script>

<template>
  <div>
    <div class="flex gap-96 mb-24">
      <label class="form-control">
        <input
          id="announcementDate"
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
          id="announcementYear"
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
      <label class="label-03-reg" for="announcementDateYearInput"
        >Jahresangabe</label
      >
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
