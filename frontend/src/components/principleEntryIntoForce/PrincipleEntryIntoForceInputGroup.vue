<script lang="ts" setup>
/* v8 ignore start */
import { computed } from "vue"
import DateUndefinedDateInputGroup from "@/components/DateUndefinedDateInputGroup.vue"
import { Metadata, MetadatumType } from "@/domain/norm"
import InputField, {
  LabelPosition,
} from "@/shared/components/input/InputField.vue"
import RadioInput from "@/shared/components/input/RadioInput.vue"

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
  UNDEFINED_DATE: props.modelValue.UNDEFINED_DATE,
  DATE: props.modelValue.DATE,
}

const selectedInputType = computed<
  MetadatumType.UNDEFINED_DATE | MetadatumType.DATE
>({
  get() {
    if (props.modelValue.UNDEFINED_DATE) {
      return MetadatumType.UNDEFINED_DATE
    } else if (props.modelValue.DATE) {
      return MetadatumType.DATE
    } else {
      return MetadatumType.DATE
    }
  },
  set(value) {
    emit("update:modelValue", { [value]: initialValue[value] ?? [] })
  },
})

/* -------------------------------------------------- *
 * Section data                                       *
 * -------------------------------------------------- */

const undefinedDateSection = computed({
  get: () => props.modelValue.UNDEFINED_DATE?.[0],
  set: (data) => {
    const effectiveData = data ? [data] : undefined
    initialValue.UNDEFINED_DATE = effectiveData

    const next: Metadata = { UNDEFINED_DATE: effectiveData }
    emit("update:modelValue", next)
  },
})

const dateSection = computed({
  get: () => props.modelValue.DATE?.[0],
  set: (data) => {
    const effectiveData = data ? [data] : undefined
    initialValue.DATE = effectiveData

    const next: Metadata = { DATE: effectiveData }
    emit("update:modelValue", next)
  },
})
/* v8 ignore stop */
</script>

<template>
  <div class="w-320">
    <div class="mb-8 flex justify-between">
      <InputField
        id="principleEntryIntoForceSelection"
        v-slot="{ id }"
        label="bestimmt"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput
          :id="id"
          v-model="selectedInputType"
          name="principleEntryIntoForce"
          size="medium"
          :value="MetadatumType.DATE"
        />
      </InputField>

      <InputField
        id="principleEntryIntoForceUndefinedSelection"
        v-slot="{ id }"
        label="unbestimmt"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput
          :id="id"
          v-model="selectedInputType"
          name="principleEntryIntoForce"
          size="medium"
          :value="MetadatumType.UNDEFINED_DATE"
        />
      </InputField>
    </div>

    <DateUndefinedDateInputGroup
      v-model:date-value="dateSection"
      v-model:undefined-date-state-value="undefinedDateSection"
      date-id="principleEntryIntoForceDate"
      date-input-aria-label="Bestimmtes grundsätzliches Inkrafttretedatum Date Input"
      date-input-field-label="Bestimmtes grundsätzliches Inkrafttretedatum"
      :selected-input-type="selectedInputType"
      undefined-date-dropdown-aria-label="Unbestimmtes grundsätzliches Inkrafttretedatum Dropdown"
      undefined-date-id="principleEntryIntoForceUndefinedDateState"
      undefined-date-input-field-label="Unbestimmtes grundsätzliches Inkrafttretedatum"
    />
  </div>
</template>
