<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import DateUndefinedDateInputGroup from "@/components/DateUndefinedDateInputGroup.vue"
import { Metadata, UndefinedDate } from "@/domain/Norm"

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

const inputValue = ref(props.modelValue)
const selectedInputType = ref<InputType>(InputType.UNDEFINED_DATE)

function detectSelectedInputType(): void {
  if (inputValue.value.DATE && inputValue.value.DATE.length > 0) {
    selectedInputType.value = InputType.DATE
  } else selectedInputType.value = InputType.UNDEFINED_DATE
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
          id="principleExpirationSelection"
          v-model="selectedInputType"
          aria-label="Auswahl bestimmtes grundsätzliches Außerkrafttretedatum"
          name="principleExpirationDefined"
          type="radio"
          :value="InputType.DATE"
        />
        bestimmt
      </label>
      <label class="form-control">
        <input
          id="principleExpirationUndefinedSelection"
          v-model="selectedInputType"
          aria-label="Auswahl unbestimmtes grundsätzliches Außerkrafttretedatum"
          name="principleExpirationUndefined"
          type="radio"
          :value="InputType.UNDEFINED_DATE"
        />
        unbestimmt
      </label>
    </div>
    <DateUndefinedDateInputGroup
      v-model:date-value="dateValue"
      v-model:undefined-date-state-value="undefinedDateState"
      date-id="principleExpirationDate"
      date-input-aria-label="Bestimmtes grundsätzliches Außerkrafttretedatum Date Input"
      date-input-field-label="Bestimmtes grundsätzliches Außerkrafttretedatum"
      :selected-input-type="selectedInputType"
      undefined-date-dropdown-aria-label="Unbestimmtes grundsätzliches Außerkrafttretedatum Dropdown"
      undefined-date-id="principleExpirationUndefinedDate"
      undefined-date-input-field-label="Unbestimmtes grundsätzliches Außerkrafttretedatum"
    />
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
