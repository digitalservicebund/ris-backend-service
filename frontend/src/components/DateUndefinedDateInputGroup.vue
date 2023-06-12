<script lang="ts" setup>
import { ref } from "vue"
import { UndefinedDate } from "@/domain/Norm"
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
  selectedInputType: string
}

interface Emits {
  (event: "update:dateValue", value: string): void
  (
    event: "update:undefinedDateStateValue",
    value: UndefinedDate | undefined
  ): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const undefineDateValue = ref(props.undefinedDateStateValue)
const dateValue = ref(props.dateValue)

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

interface DropdownItem {
  label: string
  value: string
}

enum InputType {
  DATE = "date",
  UNDEFINED_DATE = "undefined_date",
}
</script>

<template>
  <div class="w-320">
    <InputField
      v-if="selectedInputType === InputType.DATE"
      :id="dateId"
      :aria-label="dateInputFieldLabel"
      :label="dateInputFieldLabel"
    >
      <DateInput
        :id="dateId"
        :aria-label="dateInputAriaLabel"
        is-future-date
        :model-value="dateValue"
        @update:model-value="emit('update:dateValue', $event)"
      />
    </InputField>
    <InputField
      v-if="selectedInputType === InputType.UNDEFINED_DATE"
      :id="undefinedDateId"
      :aria-label="undefinedDateInputFieldLabel"
      :label="undefinedDateInputFieldLabel"
    >
      <DropdownInput
        :id="undefinedDateId"
        :aria-label="undefinedDateDropdownAriaLabel"
        has-smaller-height
        :items="dropdownItems"
        :model-value="undefineDateValue"
        placeholder="Bitte auswählen"
        @update:model-value="emit('update:undefinedDateStateValue', $event)"
      />
    </InputField>
  </div>
</template>
