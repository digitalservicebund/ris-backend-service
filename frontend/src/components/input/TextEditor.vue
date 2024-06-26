<script lang="ts" setup>
import { commands, selectActiveState } from "@guardian/prosemirror-invisibles"
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
import { EditorContent, Editor } from "@tiptap/vue-3"
import { computed, watch, ref, onMounted } from "vue"
import TextEditorButton, {
  EditorButton,
} from "@/components/input/TextEditorButton.vue"
import { TextAreaInputAttributes } from "@/components/input/types"
import { useCollapsingMenuBar } from "@/composables/useCollapsingMenuBar"
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
import { CustomSuperscript, CustomSubscript } from "@/editor/scriptText"
import { TableStyle } from "@/editor/tableStyle"
import IconExpand from "~icons/ic/baseline-expand"
import IconAlignJustify from "~icons/ic/baseline-format-align-justify"
import IconAlignRight from "~icons/ic/baseline-format-align-right"
import IconBold from "~icons/ic/baseline-format-bold"
import IconItalic from "~icons/ic/baseline-format-italic"
import IconStrikethrough from "~icons/ic/baseline-format-strikethrough"
import IconUnderline from "~icons/ic/baseline-format-underlined"
import IconRedo from "~icons/ic/baseline-redo"
import IconSubscript from "~icons/ic/baseline-subscript"
import IconSuperscript from "~icons/ic/baseline-superscript"
import IconUndo from "~icons/ic/baseline-undo"
import IconAlignCenter from "~icons/ic/outline-format-align-center"
import IconAlignLeft from "~icons/ic/outline-format-align-left"
import IconBlockquote from "~icons/ic/sharp-format-quote"
import IndentDecrease from "~icons/material-symbols/format-indent-decrease"
import IndentIncrease from "~icons/material-symbols/format-indent-increase"
import IconParagraph from "~icons/material-symbols/format-paragraph"

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

const hasFocus = ref(false)

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
  onBlur: () => (hasFocus.value = false),
  editable: props.editable,
  parseOptions: {
    preserveWhitespace: "full",
  },
})

const buttons = computed(() => [
  {
    type: "undo",
    icon: IconUndo,
    ariaLabel: "undo",
    group: "arrow",
    isCollapsable: false,
    callback: () => editor.chain().focus().undo().run(),
  },
  {
    type: "redo",
    icon: IconRedo,
    ariaLabel: "redo",
    group: "arrow",
    isCollapsable: false,
    callback: () => editor.chain().focus().redo().run(),
  },
  {
    type: "bold",
    icon: IconBold,
    ariaLabel: "bold",
    group: "format",
    isCollapsable: false,
    callback: () => editor.chain().focus().toggleMark("bold").run(),
  },
  {
    type: "italic",
    icon: IconItalic,
    ariaLabel: "italic",
    group: "format",
    isCollapsable: false,
    callback: () => editor.chain().focus().toggleMark("italic").run(),
  },
  {
    type: "underline",
    icon: IconUnderline,
    ariaLabel: "underline",
    group: "format",
    isCollapsable: false,
    callback: () => editor.chain().focus().toggleMark("underline").run(),
  },
  {
    type: "strike",
    icon: IconStrikethrough,
    ariaLabel: "strike",
    group: "format",
    isCollapsable: false,
    callback: () => editor.chain().focus().toggleMark("strike").run(),
  },
  {
    type: "left",
    icon: IconAlignLeft,
    ariaLabel: "left",
    group: "alignment",
    isCollapsable: true,
    callback: () => editor.chain().focus().setTextAlign("left").run(),
  },
  {
    type: "center",
    icon: IconAlignCenter,
    ariaLabel: "center",
    group: "alignment",
    isCollapsable: true,
    callback: () => editor.chain().focus().setTextAlign("center").run(),
  },
  {
    type: "right",
    icon: IconAlignRight,
    ariaLabel: "right",
    group: "alignment",
    isCollapsable: true,
    callback: () => editor.chain().focus().setTextAlign("right").run(),
  },
  {
    type: "justify",
    icon: IconAlignJustify,
    ariaLabel: "justify",
    group: "alignment",
    isCollapsable: true,
    callback: () => editor.chain().focus().setTextAlign("justify").run(),
  },
  {
    type: "superscript",
    icon: IconSuperscript,
    ariaLabel: "superscript",
    group: "vertical-alignment",
    isCollapsable: false,
    callback: () => editor.chain().focus().toggleMark("superscript").run(),
  },
  {
    type: "subscript",
    icon: IconSubscript,
    ariaLabel: "subscript",
    group: "vertical-alignment",
    isCollapsable: false,
    callback: () => editor.chain().focus().toggleMark("subscript").run(),
  },
  {
    type: "outdent",
    icon: IndentDecrease,
    ariaLabel: "outdent",
    group: "indent",
    isCollapsable: false,
    callback: () => editor.chain().focus().outdent().run(),
  },
  {
    type: "indent",
    icon: IndentIncrease,
    ariaLabel: "indent",
    group: "indent",
    isCollapsable: false,
    callback: () => editor.chain().focus().indent().run(),
  },
  {
    type: "invisible-characters",
    icon: IconParagraph,
    ariaLabel: "invisible-characters",
    group: "view",
    isCollapsable: false,
    callback: () =>
      commands.toggleActiveState()(editor.state, editor.view.dispatch),
  },
  {
    type: "blockquote",
    icon: IconBlockquote,
    ariaLabel: "blockquote",
    group: "format",
    isCollapsable: false,
    callback: () => editor.chain().focus().toggleBlockquote().run(),
  },
])

const fixButtons = [
  {
    type: "",
    icon: IconExpand,
    ariaLabel: "fullview",
    callback: () => (editorExpanded.value = !editorExpanded.value),
  },
]

const editorButtons = computed(() =>
  buttons.value.map((button) => {
    let isActive

    if (button.group === "alignment") {
      isActive = editor.isActive({ textAlign: button.type })
    } else if (button.ariaLabel === "invisible-characters") {
      isActive = selectActiveState(editor.view.state)
    } else {
      isActive = editor.isActive(button.type)
    }

    return {
      ...button,
      isActive,
    }
  }),
)
const buttonSize = 48
const containerWidth = ref()
const maxButtonEntries = computed(() =>
  Math.floor((containerWidth.value - 100) / buttonSize),
)

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
const { collapsedButtons } = useCollapsingMenuBar(
  editorButtons,
  maxButtonEntries,
)

function handleButtonClick(button: EditorButton) {
  if (button.callback) button.callback()
}

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

const showButtons = computed(() => props.editable && hasFocus.value)

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
  <div id="text-editor" class="editor bg-white" fluid>
    <div v-if="showButtons">
      <div
        :aria-label="ariaLabel + ' Button Leiste'"
        class="pa-1 flex flex-row flex-wrap justify-between"
      >
        <div class="flex flex-row">
          <TextEditorButton
            v-for="(button, index) in collapsedButtons"
            :key="index"
            v-bind="button"
            @toggle="handleButtonClick"
          />
        </div>
        <div class="flex flex-row">
          <TextEditorButton
            v-for="(button, index) in fixButtons"
            :key="index"
            v-bind="button"
            @toggle="handleButtonClick"
          />
        </div>
      </div>
      <hr />
    </div>
    <div>
      <EditorContent
        :class="editorSize"
        :data-testid="ariaLabel"
        :editor="editor"
      />
    </div>
  </div>
</template>
