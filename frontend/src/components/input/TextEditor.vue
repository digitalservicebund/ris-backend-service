<script lang="ts" setup>
import { Bold } from "@tiptap/extension-bold"
import { Color } from "@tiptap/extension-color"
import { Document } from "@tiptap/extension-document"
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
import {
  BorderNumber,
  BorderNumberContent,
  BorderNumberNumber,
} from "../../editor/borderNumber"
import { BorderNumberLink } from "../../editor/borderNumberLink"
import { CustomBulletList } from "../../editor/bulletList"
import { FontSize } from "../../editor/fontSize"
import { CustomImage } from "../../editor/image"
import { CustomListItem } from "../../editor/listItem"
import { CustomOrderedList } from "../../editor/orderedList"
import { CustomParagraph } from "../../editor/paragraph"
import { CustomSuperscript, CustomSubscript } from "../../editor/scriptText"
import { TableStyle } from "../../editor/tableStyle"
import TextEditorButton, {
  EditorButton,
} from "@/components/input/TextEditorButton.vue"
import { TextAreaInputAttributes } from "@/components/input/types"
import { useCollapsingMenuBar } from "@/composables/useCollapsingMenuBar"
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

interface Props {
  value?: string
  editable?: boolean
  ariaLabel?: string
  fieldSize?: TextAreaInputAttributes["fieldSize"]
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  editable: false,
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
      style:
        "height: 100%; overflow-y: auto; padding: 0.75rem 1rem; outline: 0",
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
  buttons.value.map((button) => ({
    ...button,
    isActive:
      button.group == "alignment"
        ? editor.isActive({ textAlign: button.type })
        : editor.isActive(button.type),
  })),
)
const buttonSize = 48
const containerWidth = ref()
const maxButtonEntries = computed(() =>
  Math.floor((containerWidth.value - 100) / buttonSize),
)

const editorExpanded = ref(false)
const editorSize = computed(() => {
  return editorExpanded.value
    ? "h-640"
    : props.fieldSize == "max"
      ? "h-full"
      : props.fieldSize == "big"
        ? "h-320"
        : props.fieldSize == "medium"
          ? "h-160"
          : props.fieldSize == "small"
            ? "h-96"
            : undefined
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
    editor.commands.setContent(value, false)
  },
)

const showButtons = computed(() => props.editable && hasFocus.value)

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
  <div class="editor bg-white" fluid>
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

<style lang="scss" module>
ol {
  padding: revert;
  margin: auto;
  list-style: auto;
}
</style>
