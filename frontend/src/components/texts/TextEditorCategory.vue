<script lang="ts" setup>
import { Component, nextTick } from "vue"
import TextEditor from "../input/TextEditor.vue"
import CategoryWrapper from "@/components/CategoryWrapper.vue"
import { TextAreaInputAttributes } from "@/components/input/types"
import { useFeatureToggle } from "@/composables/useFeatureToggle"

interface Props {
  id: string
  label: string
  shouldShowButton: boolean
  modelValue?: string
  fieldSize?: TextAreaInputAttributes["fieldSize"]
  registerTextEditorRef: (key: string, el: Component | null) => void
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: undefined,
  fieldSize: "big",
})

defineEmits<{
  "update:modelValue": [value: string]
}>()

const textCheck = useFeatureToggle("neuris.text-check")

async function focusEditor() {
  await nextTick()
  const editorElement = document.getElementById(props.id)
    ?.firstElementChild as HTMLElement
  editorElement?.focus()
}
</script>

<template>
  <CategoryWrapper
    :label="label"
    :should-show-button="shouldShowButton"
    @toggled="focusEditor"
  >
    <div class="flex flex-col">
      <label class="ris-label2-regular mb-4" :for="id">{{ label }}</label>

      <TextEditor
        :id="id"
        :ref="(el) => registerTextEditorRef(id, el)"
        :aria-label="label"
        :category="props.id"
        class="shadow-blue focus-within:shadow-focus hover:shadow-hover"
        editable
        :field-size="fieldSize"
        :text-check="textCheck"
        :value="modelValue"
        @update-value="$emit('update:modelValue', $event)"
      />
    </div>
  </CategoryWrapper>
</template>
