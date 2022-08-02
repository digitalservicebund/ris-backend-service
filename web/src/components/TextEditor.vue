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
import { watch, ref, onMounted } from "vue"
import { onBeforeRouteUpdate } from "vue-router"
import {
  BorderNumber,
  BorderNumberContent,
  BorderNumberNumber,
} from "../editor/border-number"
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
const showMore = ref<boolean>(false)
const showMoreTextAlign = ref<boolean>(false)
const showImageAlignment = ref<boolean>(false)
const showListStyles = ref<boolean>(false)
const sm = ref<boolean>(false)
const md = ref<boolean>(false)
const lg = ref<boolean>(false)

const editor = new Editor({
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

const onResize = () => {
  // console.log("resize triggered")
  let containerWidth
  const containerElement = document.getElementById("container")
  if (containerElement)
    containerWidth = containerElement.getBoundingClientRect().width
  // console.log(containerWidth)
  calculateBreakpoints(containerWidth)
}

const calculateBreakpoints = (containerWidth: number | undefined) => {
  if (containerWidth) {
    if (containerWidth < 830) {
      // console.log("small")
      sm.value = true
      md.value = false
      lg.value = false
    } else if (containerWidth >= 830 && containerWidth < 950) {
      // console.log("medium")
      sm.value = false
      md.value = true
      lg.value = false
    } else {
      // console.log("large")
      sm.value = false
      md.value = false
      lg.value = true
    }
  }
}

const toggleShowMore = () => (showMore.value = !showMore.value)
const toggleShowTextAlignModal = () => {
  showMoreTextAlign.value = !showMoreTextAlign.value
  showListStyles.value = false
  showImageAlignment.value = false
}
const toggleShowImageAlignmentModal = () => {
  showImageAlignment.value = !showImageAlignment.value
  showListStyles.value = false
  showMoreTextAlign.value = false
}
const toggleShowListStylesModal = () => {
  showListStyles.value = !showListStyles.value
  showImageAlignment.value = false
  showMoreTextAlign.value = false
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
  const isShowButtons = props.editable && hasFocus.value
  // const isShowButtons = props.editable

  if (!isShowButtons) {
    showListStyles.value = false
    showImageAlignment.value = false
    showMoreTextAlign.value = false
  }
  return isShowButtons
  // return props.editable
}

const showMoreOptions = () => {
  return showMore.value
}

interface EditorBtn {
  type: string
  icon: string
}

const editorBtnsGroup1: EditorBtn[] = [
  ["undo", "undo"],
  ["redo", "redo"],
].map((button) => {
  return {
    type: button[0],
    icon: button[1],
  }
})

const editorBtnsGroup2: EditorBtn[] = [
  ["bold", "format_bold"],
  ["italic", "format_italic"],
  ["underline", "format_underlined"],
  ["strike", "strikethrough_s"],
].map((button) => {
  return {
    type: button[0],
    icon: button[1],
  }
})

const editorBtnsGroup3: EditorBtn[] = [
  ["left", "format_align_left"],
  ["center", "format_align_center"],
  ["right", "format_align_right"],
  ["justify", "format_align_justify"],
].map((button) => {
  return {
    type: button[0],
    icon: button[1],
  }
})

const editorBtnsGroup4: EditorBtn[] = [
  ["superscript", "superscript"],
  ["subscript", "subscript"],
].map((button) => {
  return {
    type: button[0],
    icon: button[1],
  }
})

const editorBtnsGroup5: EditorBtn[] = [
  ["numbered-list", "format_list_numbered"],
  ["bullet-list", "format_list_bulleted"],
].map((button) => {
  return {
    type: button[0],
    icon: button[1],
  }
})
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

// same as beforeRouteUpdate option with no access to `this`
onBeforeRouteUpdate(async () => {
  // only fetch the user if the id changed as maybe only the query or the hash changed
  onResize()
})
</script>

<template>
  <v-container id="container" v-resize="onResize" fluid>
    <v-row
      v-if="showButtons()"
      :aria-label="
        props.ariaLabel ? props.ariaLabel + ' Editor Button Leiste' : null
      "
    >
      <v-col v-for="(btn, index) in editorBtnsGroup1" :key="index">
        <v-icon
          class="editor-btn"
          :class="{ 'editor-btn__active': editor.isActive(btn.type) }"
          @click="editor.chain().focus().toggleMark(btn.type).run()"
          @mousedown.prevent=""
          >{{ btn.icon }}</v-icon
        >
      </v-col>

      <v-divider inset vertical></v-divider>

      <v-col v-for="(btn, index) in editorBtnsGroup2" :key="index">
        <v-icon
          class="editor-btn"
          :class="{ 'editor-btn__active': editor.isActive(btn.type) }"
          @click="editor.chain().focus().toggleMark(btn.type).run()"
          @mousedown.prevent=""
          >{{ btn.icon }}</v-icon
        >
      </v-col>

      <v-divider inset vertical></v-divider>

      <v-col>Heading</v-col>

      <v-divider inset vertical></v-divider>

      <v-col v-show="!lg" class="display-group">
        <div class="dropdown-container">
          <div class="dropdown-icons">
            <v-icon @click="toggleShowTextAlignModal()" @mousedown.prevent=""
              >format_align_left</v-icon
            >
            <v-icon @click="toggleShowTextAlignModal()" @mousedown.prevent=""
              >arrow_drop_down</v-icon
            >
          </div>
          <div v-if="showMoreTextAlign" class="dropdown-content">
            <v-col class="display-group pa-0">
              <v-col
                v-for="(btn, index) in editorBtnsGroup3"
                :key="index"
                class="dropdown-content-items"
              >
                <v-icon
                  class="editor-btn"
                  :class="{ 'editor-btn__active': editor.isActive(btn.type) }"
                  @click="editor.chain().focus().setTextAlign(btn.type).run()"
                  @mousedown.prevent=""
                  >{{ btn.icon }}
                </v-icon>
              </v-col>
            </v-col>
          </div>
        </div>
      </v-col>
      <v-col v-show="lg" class="display-group pa-0">
        <v-col v-for="(btn, index) in editorBtnsGroup3" :key="index">
          <v-icon
            class="editor-btn"
            :class="{ 'editor-btn__active': editor.isActive(btn.type) }"
            @click="editor.chain().focus().setTextAlign(btn.type).run()"
            @mousedown.prevent=""
            >{{ btn.icon }}</v-icon
          >
        </v-col>
      </v-col>

      <v-divider inset vertical></v-divider>

      <v-col v-for="(btn, index) in editorBtnsGroup4" v-show="!sm" :key="index">
        <v-icon
          class="editor-btn"
          :class="{ 'editor-btn__active': editor.isActive(btn.type) }"
          @click="editor.chain().focus().toggleMark(btn.type).run()"
          @mousedown.prevent=""
          >{{ btn.icon }}</v-icon
        >
      </v-col>

      <v-divider v-show="!sm" inset vertical></v-divider>

      <v-col v-show="md" class="display-group">
        <div class="dropdown-container">
          <div class="dropdown-icons">
            <v-icon @click="toggleShowListStylesModal()" @mousedown.prevent=""
              >format_list_bulleted</v-icon
            >
            <v-icon @click="toggleShowListStylesModal()" @mousedown.prevent=""
              >arrow_drop_down</v-icon
            >
          </div>
          <div v-if="showListStyles" class="dropdown-content">
            <v-col class="display-group pa-0">
              <v-col
                v-for="(btn, index) in editorBtnsGroup5"
                :key="index"
                class="dropdown-content-items"
              >
                <v-icon
                  class="editor-btn"
                  :class="{ 'editor-btn__active': editor.isActive(btn.type) }"
                  @click="editor.chain().focus().toggleMark(btn.type).run()"
                  @mousedown.prevent=""
                  >{{ btn.icon }}
                </v-icon>
              </v-col>
            </v-col>
          </div>
        </div>
      </v-col>

      <v-col v-for="(btn, index) in editorBtnsGroup5" v-show="lg" :key="index">
        <v-icon
          class="editor-btn"
          :class="{ 'editor-btn__active': editor.isActive(btn.type) }"
          @click="editor.chain().focus().toggleMark(btn.type).run()"
          @mousedown.prevent=""
          >{{ btn.icon }}</v-icon
        >
      </v-col>

      <v-divider inset vertical></v-divider>
      <v-col v-show="md" class="display-group">
        <div class="dropdown-container">
          <div class="dropdown-icons">
            <v-icon
              @click="toggleShowImageAlignmentModal()"
              @mousedown.prevent=""
              >vertical_split</v-icon
            >
            <v-icon
              @click="toggleShowImageAlignmentModal()"
              @mousedown.prevent=""
              >arrow_drop_down</v-icon
            >
          </div>
          <div v-if="showImageAlignment" class="dropdown-content">
            <div class="dropdown-content-items">
              <v-col class="display-group pa-0">
                <v-col>
                  <v-icon>vertical_split</v-icon>
                </v-col>
                <v-col>
                  <v-icon class="mirrored">vertical_split</v-icon>
                </v-col>
              </v-col>
            </div>
          </div>
        </div>
      </v-col>

      <v-divider v-show="lg" inset vertical></v-divider>

      <v-col v-show="lg" class="display-group pa-0">
        <v-col>
          <v-icon>vertical_split</v-icon>
        </v-col>
        <v-col>
          <v-icon class="mirrored">vertical_split</v-icon>
        </v-col>
        <v-divider inset vertical></v-divider>
      </v-col>

      <v-divider v-show="md" inset vertical></v-divider>
      <v-col v-show="md">
        <v-icon>table_chart</v-icon>
      </v-col>

      <v-col v-show="sm">
        <v-icon @click="toggleShowMore()" @mousedown.prevent=""
          >more_horiz</v-icon
        >
      </v-col>

      <v-col>
        <v-icon>123</v-icon>
      </v-col>
      <v-col>
        <v-icon>open_in_full</v-icon>
      </v-col>
    </v-row>

    <!-- Small layout second row on showMore button click-->
    <v-row v-show="sm" v-if="showMoreOptions() && showButtons()">
      <v-col>
        <v-divider class="horizontal-divider"></v-divider>
      </v-col>
    </v-row>

    <v-row v-show="sm" v-if="showMoreOptions() && showButtons()">
      <v-col v-for="(btn, index) in editorBtnsGroup4" :key="index">
        <v-icon
          class="editor-btn"
          :class="{ 'editor-btn__active': editor.isActive(btn.type) }"
          @click="editor.chain().focus().toggleMark(btn.type).run()"
          @mousedown.prevent=""
          >{{ btn.icon }}</v-icon
        >
      </v-col>

      <v-divider inset vertical></v-divider>

      <v-col v-for="(btn, index) in editorBtnsGroup5" :key="index">
        <v-icon
          class="editor-btn"
          :class="{ 'editor-btn__active': editor.isActive(btn.type) }"
          @click="editor.chain().focus().toggleMark(btn.type).run()"
          @mousedown.prevent=""
          >{{ btn.icon }}</v-icon
        >
      </v-col>

      <v-divider inset vertical></v-divider>

      <v-col>
        <v-icon>vertical_split</v-icon>
      </v-col>
      <v-col>
        <v-icon class="mirrored">vertical_split</v-icon>
      </v-col>

      <v-divider inset vertical></v-divider>
      <v-col>
        <v-icon>table_chart</v-icon>
      </v-col>
    </v-row>

    <v-row v-if="showButtons()">
      <v-col></v-col>
    </v-row>
    <v-divider v-if="showButtons()" class="horizontal-divider"></v-divider>
    <v-row>
      <v-col cols="12">
        <editor-content
          :aria-label="ariaLabel"
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

.display-group {
  display: flex;
  flex-direction: row;
}

.mirrored {
  transform: scaleX(-1);
}

.horizontal-divider {
  border-color: #004b76;
  margin-left: -16px;
  margin-right: -16px;
}

.dropdown-container {
  width: max-content;
  position: relative;
  display: inline-block;

  .dropdown-content {
    display: flex;
    flex-direction: row;
    background: $white;
    border: 1px solid #004b76;
    position: absolute;
    right: 0;
    z-index: 1;
  }
}
</style>
