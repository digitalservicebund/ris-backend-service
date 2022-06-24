<script lang="ts" setup>
import { Bold } from "@tiptap/extension-bold"
import { Document } from "@tiptap/extension-document"
import { Italic } from "@tiptap/extension-italic"
import { Paragraph } from "@tiptap/extension-paragraph"
import { Strike } from "@tiptap/extension-strike"
import { Text } from "@tiptap/extension-text"
import { Underline } from "@tiptap/extension-underline"
import { EditorContent, Editor } from "@tiptap/vue-3"
import { PropType, watch } from "vue"
import {
  DocUnitParagraphExtension,
  Randnummer,
} from "../editor/docUnitExtension"
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
  editable: {
    type: Boolean,
    required: false,
    default: true,
  },
})

const emit = defineEmits(["update:modelValue"])

const editor = new Editor({
  content: props.modelValue,
  extensions: [
    Document,
    Paragraph,
    Text,
    Randnummer,
    DocUnitParagraphExtension,
    Bold,
    Italic,
    Underline,
    Strike,
  ],
  onUpdate: () => {
    // outgoing changes
    emit("update:modelValue", editor.getHTML())
  },
  editable: props.editable,
})

watch(
  () => props.modelValue,
  (value) => {
    if (!value || value === editor.getHTML()) {
      return
    }
    // incoming changes
    editor.commands.setContent(value, false)
  }
)

const showButtons = () => {
  return props.editable // && in focus TODO
}
</script>

<template>
  <v-container fluid>
    <v-row v-if="showButtons()">
      <v-col cols="1"
        ><v-icon
          :class="{ 'editor-btn-active': editor.isActive('bold') }"
          @click="editor.chain().focus().toggleBold().run()"
          >format_bold</v-icon
        ></v-col
      >
      <v-col cols="1"
        ><v-icon
          :class="{ 'editor-btn-active': editor.isActive('italic') }"
          @click="editor.chain().focus().toggleItalic().run()"
          >format_italic</v-icon
        ></v-col
      >
      <v-col cols="1"
        ><v-icon
          :class="{ 'editor-btn-active': editor.isActive('underline') }"
          @click="editor.chain().focus().toggleUnderline().run()"
          >format_underlined</v-icon
        ></v-col
      >
      <v-col cols="1"
        ><v-icon
          :class="{ 'editor-btn-active': editor.isActive('strike') }"
          @click="editor.chain().focus().toggleStrike().run()"
          >strikethrough_s</v-icon
        ></v-col
      >
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
.editor-btn-active {
  color: $blue700;
}
</style>
