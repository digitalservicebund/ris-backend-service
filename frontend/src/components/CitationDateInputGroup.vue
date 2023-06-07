<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { Metadata } from "@/domain/Norm"
import DateInput from "@/shared/components/input/DateInput.vue"
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
  DATE = "date",
  YEAR = "year",
}

const inputValue = ref(props.modelValue)
const selectedInputType = ref<InputType>(InputType.DATE)
function detectSelectedInputType(): void {
  if (inputValue.value.YEAR && inputValue.value.YEAR.length > 0) {
    selectedInputType.value = InputType.YEAR
  } else selectedInputType.value = InputType.DATE
}

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

watch(inputValue, detectSelectedInputType, { immediate: true, deep: true })

const dateValue = computed({
  get: () => inputValue.value.DATE?.[0],
  set: (value) => {
    inputValue.value.DATE = value ? [value] : undefined
    inputValue.value.YEAR = undefined
  },
})

const yearValue = computed({
  get: () => inputValue.value.YEAR?.[0],
  set: (value) => {
    inputValue.value.YEAR = value ? [value] : undefined
    inputValue.value.DATE = undefined
  },
})
</script>

<template>
  <div class="w-320">
    <div class="flex justify-between mb-24">
      <label class="form-control">
        <input
          id="citationTypeDate"
          v-model="selectedInputType"
          aria-label="Wählen Sie Zitierdatum Datum"
          name="inputType"
          type="radio"
          :value="InputType.DATE"
        />
        Datum
      </label>
      <label class="form-control">
        <input
          id="citationTypeYear"
          v-model="selectedInputType"
          aria-label="Wählen Sie Zitierdatum Jahr"
          name="inputType"
          type="radio"
          :value="InputType.YEAR"
        />
        Jahresangabe
      </label>
    </div>
    <label
      class="flex gap-4 items-center label-03-reg mb-2 text-gray-900"
      :for="
        selectedInputType === InputType.DATE ? 'citationDate' : 'citationYear'
      "
      >Zitierdatum</label
    >
    <DateInput
      v-if="selectedInputType === InputType.DATE"
      id="citationDate"
      v-model="dateValue"
      alt-text="Zitierdatum Datum"
      aria-label="Zitierdatum Datum"
      is-future-date
    />
    <div>
      <YearInput
        v-if="selectedInputType === InputType.YEAR"
        id="citationYear"
        v-model="yearValue"
        aria-label="Zitierdatum Jahresangabe"
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
