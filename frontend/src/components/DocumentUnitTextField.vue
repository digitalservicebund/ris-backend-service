<script lang="ts" setup>
import TextEditor from "../components/input/TextEditor.vue"
import { TextAreaInputAttributes } from "@/components/input/types"

interface Props {
  name?: string
  label?: string
  value?: string
  fieldSize: TextAreaInputAttributes["fieldSize"]
}

const props = withDefaults(defineProps<Props>(), {
  name: undefined,
  label: undefined,
  value: undefined,
  fieldSize: "medium",
})

const emit = defineEmits<{
  "update-value": [value: string]
}>()
</script>

<template>
  <div class="flex flex-col">
    <label class="ds-label-02-reg mb-4" :for="props.name">{{
      props.label
    }}</label>

    <TextEditor
      :id="props.name"
      :aria-label="props.label"
      class="shadow-blue focus-within:shadow-focus hover:shadow-hover"
      editable
      :field-size="props.fieldSize"
      :value="props.value"
      @update-value="emit('update-value', $event)"
    />
  </div>
</template>
