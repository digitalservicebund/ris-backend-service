<script lang="ts" setup>
import { commands, selectActiveState } from "@guardian/prosemirror-invisibles"
import { Editor } from "@tiptap/vue-3"
import { computed, ref } from "vue"
import TextEditorButton, {
  EditorButton,
} from "@/components/input/TextEditorButton.vue"
import { useCollapsingMenuBar } from "@/composables/useCollapsingMenuBar"
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
  editorExpanded: boolean
  ariaLabel: string
  buttonsDisabled: boolean
  editor: Editor
  containerWidth: number
}

const props = defineProps<Props>()

const emit = defineEmits<{ onEditorExpandedChanged: boolean }>()

const buttons = computed(() => [
  {
    type: "expand",
    icon: IconExpand,
    ariaLabel: "fullview",
    group: "display",
    isCollapsable: false,
    callback: () => emit("onEditorExpandedChanged", !props.editorExpanded),
  },
  {
    type: "invisible-characters",
    icon: IconParagraph,
    ariaLabel: "invisible-characters",
    group: "display",
    isCollapsable: false,
    callback: () =>
      commands.toggleActiveState()(
        props.editor.state,
        props.editor.view.dispatch,
      ),
  },
  {
    type: "bold",
    icon: IconBold,
    ariaLabel: "bold",
    group: "format",
    isCollapsable: false,
    callback: () => props.editor.chain().focus().toggleMark("bold").run(),
  },
  {
    type: "italic",
    icon: IconItalic,
    ariaLabel: "italic",
    group: "format",
    isCollapsable: false,
    callback: () => props.editor.chain().focus().toggleMark("italic").run(),
  },
  {
    type: "underline",
    icon: IconUnderline,
    ariaLabel: "underline",
    group: "format",
    isCollapsable: false,
    callback: () => props.editor.chain().focus().toggleMark("underline").run(),
  },
  {
    type: "strike",
    icon: IconStrikethrough,
    ariaLabel: "strike",
    group: "format",
    isCollapsable: false,
    callback: () => props.editor.chain().focus().toggleMark("strike").run(),
  },
  {
    type: "superscript",
    icon: IconSuperscript,
    ariaLabel: "superscript",
    group: "format",
    isCollapsable: false,
    callback: () =>
      props.editor.chain().focus().toggleMark("superscript").run(),
  },
  {
    type: "subscript",
    icon: IconSubscript,
    ariaLabel: "subscript",
    group: "format",
    isCollapsable: false,
    callback: () => props.editor.chain().focus().toggleMark("subscript").run(),
  },
  {
    type: "left",
    icon: IconAlignLeft,
    ariaLabel: "left",
    group: "alignment",
    isCollapsable: true,
    callback: () => props.editor.chain().focus().setTextAlign("left").run(),
  },
  {
    type: "center",
    icon: IconAlignCenter,
    ariaLabel: "center",
    group: "alignment",
    isCollapsable: true,
    callback: () => props.editor.chain().focus().setTextAlign("center").run(),
  },
  {
    type: "right",
    icon: IconAlignRight,
    ariaLabel: "right",
    group: "alignment",
    isCollapsable: true,
    callback: () => props.editor.chain().focus().setTextAlign("right").run(),
  },
  {
    type: "justify",
    icon: IconAlignJustify,
    ariaLabel: "justify",
    group: "alignment",
    isCollapsable: true,
    callback: () => props.editor.chain().focus().setTextAlign("justify").run(),
  },
  {
    type: "bulletList",
    icon: IconUnorderedList,
    ariaLabel: "bulletList",
    group: "indent",
    isCollapsable: false,
    callback: () => props.editor.chain().focus().toggleBulletList().run(),
  },
  {
    type: "orderedList",
    icon: IconOrderedList,
    ariaLabel: "orderedList",
    group: "indent",
    isCollapsable: false,
    callback: () => props.editor.chain().focus().toggleOrderedList().run(),
  },
  {
    type: "outdent",
    icon: IndentDecrease,
    ariaLabel: "outdent",
    group: "indent",
    isCollapsable: false,
    callback: () => props.editor.chain().focus().outdent().run(),
  },
  {
    type: "indent",
    icon: IndentIncrease,
    ariaLabel: "indent",
    group: "indent",
    isCollapsable: false,
    callback: () => props.editor.chain().focus().indent().run(),
  },
  {
    type: "blockquote",
    icon: IconBlockquote,
    ariaLabel: "blockquote",
    group: "blockquote",
    isCollapsable: false,
    callback: () => props.editor.chain().focus().toggleBlockquote().run(),
  },
  {
    type: "deleteBorderNumber",
    icon: MaterialSymbolsDeleteSweepOutline,
    ariaLabel: "deleteBorderNumber",
    group: "borderNumber",
    isCollapsable: false,
    callback: () => props.editor.chain().focus().removeBorderNumbers().run(),
  },
])

