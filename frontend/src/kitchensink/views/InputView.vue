<script lang="ts" setup>
import { ref } from "vue"
import CheckboxInput from "@/components/CheckboxInput.vue"
import ChipsDateInput from "@/components/ChipsDateInput.vue"
import ChipsInput from "@/components/ChipsInput.vue"
import DateInput from "@/components/DateInput.vue"
import Dropdown from "@/components/DropdownInput.vue"
import NestedInput from "@/components/NestedInput.vue"
import TextInput from "@/components/TextInput.vue"
import {
  defineDateField,
  DateInputModelType,
  ValidationError,
  NestedInputAttributes,
} from "@/domain"
import type { ChipsInputModelType } from "@/domain/types"

const dateModelValue = ref<DateInputModelType>()
const chipsModelValue = ref<ChipsInputModelType>(["one", "two"])
const chipsDateModelValue = ref<ChipsInputModelType>(["2022-01-31T23:00:00Z"])
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

const dropdownItems = [
  { label: "Item 1", value: "1" },
  { label: "Item 2", value: "2" },
  { label: "Item 3", value: "3" },
  { label: "Item 4", value: "4" },
  { label: "Item 5", value: "5" },
]
</script>

<template>
  <div class="flex flex-col gap-y-20 h-auto w-1/2">
    <h1 class="font-bold text-24">Text Input</h1>

    <h2>Regular Text Input</h2>
    <TextInput id="regularTextInput" aria-label="regular text input" value="" />

    <h2>Text Input with Placeholder</h2>
    <TextInput
      id="textInputWithPlaceholder"
      aria-label="text input with placeholder"
      placeholder="Placeholder"
      value=""
    />

    <h2>Filled Text Input</h2>
    <TextInput
      id="filledTextInput"
      aria-label="filled text input"
      value="Loremipsum"
    />

    <h2>Invalid Text Input</h2>
    <TextInput
      id="textInputError"
      aria-label="invalid text input"
      :validation-error="mockValidationError"
      value="Loremipsum"
    />

    <h2>Read-only Text Input</h2>
    <TextInput
      id="readonlyTextInput"
      aria-label="readonly text input"
      read-only
      value="Loremipsum"
    />

    <h1 class="font-bold text-24">Dropdown Input</h1>

    <h2>Regular Dropdown</h2>
    <Dropdown
      id="regularDropdown"
      aria-label="regular dropdown"
      :items="dropdownItems"
      value=""
    />

    <h1 class="font-bold text-24">Date Input</h1>
    <DateInput
      id="dateInput"
      aria-label="date input"
      :model-value="dateModelValue"
      :value="dateModelValue"
    ></DateInput>
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
