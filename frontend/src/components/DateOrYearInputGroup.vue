<script lang="ts" setup>
import { MetadatumType } from "@/domain/norm"
import DateInput from "@/shared/components/input/DateInput.vue"
import InputField, {
  LabelPosition,
} from "@/shared/components/input/InputField.vue"
import RadioInput from "@/shared/components/input/RadioInput.vue"
import YearInput from "@/shared/components/input/YearInput.vue"

defineProps<{
  idPrefix: string
  label: string
  dateValue: string | undefined
  yearValue: string | undefined
  selectedInputType: MetadatumType.YEAR | MetadatumType.DATE
}>()

defineEmits<{
  "update:dateValue": [value: string | undefined]
  "update:yearValue": [value: string | undefined]
  "update:selectedInputType": [value: string | undefined]
}>()
</script>

<template>
  <div class="w-320">
    <div class="mb-8 flex justify-between">
      <InputField
        :id="`${idPrefix}TypeDate`"
        v-slot="{ id }"
        label="Datum"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput
          :id="id"
          :model-value="selectedInputType"
          :name="`${idPrefix}InputType`"
          size="medium"
          :value="MetadatumType.DATE"
          @update:model-value="$emit('update:selectedInputType', $event)"
        />
      </InputField>

      <InputField
        :id="`${idPrefix}TypeYear`"
        v-slot="{ id }"
        label="Jahresangabe"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput
          :id="id"
          :model-value="selectedInputType"
          :name="`${idPrefix}InputType`"
          size="medium"
          :value="MetadatumType.YEAR"
          @update:model-value="$emit('update:selectedInputType', $event)"
        />
      </InputField>
    </div>

    <InputField
      :id="
        selectedInputType === MetadatumType.DATE
          ? `${idPrefix}Date`
          : `${idPrefix}Year`
      "
      v-slot="{ id, hasError, updateValidationError }"
      class="mb-0"
      :label="label"
      :label-position="LabelPosition.TOP"
    >
      <DateInput
        v-if="selectedInputType === MetadatumType.DATE"
        :id="id"
        :aria-label="label"
        :has-error="hasError"
        is-future-date
        :model-value="dateValue"
        @update:model-value="$emit('update:dateValue', $event)"
        @update:validation-error="updateValidationError"
      />
      <YearInput
        v-else-if="selectedInputType === MetadatumType.YEAR"
        :id="id"
        :aria-label="`${label} Jahresangabe`"
        :has-error="hasError"
        :model-value="yearValue"
        @update:model-value="$emit('update:yearValue', $event)"
        @update:validation-error="updateValidationError"
      />
    </InputField>
  </div>
</template>
