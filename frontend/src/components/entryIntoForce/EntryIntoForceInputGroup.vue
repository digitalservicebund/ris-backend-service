<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import DateUndefinedDateInputGroup from "@/components/DateUndefinedDateInputGroup.vue"
import { Metadata, UndefinedDate } from "@/domain/norm"
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
        id="entryIntoForceSelection"
        v-slot="{ id }"
        label="bestimmt"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput
          :id="id"
          v-model="selectedInputType"
          name="entryIntoForce"
          size="medium"
          :value="InputType.DATE"
        />
      </InputField>

      <InputField
        id="entryIntoForceUndefinedSelection"
        v-slot="{ id }"
        label="unbestimmt"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput
          :id="id"
          v-model="selectedInputType"
          name="entryIntoForce"
          size="medium"
          :value="InputType.UNDEFINED_DATE"
        />
      </InputField>
    </div>

    <DateUndefinedDateInputGroup
      v-model:date-value="dateValue"
      v-model:undefined-date-state-value="undefinedDateState"
      date-id="entryIntoForceDate"
      date-input-aria-label="Bestimmtes Inkrafttretedatum Date Input"
      date-input-field-label="Bestimmtes Inkrafttretedatum"
      :selected-input-type="selectedInputType"
      undefined-date-dropdown-aria-label="Bestimmtes Inkrafttretedatum Date Input"
      undefined-date-id="entryIntoForceUndefinedDateState"
      undefined-date-input-field-label="Unbestimmtes Inkrafttretedatum"
    />
  </div>
</template>
