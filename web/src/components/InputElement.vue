<template>
  <component :is="component" v-bind="attributes" v-model="value" />
</template>

<script lang="ts" setup>
import { computed } from "vue"
import FileInputButton from "@/components/FileInputButton.vue"
import TextInput from "@/components/TextInput.vue"
import { InputType } from "@/domain"
import type { InputAttributes, ModelType } from "@/domain"

interface Props {
  type?: InputType
  modelValue?: ModelType
  attributes: InputAttributes
}

interface Emits {
  (event: "update:modelValue", value: ModelType): void
}

const props = withDefaults(defineProps<Props>(), {
  type: InputType.TEXT,
  modelValue: undefined,
})

const emit = defineEmits<Emits>()

const component = computed(() => {
  switch (props.type) {
    case "text":
      return TextInput
    case "file":
      return FileInputButton
    default:
      throw new Error(`Unknown input type: ${props.type}`)
  }
})

const value = computed({
  get: () => props.modelValue,
  set: (newValue) => emit("update:modelValue", newValue),
})
</script>
