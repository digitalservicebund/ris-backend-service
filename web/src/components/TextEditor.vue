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
} from "../editor/borderNumber"
import { CustomBulletList } from "../editor/bulletList"
import { FontSize } from "../editor/fontSize"
import { CustomImage } from "../editor/image"
import { CustomListItem } from "../editor/listItem"
import { CustomOrderedList } from "../editor/orderedList"
import { CustomParagraph } from "../editor/paragraph"
import { CustomSuperscript, CustomSubscript } from "../editor/scriptText"
import { TableStyle } from "../editor/tableStyle"
import TextEditorButton from "@/components/TextEditorButton.vue"
import { useCollapsingMenuBar } from "@/composables/useCollapsingMenuBar"
import { FieldSize } from "@/domain/FieldSize"

interface Props {
  value?: string
  fieldSize?: FieldSize
  editable?: boolean
  ariaLabel?: string
}

interface MenuButton {
  type: string
  icon: string
  ariaLabel: string
  childButtons?: MenuButton[]
  isLast?: boolean
  isActive?: boolean
  isSecondRow?: boolean
  isCollapsable?: boolean
  callback?: () => void
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  fieldSize: "small",
  editable: false,
  ariaLabel: "",
})

const emit = defineEmits<{
  (e: "updateValue", newValue: string): void
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
    icon: "undo",
    ariaLabel: "undo",
    group: "arrow",
    isCollapsable: false,
    callback: () => editor.chain().focus().undo().run(),
  },
  {
    type: "redo",
    icon: "redo",
    ariaLabel: "redo",
    group: "arrow",
    isCollapsable: false,
    callback: () => editor.chain().focus().redo().run(),
  },
  {
    type: "bold",
    icon: "format_bold",
    ariaLabel: "bold",
    group: "format",
    isCollapsable: false,
    callback: () => editor.chain().focus().toggleMark("bold").run(),
  },
  {
    type: "italic",
    icon: "format_italic",
    ariaLabel: "italic",
    group: "format",
    isCollapsable: false,
    callback: () => editor.chain().focus().toggleMark("italic").run(),
  },
  {
    type: "underline",
    icon: "format_underlined",
    ariaLabel: "underline",
    group: "format",
    isCollapsable: false,
    callback: () => editor.chain().focus().toggleMark("underline").run(),
  },
  {
    type: "strike",
    icon: "strikethrough_s",
    ariaLabel: "strike",
    group: "format",
    isCollapsable: false,
    callback: () => editor.chain().focus().toggleMark("strike").run(),
  },
  {
    type: "left",
    icon: "format_align_left",
    ariaLabel: "left",
    group: "alignment",
    isCollapsable: true,
    isSecondRow: true,
    callback: () => editor.chain().focus().setTextAlign("left").run(),
  },
  {
    type: "center",
    icon: "format_align_center",
    ariaLabel: "center",
    group: "alignment",
    isCollapsable: true,
    isSecondRow: true,
    callback: () => editor.chain().focus().setTextAlign("center").run(),
  },
  {
    type: "right",
    icon: "format_align_right",
    ariaLabel: "right",
    group: "alignment",
    isCollapsable: true,
    isSecondRow: true,
    callback: () => editor.chain().focus().setTextAlign("right").run(),
  },
  {
    type: "justify",
    icon: "format_align_justify",
    ariaLabel: "justify",
    group: "alignment",
    isCollapsable: true,
    isSecondRow: true,
    callback: () => editor.chain().focus().setTextAlign("justify").run(),
  },
  {
    type: "superscript",
    icon: "superscript",
    ariaLabel: "superscript",
    group: "vertical-alignment",
    isCollapsable: false,
    isSecondRow: true,
    callback: () => editor.chain().focus().toggleMark("superscript").run(),
  },
  {
    type: "subscript",
    icon: "subscript",
    ariaLabel: "subscript",
    group: "vertical-alignment",
    isCollapsable: false,
    isSecondRow: true,
    callback: () => editor.chain().focus().toggleMark("subscript").run(),
  },
  {
    type: "numbered-list",
    icon: "format_list_numbered",
    ariaLabel: "numbered-list",
    group: "list",
    isCollapsable: true,
    isSecondRow: true,
  },
  {
    type: "bullet-list",
    icon: "format_list_bulleted",
    ariaLabel: "bullet-list",
    group: "list",
    isCollapsable: true,
    isSecondRow: true,
  },
  {
    type: "vertical_split",
    icon: "vertical_split",
    ariaLabel: "vertical_split",
    group: "split",
    isCollapsable: true,
    isSecondRow: true,
  },
  {
    type: "vertical_split",
    icon: "vertical_split",
    ariaLabel: "vertical_split",
    group: "split",
    isCollapsable: true,
    isSecondRow: true,
  },
  {
    type: "table",
    icon: "table_chart",
    ariaLabel: "table",
    isCollapsable: false,
    isSecondRow: true,
  },
])

const fixButtons = [
  {
    type: "",
    icon: "123",
    ariaLabel: "margins",
  },
  {
    type: "",
    icon: "open_in_full",
    ariaLabel: "fullview",
  },
]

const editorButtons = computed(() =>
  buttons.value.map((button) => ({
    ...button,
    isActive:
      button.group == "alignment"
        ? editor.isActive({ textAlign: button.type })
        : editor.isActive(button.type),
  }))
)
const buttonSize = 48 //px
const containerWidth = ref()
const maxButtonEntries = computed(() =>
  Math.floor((containerWidth.value - 100) / buttonSize)
)
const { collapsedButtons } = useCollapsingMenuBar(
  editorButtons,
  maxButtonEntries
)
const showSecondRow = ref(false)

const container = ref()

function handleButtonClick(button: MenuButton) {
  if (button.type == "more") showSecondRow.value = !showSecondRow.value
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
  }
)

const showButtons = () => {
  const showButtons = props.editable && hasFocus.value
  return showButtons
}

const ariaLabel = props.ariaLabel ? props.ariaLabel + " Editor Feld" : null

onMounted(() => {
  const editorContainer = document.querySelector(".editor")
  if (editorContainer != null) resizeObserver.observe(editorContainer)
})

const resizeObserver = new ResizeObserver((entries) => {
  showSecondRow.value = false
  for (const entry of entries) {
    containerWidth.value = entry.contentRect.width
  }
})
</script>

<template>
  <div ref="container" class="bg-white editor" fluid>
    <div v-if="showButtons()">
      <div
        :aria-label="props.ariaLabel + ' Editor Button Leiste'"
        class="flex flex-row flex-wrap justify-between pa-1"
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
    <div v-if="showButtons() && showSecondRow">
      <div
        :aria-label="ariaLabel + ' Editor Button Leiste'"
        class="flex flex-row flex-wrap pa-1"
      >
        <TextEditorButton
          v-for="(button, index) in collapsedButtons[
            collapsedButtons.length - 1
          ].childButtons"
          :key="index"
          v-bind="button"
          @toggle="handleButtonClick"
        />
      </div>
      <hr />
    </div>
    <div>
      <EditorContent
        :aria-label="ariaLabel"
        class="p-[2rem]"
        :class="'editor-content editor-content--' + fieldSize"
        :editor="editor"
      />
    </div>
  </div>
</template>
