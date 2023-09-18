<script lang="ts" setup>
import { MetadatumType, UndefinedDate } from "@/domain/norm"
import DateInput from "@/shared/components/input/DateInput.vue"
import DropdownInput from "@/shared/components/input/DropdownInput.vue"
import InputField from "@/shared/components/input/InputField.vue"

interface Props {
  dateId: string
  dateInputFieldLabel: string
  dateInputAriaLabel: string
  undefinedDateId: string
  undefinedDateInputFieldLabel: string
  undefinedDateDropdownAriaLabel: string
  dateValue: string | undefined
  undefinedDateStateValue: UndefinedDate | undefined
  selectedInputType: MetadatumType.UNDEFINED_DATE | MetadatumType.DATE
}

defineProps<Props>()

defineEmits<{
  "update:dateValue": [value: string]
  "update:undefinedDateStateValue": [value?: UndefinedDate]
}>()

const ENTRY_INTO_FORCE_DATE_TRANSLATIONS: { [Value in UndefinedDate]: string } =
  {
    [UndefinedDate.UNDEFINED_UNKNOWN]: "unbestimmt (unbekannt)",
    [UndefinedDate.UNDEFINED_FUTURE]: "unbestimmt (zukünftig)",
    [UndefinedDate.UNDEFINED_NOT_PRESENT]: "nicht vorhanden",
  }

const dropdownItems: DropdownItem[] = Object.entries(
  ENTRY_INTO_FORCE_DATE_TRANSLATIONS,
).map(([value, label]) => {
  return { label, value }
})

interface DropdownItem {
  label: string
  value: string
}
</script>

<template>
  <div class="w-320">
    <InputField
      v-if="selectedInputType === MetadatumType.DATE"
      :id="dateId"
      v-slot="{ id, hasError, updateValidationError }"
      :aria-label="dateInputFieldLabel"
      :label="dateInputFieldLabel"
    >
      <DateInput
        :id="id"
        :aria-label="dateInputAriaLabel"
        :has-error="hasError"
        is-future-date
        :model-value="dateValue"
        @update:model-value="$emit('update:dateValue', $event)"
        @update:validation-error="updateValidationError"
      />
    </InputField>
    <InputField
      v-if="selectedInputType === MetadatumType.UNDEFINED_DATE"
      :id="undefinedDateId"
      v-slot="{ id }"
      :aria-label="undefinedDateInputFieldLabel"
      :label="undefinedDateInputFieldLabel"
    >
      <DropdownInput
        :id="id"
        :aria-label="undefinedDateDropdownAriaLabel"
        :items="dropdownItems"
        :model-value="undefinedDateStateValue"
        placeholder="Bitte auswählen"
        @update:model-value="$emit('update:undefinedDateStateValue', $event)"
      />
    </InputField>
  </div>
</template>
