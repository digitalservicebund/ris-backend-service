<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import DateInput from "@/components/DateInput.vue"
import TextInput from "@/components/TextInput.vue"
import { useInputModel } from "@/composables/useInputModel"

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

const { inputValue } = useInputModel<CitationDate, Props, Emits>(props, emit)

function onlyAllowNumbers(event: KeyboardEvent) {
  const isNumber = /^\d+$/.test(event.key)
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

const handlePaste = async (event: ClipboardEvent) => {
  const clipboardData = event.clipboardData
  if (clipboardData !== null) {
    const pastedText = clipboardData.getData("text/plain")
    if (/^\d+$/.test(pastedText.substring(0, 3))) {
      return
    } else {
      event.preventDefault()
    }
  }
}

watch(
  inputValue,
  () => {
    if (inputValue.value) {
      switch (selectedInputType.value) {
        case InputType.DATE:
          inputValue.value.year = undefined
          break
        case InputType.YEAR:
          // this if needed because of behaviour in DateInput component
          if (inputValue.value.date !== "") {
            inputValue.value.date = undefined
          }
          break
      }
    }
    selectedInputType.value = inputValue.value?.year
      ? InputType.YEAR
      : InputType.DATE
  },
  {
    immediate: true,
  }
)

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
  <div class="pb-32 w-240">
    <div class="radio-group">
      <label class="form-control">
        <input
          v-model="selectedInputType"
          aria-label="Citation Date"
          name="inputType"
          type="radio"
          :value="InputType.DATE"
        />
        Datum
      </label>
      <label class="form-control">
        <input
          v-model="selectedInputType"
          aria-label="Citation Year"
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
      alt-text="Citadation Date Input Field"
      aria-label=""
    />
    <div>
      <TextInput
        v-if="selectedInputType === InputType.YEAR"
        id="citationYear"
        v-model="yearValue"
        alt-text="Citation Year Input Field"
        aria-label=""
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
