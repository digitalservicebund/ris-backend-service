<script lang="ts" setup>
import { ref } from "vue"
import CheckboxInput from "@/components/CheckboxInput.vue"
import DropdownInput from "@/components/DropdownInput.vue"
import InputElement from "@/components/InputElement.vue"
import InputField from "@/components/InputField.vue"
import TextInput from "@/components/TextInput.vue"
import type {
  DropdownInputModelType,
  InputAttributes,
  ModelType,
} from "@/domain"
import { InputType, ValidationError, defineTextField } from "@/domain"
import type { DropdownItem } from "@/domain/types"
import dropdownItems from "@/kitchensink/data/dropdownItems.json"

const items: DropdownItem[] = dropdownItems.items
const modelValue1 = ref<DropdownInputModelType>()
const modelValue2 = ref<ModelType>()
const isReadonly = ref(true)
const mockValidationError: ValidationError = {
  defaultMessage: "wrong date",
  field: "coreData.decisionDate",
}
const updateValue1 = (textValue: DropdownInputModelType | undefined) => {
  if (!!textValue) modelValue1.value = textValue
}
const updateValue2 = (textValue: ModelType | undefined) => {
  if (!!textValue) modelValue2.value = textValue
}
const textInputAttribute: InputAttributes = {
  ariaLabel: "text input",
  placeholder: "This is a text field",
}
const dropdownInputAttribute: InputAttributes = {
  ariaLabel: "text input",
  placeholder: "This is a dropdown",
  dropdownItems: items,
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
    <div>
      <TextInput
        id="textInputWithValue"
        aria-label="text input"
        :sub-field="
          defineTextField(
            'testField',
            'Divergent Subfield',
            'Divergent Subfield',
            true
          )
        "
        value="this input has a subcategory"
      />
    </div>

    <h1 class="font-bold text-24">Dropdown Input</h1>
    <div class="pb-4">
      <DropdownInput
        id="dropdownInput"
        aria-label="dropdown input"
        :dropdown-items="items"
        :model-value="modelValue1"
        placeholder="Bitte auswählen"
        :value="modelValue1"
        @update:model-value="updateValue1"
      />
    </div>
    <h1 class="font-bold text-24">Input Element</h1>
    <span class="text-20">Type="Textfield"</span>
    <InputElement :attributes="textInputAttribute" :type="InputType.TEXT" />
    <span class="text-20">Type="Dropdown"</span>
    <div class="pb-4">
      <InputElement
        :attributes="dropdownInputAttribute"
        :type="InputType.DROPDOWN"
        :value="modelValue2"
        @update:model-value="updateValue2"
      />
    </div>
    <h1 class="font-bold text-24">Input Field</h1>
    <InputField id="inputTextBox" label="This input field has only label"
      ><InputElement :attributes="textInputAttribute" :type="InputType.TEXT" />
    </InputField>
    <InputField
      id="inputTextBox"
      icon-name="location_on"
      label="This input field is not required"
      ><InputElement :attributes="textInputAttribute" :type="InputType.TEXT" />
    </InputField>
    <InputField
      id="inputTextBox"
      icon-name="location_on"
      label="This input field is required"
      required
      ><InputElement :attributes="textInputAttribute" :type="InputType.TEXT" />
    </InputField>
    <InputField
      id="inputTextBox"
      icon-name="location_on"
      label="This input field is a dropdown"
    >
      <InputElement
        :attributes="dropdownInputAttribute"
        :type="InputType.DROPDOWN"
      />
    </InputField>

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
