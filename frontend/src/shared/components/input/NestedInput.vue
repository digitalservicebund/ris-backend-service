<script lang="ts" setup>
import { computed } from "vue"
import InputElement from "@/shared/components/input/InputElement.vue"
import InputFieldComponent from "@/shared/components/input/InputField.vue"
import SubField from "@/shared/components/input/SubField.vue"
import {
  ValidationError,
  NestedInputAttributes,
  NestedInputModelType,
} from "@/shared/components/input/types"
import { useInputModel } from "@/shared/composables/useInputModel"

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

const { inputValue } = useInputModel<NestedInputModelType, Props, Emits>(
  props,
  emits
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
</script>

<template>
  <div>
    <InputFieldComponent
      :id="fields.parent.name"
      :key="fields.parent.name"
      class="input-group__row__field"
      :label="fields.parent.label"
      :required="fields.parent.required"
    >
      <InputElement
        :id="fields.parent.name"
        v-model="parentValue"
        :attributes="fields.parent.inputAttributes"
        :type="fields.parent.type"
      ></InputElement>
    </InputFieldComponent>

    <SubField :aria-label="ariaLabel">
      <div class="mt-[3.5rem]">
        <InputFieldComponent
          :id="fields.child.name"
          :key="fields.child.name"
          class="input-group__row__field"
          :label="fields.child.label"
          :required="fields.child.required"
        >
          <InputElement
            :id="fields.child.name"
            v-model="childValue"
            :attributes="fields.child.inputAttributes"
            :type="fields.child.type"
          ></InputElement>
        </InputFieldComponent>
      </div>
    </SubField>
  </div>
</template>
