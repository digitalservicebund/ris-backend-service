<script lang="ts" setup>
import TextEditor from "../input/TextEditor.vue"
import { TextAreaInputAttributes } from "@/components/input/types"

interface Props {
  id: string
  label: string
  modelValue?: string
  editable: boolean
  fieldSize?: TextAreaInputAttributes["fieldSize"]
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: undefined,
  fieldSize: "medium",
})

defineEmits<{
  "update:modelValue": [value: string]
}>()
</script>

<template>
  <div class="flex flex-col">
    <label class="ds-label-02-reg mb-4" :for="props.id">{{
      props.label
    }}</label>

    <TextEditor
      :id="props.id"
      :aria-label="props.label"
      class="shadow-blue focus-within:shadow-focus hover:shadow-hover"
      :editable="editable"
      :field-size="props.fieldSize"
      :value="props.modelValue"
      @update-value="$emit('update:modelValue', $event)"
    />
  </div>
</template>
