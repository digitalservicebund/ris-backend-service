<script lang="ts" setup>
import {computed, ref, watch} from "vue"
import DateInput from "@/components/DateInput.vue"
import TextInput from "@/components/TextInput.vue"
import {useInputModel} from "@/composables/useInputModel"

type CitationDate = { date?: string; year?: string }

interface Props {
  value?: CitationDate
  modelValue?: CitationDate
  ariaLabel: string
}

interface Emits {
  (event: "update:modelValue", value?: CitationDate): void

  (event: "input", value: Event): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

enum InputType {
  DATE = "date",
  YEAR = "year",
}

const YearPlaceHolder = "JJJJ"

const selectedInputType = ref<InputType | undefined>(undefined)

const {inputValue} = useInputModel<CitationDate, Props, Emits>(props, emit)

function onlyAllowNumbers(event: KeyboardEvent) {
  const isNumber = /^[0-9]+$/.test(event.key)
  const isControlKey = [
    "Backspace",
    "Delete",
    "ArrowLeft",
    "ArrowRight",
  ].includes(event.key)
  if (!isNumber && !isControlKey) {
    event.preventDefault()
  }
}

const handlePaste = async (event: KeyboardEvent) => {
  event.preventDefault();
  const pastedText = await navigator.clipboard.readText();
  if (/^\d+$/.test(pastedText)) {
    return
  }
};

watch(
    inputValue,
    () => {
      selectedInputType.value = inputValue.value?.year
          ? InputType.YEAR
          : InputType.DATE
    },
    {
      immediate: true,
    }
)
watch(selectedInputType, () => {
  if (inputValue.value) {
    switch (selectedInputType.value) {
      case InputType.DATE:
        inputValue.value.year = undefined
        break
      case InputType.YEAR:
        inputValue.value.date = undefined
        break
    }
  }
})

const dateValue = computed({
  get: () => inputValue.value?.date,
  set: (value) => (inputValue.value = {...inputValue.value, date: value}),
})

const yearValue = computed({
  get: () => inputValue.value?.year,
  set: (value) => (inputValue.value = {...inputValue.value, year: value}),
})

const yearInput = ref<HTMLInputElement>()

function focusYearInput() {
  console.log("Test")
  // yearInput.value?.
}

</script>

<template>
  <div class="pb-32 w-240">
    <div class="radio-group">
      <label class="form-control">
        <input
            v-model="selectedInputType"
            name="inputType"
            type="radio"
            :value="InputType.DATE"
            aria-label="Citation Date"
        />
        Datum
      </label>
      <label class="form-control" @click="focusYearInput">
        <input
            v-model="selectedInputType"
            name="inputType"
            type="radio"
            :value="InputType.YEAR"
            aria-label="Citation Year"

        />
        Jahresangabe
      </label>
    </div>
    <label class="flex gap-4 items-center label-03-reg mb-2 text-gray-900"
    >Zitierdatum</label
    >
    <DateInput
        v-if="selectedInputType === InputType.DATE"
        id="citationDateInput"
        v-model="dateValue"
        aria-label=""
        alt-text="Citadation Date Input Field"
    />
    <div ref="yearInput">
      <TextInput
          v-if="selectedInputType === InputType.YEAR"
          id="citationYearInput"
          v-model="yearValue"
          aria-label=""
          alt-text="Citation Year Input Field"
          maxlength="4"
          :placeholder="YearPlaceHolder"
          @keypress="onlyAllowNumbers"
          @paste="handlePaste"

      />

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
