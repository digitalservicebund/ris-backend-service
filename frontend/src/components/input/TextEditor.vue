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
import MaterialSymbolsDeleteSweepOutline from "~icons/ic/sharp-delete-sweep"
import IconExpand from "~icons/ic/sharp-expand"
import IconAlignCenter from "~icons/ic/sharp-format-align-center"
import IconAlignJustify from "~icons/ic/sharp-format-align-justify"
import IconAlignLeft from "~icons/ic/sharp-format-align-left"
import IconAlignRight from "~icons/ic/sharp-format-align-right"
import IconBold from "~icons/ic/sharp-format-bold"
import IndentDecrease from "~icons/ic/sharp-format-indent-decrease"
import IndentIncrease from "~icons/ic/sharp-format-indent-increase"
import IconItalic from "~icons/ic/sharp-format-italic"
import IconUnorderedList from "~icons/ic/sharp-format-list-bulleted"
import IconOrderedList from "~icons/ic/sharp-format-list-numbered"
import IconBlockquote from "~icons/ic/sharp-format-quote"
import IconStrikethrough from "~icons/ic/sharp-format-strikethrough"
import IconUnderline from "~icons/ic/sharp-format-underlined"
import IconRedo from "~icons/ic/sharp-redo"
import IconSubscript from "~icons/ic/sharp-subscript"
import IconSuperscript from "~icons/ic/sharp-superscript"
import IconUndo from "~icons/ic/sharp-undo"
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

const buttons = computed(() => [
  {
    type: "expand",
    icon: IconExpand,
    ariaLabel: "fullview",
    group: "display",
    isCollapsable: false,
    callback: () => (editorExpanded.value = !editorExpanded.value),
  },
  {
    type: "invisible-characters",
    icon: IconParagraph,
    ariaLabel: "invisible-characters",
    group: "display",
    isCollapsable: false,
    callback: () =>
      commands.toggleActiveState()(editor.state, editor.view.dispatch),
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
    type: "superscript",
    icon: IconSuperscript,
    ariaLabel: "superscript",
    group: "format",
    isCollapsable: false,
    callback: () => editor.chain().focus().toggleMark("superscript").run(),
  },
  {
    type: "subscript",
    icon: IconSubscript,
    ariaLabel: "subscript",
    group: "format",
    isCollapsable: false,
    callback: () => editor.chain().focus().toggleMark("subscript").run(),
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
    type: "bulletList",
    icon: IconUnorderedList,
    ariaLabel: "bulletList",
    group: "indent",
    isCollapsable: false,
    callback: () => editor.chain().focus().toggleBulletList().run(),
  },
  {
    type: "orderedList",
    icon: IconOrderedList,
    ariaLabel: "orderedList",
    group: "indent",
    isCollapsable: false,
    callback: () => editor.chain().focus().toggleOrderedList().run(),
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
    type: "blockquote",
    icon: IconBlockquote,
    ariaLabel: "blockquote",
    group: "blockquote",
    isCollapsable: false,
    callback: () => editor.chain().focus().toggleBlockquote().run(),
  },
  {
    type: "deleteBorderNumber",
    icon: MaterialSymbolsDeleteSweepOutline,
    ariaLabel: "deleteBorderNumber",
    group: "borderNumber",
    isCollapsable: false,
    callback: () => editor.chain().focus().removeBorderNumbers().run(),
  },
])

const fixButtons = [
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
]

const editorButtons = computed(() =>
  buttons.value.map((button) => {
    let isActive

    if (button.group === "alignment") {
      isActive = editor.isActive({ textAlign: button.type })
    } else if (button.ariaLabel === "invisible-characters") {
      isActive = selectActiveState(editor.view.state)
    } else if (button.ariaLabel === "fullview") {
      isActive = editorExpanded.value
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

const buttonsDisabled = computed(
  () => !(props.editable && (hasFocus.value || isHovered.value)),
)

const menuBar = ref<HTMLElement>()
const fixButtonElements = ref<(typeof TextEditorButton)[]>([])
const collapsedButtonElements = ref<(typeof TextEditorButton)[]>([])
// All the HTML <button> elements of the TextEditorButtons, so we can call .focus() on them
const buttonElements = computed<HTMLElement[]>(() =>
  [...collapsedButtonElements.value, ...fixButtonElements.value]
    .flatMap((buttomComponent) => [
      buttomComponent.button,
      // If it is a collapsed button, it might have visible children
      ...(buttomComponent?.children ?? []),
    ])
    .filter((button) => !!button),
)

const focusedButtonIndex = ref(0)
const focusNextButton = () => {
  if (focusedButtonIndex.value >= buttonElements.value.length) {
    // If menu buttons are removed (collapsable), the index might be too high
    focusedButtonIndex.value = buttonElements.value.length - 1
  }
  if (focusedButtonIndex.value < buttonElements.value.length) {
    focusedButtonIndex.value++
  }
  focusCurrentButton()
}
const focusPreviousButton = () => {
  if (focusedButtonIndex.value >= buttonElements.value.length) {
    focusedButtonIndex.value = buttonElements.value.length - 1
  }
  if (focusedButtonIndex.value > 0) {
    focusedButtonIndex.value--
  }
  focusCurrentButton()
}
const focusCurrentButton = () => {
  const buttonElement = buttonElements.value?.[focusedButtonIndex.value]
  if (buttonElement) {
    buttonElement.focus()
  }
}

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
  <!-- eslint-disable vuejs-accessibility/no-static-element-interactions, vuejs-accessibility/mouse-events-have-key-events
   focus and blur events are covered in the editor properties, this is just additional fluff for mouse users -->
  <div
    id="text-editor"
    ref="editorElement"
    class="editor bg-white"
    fluid
    @blur="() => (hasFocus = false)"
    @focusin="() => (hasFocus = true)"
    @focusout="
      () => !editorElement?.matches(':focus-within') && (hasFocus = false)
    "
    @mouseenter="() => (isHovered = true)"
    @mouseleave="() => (isHovered = false)"
  >
    <!-- Menu bar can be focused so that you can navigate between the buttons with arrow left and right -->
    <!-- eslint-disable-next-line vuejs-accessibility/no-static-element-interactions -->
    <div
      ref="menuBar"
      :aria-label="ariaLabel + ' Button Leiste'"
      class="flex flex-row flex-wrap justify-between pb-8 pe-12 ps-12 pt-12"
      :tabindex="menuBar?.matches(':focus-within') ? -1 : 0"
      @focusin="focusCurrentButton"
      @keydown.left.stop.prevent="focusPreviousButton"
      @keydown.right.stop.prevent="focusNextButton"
    >
      <div class="flex flex-row">
        <TextEditorButton
          v-for="(button, index) in collapsedButtons"
          :key="index"
          v-bind="button"
          ref="collapsedButtonElements"
          :disabled="buttonsDisabled"
          :tab-index="-1"
          @toggle="handleButtonClick"
        />
      </div>
      <div class="flex flex-row">
        <TextEditorButton
          v-for="(button, index) in fixButtons"
          :key="index"
          v-bind="button"
          ref="fixButtonElements"
          :disabled="buttonsDisabled"
          :tab-index="-1"
          @toggle="handleButtonClick"
        />
      </div>
    </div>
    <hr class="ml-12 mr-12 border-blue-300" />
    <div>
      <EditorContent
        :class="editorSize"
        :data-testid="ariaLabel"
        :editor="editor"
      />
    </div>
  </div>
</template>
