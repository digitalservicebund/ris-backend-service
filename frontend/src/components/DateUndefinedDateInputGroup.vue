<script lang="ts" setup>
import { ref } from "vue"
import { UndefinedDate } from "@/domain/Norm"
import DropdownInput from "@/shared/components/input/DropdownInput.vue"
import InputElement from "@/shared/components/input/InputElement.vue"
import InputField from "@/shared/components/input/InputField.vue"
import { InputType } from "@/shared/components/input/types"

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

const props = defineProps<Props>()

defineEmits<{
  "update:dateValue": [value: string]
  "update:undefinedDateStateValue": [value?: UndefinedDate]
}>()

const undefineDateValue = ref(props.undefinedDateStateValue)
const dateValue = ref(props.dateValue)

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
      v-if="selectedInputType === InputType.DATE"
      :id="dateId"
      :aria-label="dateInputFieldLabel"
      :label="dateInputFieldLabel"
    >
      <InputElement
        :id="dateId"
        v-model="dateValue"
        :attributes="{ ariaLabel: dateInputAriaLabel }"
        is-future-date
        :type="InputType.DATE"
        @update:model-value="$emit('update:dateValue', $event)"
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
        :items="dropdownItems"
        :model-value="undefineDateValue"
        placeholder="Bitte auswählen"
        @update:model-value="$emit('update:undefinedDateStateValue', $event)"
      />
    </InputField>
  </div>
</template>
