<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { Metadata, UndefinedDate } from "@/domain/Norm"
import DateInput from "@/shared/components/input/DateInput.vue"
import DropdownInput from "@/shared/components/input/DropdownInput.vue"
import InputField from "@/shared/components/input/InputField.vue"

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
  UNDEFINED_DATE = "undefined_date",
}

interface DropdownItem {
  label: string
  value: string
}

const inputValue = ref(props.modelValue)
const selectedInputType = ref<InputType>(InputType.DATE)

function detectSelectedInputType(): void {
  if (
    inputValue.value.UNDEFINED_DATE &&
    inputValue.value.UNDEFINED_DATE.length > 0
  ) {
    selectedInputType.value = InputType.UNDEFINED_DATE
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

const ENTRY_INTO_FORCE_DATE_TRANSLATIONS: { [Value in UndefinedDate]: string } =
  {
    [UndefinedDate.UNDEFINED_UNKNOWN]: "unbestimmt (unbekannt)",
    [UndefinedDate.UNDEFINED_FUTURE]: "unbestimmt (zukünftig)",
    [UndefinedDate.UNDEFINED_NOT_PRESENT]: "nicht vorhanden",
  }

const dropdownItems: DropdownItem[] = Object.entries(
  ENTRY_INTO_FORCE_DATE_TRANSLATIONS
).map(([value, label]) => {
  return { label, value }
})

const undefinedDateState = computed({
  get: () => inputValue.value.UNDEFINED_DATE?.[0],
  set: (data?: UndefinedDate) => {
    if (data) {
      inputValue.value.UNDEFINED_DATE = [data]
    }
    inputValue.value.DATE = undefined
  },
})

const dateValue = computed({
  get: () => inputValue.value.DATE?.[0],
  set: (value) => {
    inputValue.value.DATE = value ? [value] : undefined
    inputValue.value.UNDEFINED_DATE = undefined
  },
})
</script>

<template>
  <div class="w-320">
    <div class="flex justify-between mb-24">
      <label class="form-control">
        <input
          id="entryIntoForceSelection"
          v-model="selectedInputType"
          aria-label="Auswahl bestimmtes Datum des Inkrafttretens"
          name="EntryIntoForceDefined"
          type="radio"
          :value="InputType.DATE"
        />
        bestimmt
      </label>
      <label class="form-control">
        <input
          id="entryIntoForceUndefinedSelection"
          v-model="selectedInputType"
          aria-label="Auswahl unbestimmtes Datum des Inkrafttretens"
          name="EntryIntoForceUndefined"
          type="radio"
          :value="InputType.UNDEFINED_DATE"
        />
        unbestimmt
      </label>
    </div>
    <InputField
      v-if="selectedInputType === InputType.DATE"
      id="entryIntoForceDate"
      aria-label="Bestimmtes Inkrafttretedatum"
      label="Bestimmtes Inkrafttretedatum"
    >
      <DateInput
        id="entryIntoForceDate"
        v-model="dateValue"
        aria-label="Bestimmtes Inkrafttretedatum Date Input"
        is-future-date
      />
    </InputField>
    <InputField
      v-if="selectedInputType === InputType.UNDEFINED_DATE"
      id="entryIntoForceUndefinedDateState"
      aria-label="Unbestimmtes Inkrafttretedatum"
      label="Unbestimmtes Inkrafttretedatum"
    >
      <DropdownInput
        id="entryIntoForceUndefinedDateState"
        v-model="undefinedDateState"
        aria-label="Unbestimmtes Inkrafttretedatum Dropdown"
        has-smaller-height
        :items="dropdownItems"
        placeholder="Bitte auswählen"
      />
    </InputField>
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
