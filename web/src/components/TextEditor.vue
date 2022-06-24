<script lang="ts" setup>
import { Document } from "@tiptap/extension-document"
import { Paragraph } from "@tiptap/extension-paragraph"
import { Text } from "@tiptap/extension-text"
import { EditorContent, Editor } from "@tiptap/vue-3"
import { watch } from "vue"
import {
  DocUnitParagraphExtension,
  Randnummer,
} from "../editor/docUnitExtension"
import { FieldSize } from "@/domain/FieldSize"

const props = defineProps({
  value: {
    type: String,
    required: false,
    default: undefined,
  },
  fieldSize: {
    type: String,
    required: false,
    default: "small" as FieldSize,
  },
  editable: {
    type: Boolean,
    required: false,
    default: true,
  },
})

const emit = defineEmits<{
  (e: "updateValue", newValue: string): void
}>()

const editor = new Editor({
  content: props.value,
  extensions: [
    Document,
    Paragraph,
    Text,
    Randnummer,
    DocUnitParagraphExtension,
  ],
  onUpdate: () => {
    // outgoing changes
    emit("updateValue", editor.getHTML())
  },
  editable: props.editable,
})

watch(
  () => props.value,
  (value) => {
    if (!value || value === editor.getHTML()) {
      return
    }
    // incoming changes
    editor.commands.setContent(value, false)
  }
)

const showButtons = () => {
  return false
}
</script>

<template>
  <v-container fluid>
    <v-row v-if="showButtons()">
      <v-col cols="1"><v-icon>format_bold</v-icon></v-col>
      <v-col cols="1"><v-icon>format_italic</v-icon></v-col>
      <v-col cols="1"><v-icon>format_underlined</v-icon></v-col>
      <v-col cols="1"><v-icon>strikethrough_s</v-icon></v-col>
      <v-col cols="1">Heading</v-col>
      <v-col cols="1"><v-icon>list</v-icon></v-col>
      <v-col cols="3" />
      <v-col cols="1"><v-icon>open_in_full</v-icon></v-col>
      <v-col cols="1">Vergrößern</v-col>
    </v-row>
    <v-row v-if="showButtons()"><v-col></v-col></v-row>
    <v-divider v-if="showButtons()" color="black"></v-divider>
    <v-row>
      <v-col cols="12">
        <editor-content
          :editor="editor"
          :class="'ProseMirror__' + props.fieldSize"
        />
      </v-col>
    </v-row>
  </v-container>
</template>

<style lang="scss">
.ProseMirror {
  // background: #eee;
  color: #000;
  padding: 0.75rem 1rem;
  border-radius: 0.5rem;
  overflow-y: auto;
  height: 100%;

  &__small {
    height: 60px;
  }

  &__medium {
    height: 120px;
  }

  &__large {
    height: 320px;
  }

  &__max {
    height: 640px; // ? TODO
  }
}
</style>
