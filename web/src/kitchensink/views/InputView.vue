<script lang="ts" setup>
import { ref } from "vue"
import DropdownInput from "@/components/DropdownInput.vue"
import InputElement from "@/components/InputElement.vue"
import InputField from "@/components/InputField.vue"
import TextInput from "@/components/TextInput.vue"
import { InputType } from "@/domain"
import type { InputAttributes } from "@/domain"
import type { DropdownItem } from "@/domain/types"
import dropdownItems from "@/kitchensink/data/dropdownItems.json"
import { ValidationError } from "@/services/httpClient"

const items: DropdownItem[] = dropdownItems.items
const modelValue1 = ref("")
const modelValue2 = ref("")
const hasError = ref(true)
const isReadonly = ref(true)
const mockValidationError: ValidationError = {
  defaultMessage: "wrong date",
  field: "coreData.decisionDate",
}
const updateValue1 = (textValue: string | undefined) => {
  if (!!textValue) modelValue1.value = textValue
}
const updateValue2 = (textValue: string | undefined) => {
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
    <span class="text-20">Text input without value</span>
    <div>
      <TextInput id="textInputEmpty" aria-label="text input" value="" />
    </div>
    <span class="text-20">Text input has value</span>
    <div>
      <TextInput
        id="textInputWithValue"
        aria-label="text input"
        value="This is text input value"
      />
    </div>
    <span class="text-20">Text input has placeholder</span>
    <div>
      <TextInput
        id="textInputPlaceholder"
        aria-label="text input"
        placeholder="this is text input placeholder"
        value=""
      />
    </div>
    <span class="text-20">Text input has error</span>
    <div>
      <TextInput
        id="textInputError"
        aria-label="text input"
        :validation-error="mockValidationError"
        value="wrong value"
      />
    </div>
    <span class="text-20">Text input is readonly</span>
    <div>
      <TextInput
        id="textInputReadonly"
        aria-label="text input"
        :has-error="hasError"
        placeholder="this is text input readonly"
        :read-only="isReadonly"
        value=""
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
    <span class="text-20">Type="FileInput"</span>
    <InputElement
      :attributes="textInputAttribute"
      class="w-[150px]"
      :type="InputType.FILE"
    />
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
      <div class="h-[250px]">
        <InputElement
          :attributes="dropdownInputAttribute"
          :type="InputType.DROPDOWN"
        />
      </div>
    </InputField>
  </div>
</template>
