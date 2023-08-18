<script lang="ts" setup>
import { produce } from "immer"
import { computed } from "vue"
import { Metadata } from "@/domain/norm"
import DateInput from "@/shared/components/input/DateInput.vue"
import InputField, {
  LabelPosition,
} from "@/shared/components/input/InputField.vue"
import RadioInput from "@/shared/components/input/RadioInput.vue"
import TimeInput from "@/shared/components/input/TimeInput.vue"
import { InputType } from "@/shared/components/input/types"
import YearInput from "@/shared/components/input/YearInput.vue"

interface Props {
  modelValue: Metadata
}

const props = defineProps<Props>()

const emit = defineEmits<{
  "update:modelValue": [value: Metadata]
}>()

const selectedInputType = computed({
  get: () => (props.modelValue.YEAR ? InputType.YEAR : InputType.DATE_TIME),
  set: (value) => {
    emit(
      "update:modelValue",
      value === InputType.DATE_TIME ? { DATE: [], TIME: [] } : { YEAR: [] },
    )
  },
})

const dateValue = computed({
  get: () => props.modelValue.DATE?.[0] ?? "",
  set: (value) => {
    const next = produce(props.modelValue, (draft) => {
      draft.DATE = value ? [value] : undefined
      draft.TIME = props.modelValue.TIME
    })
    emit("update:modelValue", next)
  },
})

const timeValue = computed({
  get: () => props.modelValue.TIME?.[0] ?? "",
  set: (value) => {
    const next = produce(props.modelValue, (draft) => {
      draft.TIME = value ? [value] : undefined
      draft.DATE = props.modelValue.DATE
    })
    emit("update:modelValue", next)
  },
})

const yearValue = computed({
  get: () => props.modelValue.YEAR?.[0] ?? "",
  set: (value) => {
    const next = produce(props.modelValue, (draft) => {
      draft.YEAR = value ? [value] : []
    })
    emit("update:modelValue", next)
  },
})
</script>

<template>
  <div>
    <div class="mb-8 flex gap-96">
      <div>
        <InputField
          id="announcementDateSelection"
          v-slot="{ id }"
          label="Datum"
          :label-position="LabelPosition.RIGHT"
        >
          <RadioInput
            :id="id"
            v-model="selectedInputType"
            name="announcementDateSelection"
            size="medium"
            :value="InputType.DATE_TIME"
          />
        </InputField>
      </div>

      <div>
        <InputField
          id="announcementYearSelection"
          v-slot="{ id }"
          label="Jahresangabe"
          :label-position="LabelPosition.RIGHT"
        >
          <RadioInput
            :id="id"
            v-model="selectedInputType"
            name="announcementDateSelection"
            size="medium"
            :value="InputType.YEAR"
          />
        </InputField>
      </div>
    </div>

    <div v-if="selectedInputType === InputType.DATE_TIME" class="flex gap-24">
      <div class="w-288">
        <InputField
          id="announcementDateInput"
          v-slot="{ id, hasError, updateValidationError }"
          label="Datum"
        >
          <DateInput
            :id="id"
            v-model="dateValue"
            aria-label="Datum"
            :has-error="hasError"
            is-future-date
            @update:validation-error="updateValidationError"
          />
        </InputField>
      </div>

      <div class="w-288">
        <InputField id="announcementDateTime" v-slot="{ id }" label="Uhrzeit">
          <TimeInput :id="id" v-model="timeValue" aria-label="Uhrzeit" />
        </InputField>
      </div>
    </div>

    <div v-if="selectedInputType === InputType.YEAR" class="w-112">
      <InputField
        id="announcementDateYearInput"
        v-slot="{ id, hasError, updateValidationError }"
        label="Jahresangabe"
      >
        <YearInput
          :id="id"
          v-model="yearValue"
          aria-label="Jahresangabe"
          :has-error="hasError"
          @update:validation-error="updateValidationError"
        />
      </InputField>
    </div>
  </div>
</template>
