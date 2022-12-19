<script lang="ts" setup>
import { ref } from "vue"
import CheckboxInput from "@/components/CheckboxInput.vue"
import ChipsDateInput from "@/components/ChipsDateInput.vue"
import ChipsInput from "@/components/ChipsInput.vue"
import DateInput from "@/components/DateInput.vue"
import ComboboxInput from "@/components/DropdownInput.vue"
import NestedInput from "@/components/NestedInput.vue"
import TextInput from "@/components/TextInput.vue"
import {
  defineDateField,
  DateInputModelType,
  ValidationError,
  NestedInputAttributes,
} from "@/domain"
import type { ChipsInputModelType, DropdownItem } from "@/domain/types"
import dropdownItems from "@/kitchensink/data/dropdownItems.json"

const items: DropdownItem[] = dropdownItems.items
const comboboxModelValue = ref<string>()
const dateModelValue = ref<DateInputModelType>()
const chipsModelValue = ref<ChipsInputModelType>(["one", "two"])
const chipsDateModelValue = ref<ChipsInputModelType>(["2022-01-31T23:00:00Z"])
const isReadonly = ref(true)
const mockValidationError: ValidationError = {
  defaultMessage: "wrong date",
  field: "coreData.decisionDate",
}
const nestedInputFields: NestedInputAttributes["fields"] = {
  parent: defineDateField("field", "Input", "Input", undefined),
  child: defineDateField(
    "deviatingField",
    "Abweichender Input",
    "Abweichender Input",
    undefined
  ),
}

const updateComboboxModelValue = (textValue?: string) => {
  if (!!textValue) comboboxModelValue.value = textValue
}
</script>

<template>
  <div class="flex flex-col gap-y-20 h-auto w-1/2">
    <h1 class="font-bold text-24">Text Input</h1>
    <div>
      <TextInput id="textInputEmpty" aria-label="text input" value="" />
    </div>
    <div>
      <TextInput
        id="textInputWithValue"
        aria-label="text input"
        value="this is a text input with a value"
      />
    </div>
    <div>
      <TextInput
        id="textInputPlaceholder"
        aria-label="text input"
        placeholder="this is text input placeholder"
        value=""
      />
    </div>
    <div>
      <TextInput
        id="textInputError"
        aria-label="text input"
        :validation-error="mockValidationError"
        value="wrong value"
      />
    </div>
    <div>
      <TextInput
        id="textInputReadonly"
        aria-label="text input"
        placeholder="this is text input readonly"
        :read-only="isReadonly"
        value=""
      />
    </div>
    <h1 class="font-bold text-24">Date Input</h1>
    <DateInput
      id="dateInput"
      aria-label="date input"
      :model-value="dateModelValue"
      :value="dateModelValue"
    ></DateInput>
    <h1 class="font-bold text-24">Dropdown Input</h1>
    <div class="pb-4">
      <ComboboxInput
        id="comboboxInput"
        aria-label="combobox input"
        :items="items"
        :model-value="comboboxModelValue"
        placeholder="Bitte auswählen"
        :value="comboboxModelValue"
        @update:model-value="updateComboboxModelValue"
      />
    </div>

    <h1 class="font-bold text-24">Chips Input</h1>
    <ChipsInput
      id="ChipsInput"
      aria-label="chips input"
      :model-value="chipsModelValue"
      :value="chipsModelValue"
    ></ChipsInput>

    <h1 class="font-bold text-24">Chips Date Input</h1>
    <ChipsDateInput
      id="ChipsDateInput"
      aria-label="chips date input"
      :model-value="chipsDateModelValue"
      :value="chipsDateModelValue"
    ></ChipsDateInput>

    <h1 class="font-bold text-24">Nested Input</h1>
    <div class="mb-24">
      <NestedInput
        aria-label="Nested Input"
        :fields="nestedInputFields"
      ></NestedInput>
    </div>

    <h1 class="font-bold text-24">Checkbox Input</h1>

    Regular Checkbox
    <CheckboxInput id="regularCheckbox" aria-label="regular checkbox" />

    Checked Checkbox
    <CheckboxInput id="checkedCheckbox" aria-label="checked checkbox" value />

    Disabled Checkbox
    <CheckboxInput
      id="disabedCheckbox"
      aria-label="disabled checkbox"
      disabled
    />

    Disabled and checked Checkbox
    <CheckboxInput
      id="disabledAndCheckedCheckbox"
      aria-label="disabled checkbox"
      disabled
      value
    />

    Invalid Checkbox
    <CheckboxInput
      id="invalidCheckbox"
      aria-label="invalid checkbox"
      :validation-error="{
        defaultMessage: 'error message',
        field: 'invalidCheckbox',
      }"
    />
  </div>
</template>
