<script lang="ts" setup>
import { Bold } from "@tiptap/extension-bold"
import { BulletList } from "@tiptap/extension-bullet-list"
import { Color } from "@tiptap/extension-color"
import { Document } from "@tiptap/extension-document"
import { Italic } from "@tiptap/extension-italic"
import { ListItem } from "@tiptap/extension-list-item"
import { OrderedList } from "@tiptap/extension-ordered-list"
import { Strike } from "@tiptap/extension-strike"
import { Subscript } from "@tiptap/extension-subscript"
import { Superscript } from "@tiptap/extension-superscript"
import { Table } from "@tiptap/extension-table"
import { TableCell } from "@tiptap/extension-table-cell"
import { TableHeader } from "@tiptap/extension-table-header"
import { TableRow } from "@tiptap/extension-table-row"
import { Text } from "@tiptap/extension-text"
import { TextAlign } from "@tiptap/extension-text-align"
import { TextStyle } from "@tiptap/extension-text-style"
import { Underline } from "@tiptap/extension-underline"
import { EditorContent, Editor } from "@tiptap/vue-3"
import { watch, ref } from "vue"
import { BorderNumber } from "../editor/border-number"
import { FontSize } from "../editor/font-size"
import { CustomImage } from "../editor/image"
import { CustomParagraph } from "../editor/paragraph"
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
  ariaLabel: {
    type: String,
    required: false,
    default: null,
  },
})

const emit = defineEmits<{
  (e: "updateValue", newValue: string): void
}>()

const hasFocus = ref<boolean>(false)

const editor = new Editor({
  content: props.value,
  extensions: [
    Document,
    CustomParagraph,
    Text,
    BorderNumber,
    Bold,
    Color,
    FontSize,
    Italic,
    ListItem,
    BulletList,
    OrderedList,
    Underline,
    Strike,
    Subscript,
    Superscript,
    Table,
    TableCell,
    TableHeader,
    TableRow,
    TextStyle,
    TextAlign.configure({
      types: ["paragraph", "span"],
    }),
    CustomImage.configure({
      allowBase64: true,
      inline: true,
    }),
  ],
  onUpdate: () => {
    // outgoing changes
    emit("updateValue", editor.getHTML())
  },
  onFocus: () => (hasFocus.value = true),
  onBlur: () => (hasFocus.value = false),
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
  return props.editable && hasFocus.value
}

interface EditorBtn {
  type: string
  icon: string
}

const editorBtns: EditorBtn[] = [
  ["bold", "format_bold"],
  ["italic", "format_italic"],
  ["underline", "format_underlined"],
  ["strike", "strikethrough_s"],
  ["superscript", "superscript"],
  ["subscript", "subscript"],
].map((button) => {
  return {
    type: button[0],
    icon: button[1],
  }
})
</script>

<template>
  <v-container fluid>
    <v-row
      v-if="showButtons()"
      :aria-label="
        props.ariaLabel ? props.ariaLabel + ' Editor Button Leiste' : null
      "
    >
      <v-col v-for="(btn, index) in editorBtns" :key="index"
        ><v-icon
          class="editor-btn"
          :class="{ 'editor-btn__active': editor.isActive(btn.type) }"
          @click="editor.chain().focus().toggleMark(btn.type).run()"
          @mousedown.prevent=""
          >{{ btn.icon }}</v-icon
        >
      </v-col>
      <v-col>Heading</v-col>
      <v-col><v-icon>list</v-icon></v-col>

      <v-col><v-icon>open_in_full</v-icon></v-col>
      <v-col>Vergrößern</v-col>
    </v-row>
    <v-row v-if="showButtons()"><v-col></v-col></v-row>
    <v-divider v-if="showButtons()" color="black"></v-divider>
    <v-row>
      <v-col cols="12">
        <editor-content
          :aria-label="
            props.ariaLabel ? props.ariaLabel + ' Editor Feld' : null
          "
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

  &__100percent {
    height: 100%;
  }
}
.ProseMirror-focused {
  outline: 0;
}
.editor-btn {
  color: $black;
  &:hover {
    color: $text-tertiary;
    background-color: $button-tertiary-focus;
  }
  &__active {
    color: $white;
    background-color: $text-tertiary;
  }
}

.ProseMirror p {
  margin-bottom: 8pt;
}
.ProseMirror .clearfix::after {
  content: "";
  clear: both;
  display: table;
}
</style>
