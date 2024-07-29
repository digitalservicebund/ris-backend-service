<script lang="ts" setup>
import { commands } from "@guardian/prosemirror-invisibles"
import { Blockquote } from "@tiptap/extension-blockquote"
import { Bold } from "@tiptap/extension-bold"
import { Color } from "@tiptap/extension-color"
import { Document } from "@tiptap/extension-document"
import { HardBreak } from "@tiptap/extension-hard-break"
import { History } from "@tiptap/extension-history"
import { Italic } from "@tiptap/extension-italic"
import { Strike } from "@tiptap/extension-strike"
import { Table } from "@tiptap/extension-table"
import { TableCell } from "@tiptap/extension-table-cell"
import { TableHeader } from "@tiptap/extension-table-header"
import { TableRow } from "@tiptap/extension-table-row"
import { Text } from "@tiptap/extension-text"
import { TextAlign } from "@tiptap/extension-text-align"
import { TextStyle } from "@tiptap/extension-text-style"
import { Underline } from "@tiptap/extension-underline"
import { Editor, EditorContent } from "@tiptap/vue-3"
import { computed, onMounted, ref, watch } from "vue"
import TextEditorMenu from "@/components/input/TextEditorMenu.vue"
import { TextAreaInputAttributes } from "@/components/input/types"
import {
  BorderNumber,
  BorderNumberContent,
  BorderNumberNumber,
} from "@/editor/borderNumber"
import { BorderNumberLink } from "@/editor/borderNumberLink"
import { CustomBulletList } from "@/editor/bulletList"
import { FontSize } from "@/editor/fontSize"
import { CustomImage } from "@/editor/image"
import { Indent } from "@/editor/indent"
import { InvisibleCharacters } from "@/editor/invisibleCharacters"
import { CustomListItem } from "@/editor/listItem"
import { CustomOrderedList } from "@/editor/orderedList"
import { CustomParagraph } from "@/editor/paragraph"
import { CustomSubscript, CustomSuperscript } from "@/editor/scriptText"
import { TableStyle } from "@/editor/tableStyle"

interface Props {
  value?: string
  editable?: boolean
  preview?: boolean
  ariaLabel?: string
  fieldSize?: TextAreaInputAttributes["fieldSize"]
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  editable: false,
  preview: false,
  ariaLabel: "Editor Feld",
  fieldSize: "medium",
})

const emit = defineEmits<{
  updateValue: [newValue: string]
}>()

const editorElement = ref<HTMLElement>()
const hasFocus = ref(false)
const isHovered = ref(false)

const editor = new Editor({
  editorProps: {
    attributes: {
      tabindex: "0",
      style: props.preview
        ? "height: 100%; overflow-y: auto; outline: 0"
        : "height: 100%; overflow-y: auto; padding: 0.75rem 1rem; outline: 0",
    },
  },
  content: props.value,
  extensions: [
    Document,
    CustomParagraph,
    Text,
    BorderNumber,
    BorderNumberNumber,
    BorderNumberContent,
    BorderNumberLink,
    Bold,
    Color,
    FontSize,
    Italic,
    CustomListItem,
    CustomBulletList,
    CustomOrderedList,
    Underline,
    Strike,
    CustomSubscript,
    CustomSuperscript,
    Table,
    TableCell,
    TableHeader,
    TableRow,
    TableStyle,
    TextStyle,
    HardBreak,
    InvisibleCharacters,
    TextAlign.configure({
      types: ["paragraph", "span"],
    }),
    CustomImage.configure({
      allowBase64: true,
      inline: true,
      HTMLAttributes: {
        class: "inline align-baseline",
      },
    }),
    History.configure({
      depth: 100,
    }),
    Blockquote,
    Indent.configure({
      names: ["listItem", "paragraph"],
    }),
  ],
  onUpdate: () => {
    emit("updateValue", editor.getHTML())
  },
  onFocus: () => (hasFocus.value = true),
  // onBlur: () => (hasFocus.value = false),
  editable: props.editable,
  parseOptions: {
    preserveWhitespace: "full",
  },
})

const containerWidth = ref<number>(0)

const editorExpanded = ref(false)
const editorSize = computed(() => {
  if (editorExpanded.value) return "h-640"

  switch (props.fieldSize) {
    case "max":
      return "h-full"
    case "big":
      return "h-320"
    case "medium":
      return "h-160"
    case "small":
      return "h-96"
  }
  return undefined
})

watch(
  () => props.value,
  (value) => {
    if (!value || value === editor.getHTML()) {
      return
    }
    // incoming changes
    // the cursor should not jump to the end of the content but stay where it is
    const cursorPos = editor.state.selection.anchor
    editor.commands.setContent(value, false)
    editor.commands.setTextSelection(cursorPos)
  },
)

const buttonsDisabled = computed(
  () => !(props.editable && (hasFocus.value || isHovered.value)),
)

watch(
  () => hasFocus.value,
  () => {
    // When the TextEditor is editable and has focus, the invisibleCharacters should be visible
    commands.setActiveState(props.editable && hasFocus.value)(
      editor.state,
      editor.view.dispatch,
    )
  },
  { immediate: true },
)

const ariaLabel = props.ariaLabel ? props.ariaLabel : null

onMounted(() => {
  const editorContainer = document.querySelector(".editor")
  if (editorContainer != null) resizeObserver.observe(editorContainer)
})

const resizeObserver = new ResizeObserver((entries) => {
  for (const entry of entries) {
    containerWidth.value = entry.contentRect.width
  }
})
</script>

<template>
  <!-- eslint-disable vuejs-accessibility/no-static-element-interactions -->
  <div
    id="text-editor"
    ref="editorElement"
    class="editor bg-white"
    fluid
    @blur="hasFocus = false"
    @focusin="hasFocus = true"
    @focusout="!editorElement?.matches(':focus-within') && (hasFocus = false)"
    @mouseenter="isHovered = true"
    @mouseleave="isHovered = false"
  >
    <TextEditorMenu
      v-if="editable"
      :aria-label="props.ariaLabel"
      :buttons-disabled="buttonsDisabled"
      :container-width="containerWidth"
      :editor="editor"
      :editor-expanded="editorExpanded"
      @on-editor-expanded-changed="
        (isExpanded) => (editorExpanded = isExpanded)
      "
    />
    <hr v-if="editable" class="ml-8 mr-8 border-blue-300" />
    <div>
      <EditorContent
        :class="editorSize"
        :data-testid="ariaLabel"
        :editor="editor"
      />
    </div>
  </div>
</template>
