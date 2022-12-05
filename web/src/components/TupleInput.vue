<script lang="ts" setup>
import { computed } from "vue"
import InputElement from "@/components/InputElement.vue"
import InputFieldComponent from "@/components/InputField.vue"
import SubField from "@/components/SubField.vue"
import { useInputModel } from "@/composables/useInputModel"
import { ValidationError } from "@/domain"
import type { InputField, ModelType } from "@/domain"

interface Props {
  ariaLabel: string
  value?: { parent: ModelType; child: ModelType }
  modelValue?: { parent: ModelType; child: ModelType }
  fields: { parent: InputField; child: InputField }
  validationError?: ValidationError
}

interface Emits {
  (
    event: "update:modelValue",
    value: { parent: ModelType; child: ModelType } | undefined
  ): void
  (event: "input", value: Event): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const { inputValue } = useInputModel<
  { parent: ModelType; child: ModelType },
  Props,
  Emits
>(props, emit)

// const parentValue = computed({
//   get: () => (props.modelValue ? props.modelValue.parent : undefined),
//   set: (value) => {
//     if (inputValue.value) inputValue.value.parent = value
//   },
// })

const parentValue = computed({
  get: () => inputValue.value?.parent,
  set: (value) => {
    if (value && inputValue.value) inputValue.value.parent = value
  },
})

const childValue = computed({
  get: () => inputValue.value?.child,
  set: (value) => {
    if (value && inputValue.value) inputValue.value.child = value
  },
})

// const value = computed({
//   get: () => props.modelValue,
//   set: (newValue) => emit("update:modelValue", newValue),
// })
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

    <SubField>
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
