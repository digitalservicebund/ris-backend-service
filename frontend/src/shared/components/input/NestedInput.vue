<script lang="ts" setup>
import { computed, ref } from "vue"
import InputElement from "@/shared/components/input/InputElement.vue"
import InputFieldComponent from "@/shared/components/input/InputField.vue"
import {
  ValidationError,
  NestedInputAttributes,
  NestedInputModelType,
} from "@/shared/components/input/types"
import { useInputModel } from "@/shared/composables/useInputModel"
import IconAdd from "~icons/ic/baseline-add"
import IconHorizontalRule from "~icons/ic/baseline-horizontal-rule"

interface Props {
  ariaLabel: string
  value?: NestedInputModelType
  modelValue?: NestedInputModelType
  fields: NestedInputAttributes["fields"]
  validationError?: ValidationError
}

interface Emits {
  (event: "update:modelValue", value?: NestedInputModelType): void
  (event: "input", value: Event): void
}

const props = defineProps<Props>()
const emits = defineEmits<Emits>()
const localIsExpanded = ref(false)

const { inputValue } = useInputModel<NestedInputModelType, Props, Emits>(
  props,
  emits,
)

const parentValue = computed({
  get: () => inputValue.value?.fields.parent,
  set: (value) => {
    if (inputValue.value) inputValue.value.fields.parent = value
  },
})

const childValue = computed({
  get: () => inputValue.value?.fields.child,
  set: (value) => {
    if (inputValue.value) inputValue.value.fields.child = value
  },
})

function toggleContentVisibility(): void {
  localIsExpanded.value = !localIsExpanded.value
}
</script>

<template>
  <div class="relative">
    <div class="absolute -right-10 top-48 z-10">
      <button
        class="w-icon rounded-full bg-blue-800 text-white"
        @click="toggleContentVisibility"
      >
        <slot
          v-if="localIsExpanded"
          :aria-label="ariaLabel + ' schließen'"
          class="w-icon rounded-full bg-blue-800 text-white"
          name="close-icon"
        >
          <IconHorizontalRule />
        </slot>
        <slot v-else :aria-label="ariaLabel + ' anzeigen'" name="open-icon">
          <IconAdd />
        </slot>
      </button>
    </div>

    <!-- Parent Element -->
    <InputFieldComponent
      :id="fields.parent.name"
      :key="fields.parent.name"
      v-slot="{ id, hasError, updateValidationError }"
      :label="fields.parent.label"
      :required="fields.parent.required"
    >
      <InputElement
        :id="id"
        v-model="parentValue"
        :attributes="fields.parent.inputAttributes"
        :has-error="hasError"
        :type="fields.parent.type"
        @update:validation-error="updateValidationError"
      ></InputElement>
    </InputFieldComponent>

    <!-- Child Element -->
    <InputFieldComponent
      v-show="localIsExpanded"
      :id="fields.child.name"
      :key="fields.child.name"
      v-slot="{ id, hasError, updateValidationError }"
      :label="fields.child.label"
      :required="fields.child.required"
    >
      <InputElement
        :id="id"
        v-model="childValue"
        :attributes="fields.child.inputAttributes"
        :has-error="hasError"
        :type="fields.child.type"
        @update:validation-error="updateValidationError"
      ></InputElement>
    </InputFieldComponent>
  </div>
</template>