const fixButtons = [
  {
    type: "undo",
    icon: IconUndo,
    ariaLabel: "undo",
    group: "arrow",
    isCollapsable: false,
    callback: () => props.editor.chain().focus().undo().run(),
  },
  {
    type: "redo",
    icon: IconRedo,
    ariaLabel: "redo",
    group: "arrow",
    isCollapsable: false,
    callback: () => props.editor.chain().focus().redo().run(),
  },
]

const editorButtons = computed(() =>
  buttons.value.map((button) => {
    let isActive

    if (button.group === "alignment") {
      isActive = props.editor.isActive({ textAlign: button.type })
    } else if (button.ariaLabel === "invisible-characters") {
      isActive = selectActiveState(props.editor.view.state)
    } else if (button.ariaLabel === "fullview") {
      isActive = props.editorExpanded
    } else {
      isActive = props.editor.isActive(button.type)
    }

    return {
      ...button,
      isActive,
    }
  }),
)
const buttonSize = 48
const maxButtonEntries = computed(() =>
  Math.floor((props.containerWidth.value - 100) / buttonSize),
)

const { collapsedButtons } = useCollapsingMenuBar(
  editorButtons,
  maxButtonEntries,
)

function handleButtonClick(button: EditorButton) {
  if (button.callback) button.callback()
}

const menuBar = ref<HTMLElement>()
const fixButtonElements = ref<(typeof TextEditorButton)[]>([])
const collapsedButtonElements = ref<(typeof TextEditorButton)[]>([])
// All the HTML <button> elements of the TextEditorButtons, so we can call .focus() on them
const buttonElements = computed<HTMLElement[]>(() =>
  [...collapsedButtonElements.value, ...fixButtonElements.value]
    .flatMap((buttonComponent) => [
      buttonComponent.button,
      // If it is a collapsed button, it might have visible children
      ...(buttonComponent?.children ?? []),
    ])
    .filter((button) => !!button),
)

const focusedButtonIndex = ref(0)
const focusNextButton = () => {
  if (focusedButtonIndex.value < buttonElements.value.length) {
    focusedButtonIndex.value++
  }
  focusCurrentButton()
}
const focusPreviousButton = () => {
  if (focusedButtonIndex.value > 0) {
    focusedButtonIndex.value--
  }
  focusCurrentButton()
}
const focusCurrentButton = () => {
  if (focusedButtonIndex.value >= buttonElements.value.length) {
    // If menu buttons are removed (collapsable), the index might be too high
    focusedButtonIndex.value = buttonElements.value.length - 1
  }
  const buttonElement = buttonElements.value?.[focusedButtonIndex.value]
  if (buttonElement && !props.buttonsDisabled) {
    buttonElement.focus()
  } else if (props.buttonsDisabled) {
    // When navigating from a previous element the buttons are initially disabled.
    // We don't want to focus the toolbar but the EditorContent instead
    props.editor.commands.focus()
  }
}

const ariaLabel = props.ariaLabel ? props.ariaLabel : null
</script>

<template>
  <!-- Menu bar can be focused so that you can navigate between the buttons with arrow left and right -->
  <!-- eslint-disable-next-line vuejs-accessibility/no-static-element-interactions -->
  <div
    ref="menuBar"
    :aria-label="ariaLabel + ' Button Leiste'"
    class="flex flex-row flex-wrap justify-between pb-8 pe-12 ps-12 pt-12"
    :tabindex="
      menuBar?.matches(':focus-within') || props.buttonsDisabled ? -1 : 0
    "
    @focus="focusCurrentButton"
    @keydown.left.stop.prevent="focusPreviousButton"
    @keydown.right.stop.prevent="focusNextButton"
  >
    <div class="flex flex-row">
      <TextEditorButton
        v-for="(button, index) in collapsedButtons"
        :key="index"
        v-bind="button"
        ref="collapsedButtonElements"
        :disabled="props.buttonsDisabled"
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
        :disabled="props.buttonsDisabled"
        :tab-index="-1"
        @toggle="handleButtonClick"
      />
    </div>
  </div>
</template>
