<script lang="ts" setup>
import { Bold } from "@tiptap/extension-bold"
import { Color } from "@tiptap/extension-color"
import { Document } from "@tiptap/extension-document"
import { History } from "@tiptap/extension-history"
import { Italic } from "@tiptap/extension-italic"
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
import { computed, watch, ref, onMounted } from "vue"
import { onBeforeRouteUpdate } from "vue-router"
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

interface Button {
  type: string
  icon: string
  ariaLabel: string
  childButtons?: Button[]
  isLast?: boolean
  isActive?: boolean
  isSecondRow?: boolean
  isCollapsable?: boolean
  callback?: string
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
    Subscript,
    Superscript,
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
    }),
    History.configure({
      depth: 10,
    }),
  ],
  onUpdate: () => {
    // outgoing changes
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
    callback: "undo",
  },
  {
    type: "redo",
    icon: "redo",
    ariaLabel: "redo",
    group: "arrow",
    isCollapsable: false,
    callback: "redo",
  },
  {
    type: "bold",
    icon: "format_bold",
    ariaLabel: "bold",
    group: "format",
    isCollapsable: false,
    callback: "toggle",
  },
  {
    type: "italic",
    icon: "format_italic",
    ariaLabel: "italic",
    group: "format",
    isCollapsable: false,
    callback: "toggle",
  },
  {
    type: "underline",
    icon: "format_underlined",
    ariaLabel: "underline",
    group: "format",
    isCollapsable: false,
    callback: "toggle",
  },
  {
    type: "strike",
    icon: "strikethrough_s",
    ariaLabel: "strike",
    group: "format",
    isCollapsable: false,
    callback: "toggle",
  },

  {
    type: "left",
    icon: "format_align_left",
    ariaLabel: "left",
    group: "alignment",
    isCollapsable: true,
    isSecondRow: true,
    callback: "textAlign",
  },
  {
    type: "center",
    icon: "format_align_center",
    ariaLabel: "center",
    group: "alignment",
    isCollapsable: true,
    isSecondRow: true,
    callback: "textAlign",
  },
  {
    type: "right",
    icon: "format_align_right",
    ariaLabel: "right",
    group: "alignment",
    isCollapsable: true,
    isSecondRow: true,
    callback: "textAlign",
  },
  {
    type: "justify",
    icon: "format_align_justify",
    ariaLabel: "justify",
    group: "alignment",
    isCollapsable: true,
    isSecondRow: true,
    callback: "textAlign",
  },
  {
    type: "superscript",
    icon: "superscript",
    ariaLabel: "superscript",
    group: "vertical-alignment",
    isCollapsable: false,
    isSecondRow: true,
    callback: "toggle",
  },
  {
    type: "subscript",
    icon: "subscript",
    ariaLabel: "subscript",
    group: "vertical-alignment",
    isCollapsable: false,
    isSecondRow: true,
    callback: "toggle",
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
    callback: "toggle",
  },
  {
    type: "",
    icon: "open_in_full",
    ariaLabel: "fullview",
    callback: "toggle",
  },
]

const editorButtons = computed(() =>
  buttons.value.map((button) => ({
    ...button,
    isActive: editor.isActive(button.type),
  }))
)
const buttonSize = 47 //px
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

function onResize() {
  showSecondRow.value = false
  containerWidth.value = container.value.getBoundingClientRect().width
}

function handleButtonClick(button: Button) {
  if (button.callback === "toggle") {
    editor.chain().focus().toggleMark(button.type).run()
  } else if (button.callback === "textAlign") {
    editor.chain().focus().setTextAlign(button.type).run()
  } else if (button.callback === "showMore") {
    showSecondRow.value = !showSecondRow.value
  } else if (button.callback === "undo") {
    editor.chain().focus().undo().run()
  } else if (button.callback === "redo") {
    editor.chain().focus().redo().run()
  }
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
  // const showButtons = props.editable
  return showButtons
}

const ariaLabel = props.ariaLabel ? props.ariaLabel + " Editor Feld" : null
const alignText = [
  { style: "text-align: right", align: "right" },
  { style: "text-align: left", align: "left" },
  { style: "text-align: center", align: "center" },
  { style: "text-align: justify", align: "justify" },
]
onMounted(() => {
  onResize()
  const editorContainer = document.querySelector(`[aria-label="${ariaLabel}"]`)
  if (editorContainer) {
    editorContainer.addEventListener("paste", (e) => {
      const clipboardCoppiedData =
        (e as ClipboardEvent).clipboardData?.getData("text/html") ?? ""
      if (clipboardCoppiedData) {
        const parser = new DOMParser()
        const pastedContent = parser.parseFromString(
          clipboardCoppiedData,
          "text/html"
        ).body.innerHTML
        const applyStyle = alignText.filter((alignTextElement) =>
          pastedContent.includes(alignTextElement.style)
        )
        if (applyStyle.length === 1) {
          editor.chain().focus().setTextAlign(applyStyle[0].align).run()
          emit("updateValue", editor.getHTML())
        }
      }
    })
  }
})

onBeforeRouteUpdate(async () => {
  onResize()
})
</script>

<template>
  <div
    id="container"
    ref="container"
    v-resize="onResize"
    class="bg-white"
    fluid
  >
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
          v-for="(button, index) in collapsedButtons[6].childButtons"
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
        :class="'editor-content editor-content--' + fieldSize"
        :editor="editor"
      />
    </div>
  </div>
</template>
