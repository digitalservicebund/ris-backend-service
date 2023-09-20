<script lang="ts" setup>
import { computed } from "vue"
import { Metadata, MetadatumType } from "@/domain/norm"
import DateInput from "@/shared/components/input/DateInput.vue"
import InputField, {
  LabelPosition,
} from "@/shared/components/input/InputField.vue"
import RadioInput from "@/shared/components/input/RadioInput.vue"
import TimeInput from "@/shared/components/input/TimeInput.vue"
import YearInput from "@/shared/components/input/YearInput.vue"

const props = defineProps<{
  modelValue: Metadata
}>()

const emit = defineEmits<{
  "update:modelValue": [value: Metadata]
}>()

/* -------------------------------------------------- *
 * Section type                                       *
 * -------------------------------------------------- */

const initialValue: Metadata = {
  YEAR: props.modelValue.YEAR,
  DATE: props.modelValue.DATE,
  TIME: props.modelValue.TIME,
}

const selectedInputType = computed<MetadatumType.YEAR | MetadatumType.DATE>({
  get() {
    if (props.modelValue.DATE) {
      return MetadatumType.DATE
    } else if (props.modelValue.YEAR) {
      return MetadatumType.YEAR
    } else {
      return MetadatumType.DATE
    }
  },
  set(value) {
    if (value === MetadatumType.DATE) {
      emit("update:modelValue", {
        ...initialValue,
        DATE: initialValue.DATE ?? [],
        YEAR: undefined,
      })
    } else if (value === MetadatumType.YEAR) {
      emit("update:modelValue", {
        YEAR: initialValue.YEAR ?? [],
      })
    }
  },
})

/* -------------------------------------------------- *
 * Section data                                       *
 * -------------------------------------------------- */

const date = computed({
  get: () => props.modelValue.DATE?.[0] ?? "",
  set: (data) => {
    const effectiveData = data ? [data] : undefined
    initialValue.DATE = effectiveData

    const next: Metadata = { DATE: effectiveData }
    emit("update:modelValue", next)
  },
})

const year = computed({
  get: () => props.modelValue.YEAR?.[0] ?? "",
  set: (data) => {
    const effectiveData = data ? [data] : undefined
    initialValue.YEAR = effectiveData

    const next: Metadata = { YEAR: effectiveData }
    emit("update:modelValue", next)
  },
})

const time = computed({
  get: () => props.modelValue.TIME?.[0] ?? "",
  set: (data) => {
    let effectiveData

    if (props.modelValue.DATE) {
      effectiveData = data ? [data] : undefined
    } else {
      effectiveData = undefined
    }
    initialValue.TIME = effectiveData

    const next: Metadata = {
      ...props.modelValue,
      TIME: effectiveData,
    }
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
            :value="MetadatumType.DATE"
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
            :value="MetadatumType.YEAR"
          />
        </InputField>
      </div>
    </div>

    <div v-if="selectedInputType === MetadatumType.DATE" class="flex gap-24">
      <div class="w-288">
        <InputField
          id="announcementDateInput"
          v-slot="{ id, hasError, updateValidationError }"
          label="Datum"
        >
          <DateInput
            :id="id"
            v-model="date"
            aria-label="Datum"
            :has-error="hasError"
            is-future-date
            @update:validation-error="updateValidationError"
          />
        </InputField>
      </div>

      <div class="w-288">
        <InputField id="announcementDateTime" v-slot="{ id }" label="Uhrzeit">
          <TimeInput :id="id" v-model="time" aria-label="Uhrzeit" />
        </InputField>
      </div>
    </div>

    <div v-if="selectedInputType === MetadatumType.YEAR" class="w-112">
      <InputField
        id="announcementDateYearInput"
        v-slot="{ id, hasError, updateValidationError }"
        label="Jahresangabe"
      >
        <YearInput
          :id="id"
          v-model="year"
          aria-label="Jahresangabe"
          :has-error="hasError"
          @update:validation-error="updateValidationError"
        />
      </InputField>
    </div>
  </div>
</template>
