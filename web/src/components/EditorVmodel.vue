<script lang="ts" setup>
import { Document } from "@tiptap/extension-document"
import { Paragraph } from "@tiptap/extension-paragraph"
import { Text } from "@tiptap/extension-text"
import { EditorContent, Editor } from "@tiptap/vue-3"
import { PropType, watch } from "vue"
import { FieldSize } from "../types/FieldSize"

const props = defineProps({
  modelValue: {
    type: String,
    required: true,
    default: "",
  },
  fieldSize: {
    type: Object as PropType<FieldSize>,
    required: false,
    default: "small" as FieldSize,
  },
})

const emit = defineEmits(["update:modelValue"])

const editor = new Editor({
  content: props.modelValue,
  extensions: [Document, Paragraph, Text],
  onUpdate: () => {
    emit("update:modelValue", editor.getHTML())
  },
})

watch(
  () => props.modelValue,
  (value) => {
    if (!value || value === editor.getHTML()) {
      return
    }
    editor.commands.setContent(value, false)
  }
)
</script>

<template>
  <editor-content :editor="editor" :class="'ProseMirror__' + props.fieldSize" />
</template>

<style lang="scss">
.ProseMirror {
  background: #eee;
  color: #000;
  padding: 0.75rem 1rem;
  border-radius: 0.5rem;

  &__small {
    height: 60px;
  }

  &__medium {
    height: 120px;
  }

  &__large {
    height: 320px;
  }
}
</style>
