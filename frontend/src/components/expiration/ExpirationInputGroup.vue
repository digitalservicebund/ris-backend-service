<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import DateUndefinedDateInputGroup from "@/components/DateUndefinedDateInputGroup.vue"
import { Metadata, UndefinedDate } from "@/domain/Norm"
import InputField, {
  LabelPosition,
} from "@/shared/components/input/InputField.vue"
import RadioInput from "@/shared/components/input/RadioInput.vue"

interface Props {
  modelValue: Metadata
}

const props = defineProps<Props>()

const emit = defineEmits<{
  "update:modelValue": [value: Metadata]
}>()

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
  { immediate: true },
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
    <div class="mb-8 flex justify-between">
      <InputField
        id="expirationSelection"
        v-slot="{ id }"
        label="bestimmt"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput
          :id="id"
          v-model="selectedInputType"
          name="expiration"
          size="medium"
          :value="InputType.DATE"
        />
      </InputField>

      <InputField
        id="expirationUndefinedSelection"
        v-slot="{ id }"
        label="unbestimmt"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput
          :id="id"
          v-model="selectedInputType"
          name="expiration"
          size="medium"
          :value="InputType.UNDEFINED_DATE"
        />
      </InputField>
    </div>

    <DateUndefinedDateInputGroup
      v-model:date-value="dateValue"
      v-model:undefined-date-state-value="undefinedDateState"
      date-id="expirationDate"
      date-input-aria-label="Bestimmtes Außerkrafttretedatum Date Input"
      date-input-field-label="Bestimmtes Außerkrafttretedatum"
      :selected-input-type="selectedInputType"
      undefined-date-dropdown-aria-label="Unbestimmtes Außerkrafttretedatum Dropdown"
      undefined-date-id="expirationUndefinedDate"
      undefined-date-input-field-label="Unbestimmtes Außerkrafttretedatum"
    />
  </div>
</template>
