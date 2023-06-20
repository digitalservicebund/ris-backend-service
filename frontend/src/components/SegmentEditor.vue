<script lang="ts" setup>
import { Document } from "@tiptap/extension-document"
import { History } from "@tiptap/extension-history"
import { Paragraph } from "@tiptap/extension-paragraph"
import { Text } from "@tiptap/extension-text"
import { Editor, EditorContent, JSONContent } from "@tiptap/vue-3"
import { watch } from "vue"
import {
  createSuggestionExtension,
  SuggestionGroupOptions,
} from "@/shared/editor/suggestion"

interface Props {
  modelValue?: Segment[]
  suggestions: SuggestionGroupOptions[]
}
interface Emits {
  (event: "update:modelValue", value: Segment[]): void
}
const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const suggestionExtensions = props.suggestions.map((options) =>
  createSuggestionExtension(options)
)
const EDITOR_CLASSES = [
  "p-16",
  "border-2",
  "border-blue-800",
  "focus:outline-2",
  "hover:outline-2",
  "h-128",
  "outline-0",
  "outline-blue-800",
  "outline-none",
  "outline-offset-[-4px]",
  "input",
  "overflow-y-auto",
]
const editor = new Editor({
  editorProps: {
    attributes: {
      tabindex: "0",
      class: EDITOR_CLASSES.join(" "),
    },
  },
  extensions: [Document, Paragraph, Text, History, ...suggestionExtensions],
  onUpdate: emitEditorContentAsModelValue,
})
function emitEditorContentAsModelValue(): void {
  const editorContent = editor.getJSON()
  const segments = parseEditorContentAsSegments(editorContent)
  emit("update:modelValue", segments)
}
function parseEditorContentAsSegments(data?: JSONContent): Segment[] {
  let paragraphs =
    data?.content?.filter(({ type }) => type == "paragraph") ?? []
  paragraphs = insertMewLineTextNodeBetweenParagraphs(paragraphs)
  let nodes = getAllNodes(paragraphs)
  nodes = mergeSequencesOfTextNodes(nodes)
  return nodes.map(mapEditorNodeToSegment)
}
/**
 * Inserts a new text node with a new line character as text in front of each
 * paragraph that is not the first one. This is helpful to convert from a list
 * of paragraphs with nodes to just a list of nodes while preserving the
 * information where the user created new lines.
 *
 * @example
 * ```ts
 * insertMewLineTextNodeBetweenParagraphs([
 *  { type: "paragraph", content: [{ type: "text", text: "a" }] }
 *  { type: "paragraph", content: [{ type: "text", text: "b" }] }
 * ])
 * // =>
 * // [
 * //   { type: "paragraph", content: [{ type: "text", text: "a" }] },
 * //   { type: "paragraph", content: [
 * //     { type: "text", text: "\n" },
 * //     { type: "text", text: "b" },
 * //   ]},
 * // ]
 * ```
 */
function insertMewLineTextNodeBetweenParagraphs(
  paragraphs: JSONContent[]
): JSONContent[] {
  return paragraphs.map((paragraph, index) => {
    const content = paragraph.content ?? []
    index > 0 && content.unshift({ type: "text", text: "\n" })
    return { ...paragraph, content }
  })
}
function getAllNodes(paragraphs: JSONContent[]): JSONContent[] {
  return paragraphs.reduce(
    (allContentNodes, paragraph) => [
      ...allContentNodes,
      ...(paragraph.content ?? []),
    ],
    [] as JSONContent[]
  )
}
/**
 * Shrinks the list of nodes by merging the text of consecutive text nodes together.
 *
 * @example
 * ```ts
 * mergeSequencesOfTextNodes([
 *  { type: "not-text" },
 *  { type: "text", text: "a" },
 *  { type: "text", text: "c" },
 *  { type: "not-text" },
 * ])
 * // => [{ type: "not-text" }, { type: "text", text: "ab" }, { type: "not-text" }]
 * ```
 */
function mergeSequencesOfTextNodes(nodes: JSONContent[]): JSONContent[] {
  return nodes.reduce((nodes, currentNode) => {
    const lastIndex = nodes.length - 1
    const lastNode = nodes[lastIndex] as JSONContent | undefined
    const shouldMergeWithLastNode =
      lastNode?.type == "text" && currentNode.type == "text"
    if (shouldMergeWithLastNode) {
      const text = (lastNode?.text ?? "") + currentNode.text
      const mergedNode = { ...lastNode, ...currentNode, text }
      nodes[lastIndex] = mergedNode
    } else {
      nodes.push(currentNode)
    }
    return nodes
  }, [] as JSONContent[])
}
function mapEditorNodeToSegment(node: JSONContent): Segment {
  const { type, attrs, text } = node
  return {
    type: type ?? "text",
    content: (attrs?.label as string) ?? text ?? "",
    id: attrs?.id,
  }
}
watch(() => props.modelValue, updateEditorContent, { immediate: true })
function updateEditorContent(): void {
  const editorContent = parseSegmentsAsEditorContent(props.modelValue)
  const cursorPosition = editor.state.selection
  editor.commands.setContent(editorContent, false)
  editor.commands.setTextSelection(cursorPosition)
}
function mapSegmentToEditorNode(segment: Segment): JSONContent {
  const isSuggestion = segment.type != "text"
  const content = isSuggestion
    ? { attrs: { id: segment.id, label: segment.content } }
    : { text: segment.content }
  return { type: segment.type, ...content }
}
/**
 * Divides a list of editor nodes into groups based on lines. That means it
 * collects all nodes up to a text node that includes a new line character. Then
 * it splits that node by all including new line characters, appends the first
 * part to the last group/line and opens new groups/lines for all other parts.
 * This is helpful to use the groups as paragraph content nodes in the editor
 * again, as the editor does not understand new lines.
 *
 * @example
 * ```ts
 * groupEditorNodesByNewLines([
 *  { type: "text", text: "a" }
 *  { type: "text", text: "b\nc\nd" }
 *  { type: "text", text: "e" }
 * ])
 * // =>
 * // [
 * //   [{ type: "text", text: "a" }, { type: "text", text: "b" }],
 * //   [{ type: "text", text: "c" }],
 * //   [{ type: "text", text: "d" }, { type: "text", text: "e" }],
 * // ]
 * ```
 */
function groupEditorNodesByNewLines(nodes: JSONContent[]): JSONContent {
  return nodes.reduce(
    (groups, node) => {
      const linesOfNode = node.text?.split("\n")
      const splitNodes = linesOfNode?.map((text) => ({ ...node, text }))
      const nodesToAdd = splitNodes ?? [node]
      groups[groups.length - 1].push(nodesToAdd[0])
      nodesToAdd.splice(1).forEach((node) => groups.push([node]))
      return groups
    },
    [[]] as JSONContent[][]
  )
}
function parseSegmentsAsEditorContent(segments?: Segment[]): JSONContent {
  const nodes = segments?.map(mapSegmentToEditorNode) ?? []
  const nodeGroups = groupEditorNodesByNewLines(nodes)
  // TipTap throws errors for empty text nodes. Such can be a result from the
  // former splitting across paragraphs.
  const nodeGroupdsWithoutEmptyTexts = nodeGroups?.map((group: JSONContent[]) =>
    group.filter(({ text }) => text == undefined || text.length > 0)
  )
  const paragraphs = nodeGroupdsWithoutEmptyTexts?.map(
    (nodeGroup: JSONContent) => ({
      type: "paragraph",
      content: nodeGroup,
    })
  )
  return { type: "doc", content: paragraphs }
}

/**
 * A segment/part/snippet of the content within the editor. Segments are
 * a mixture of normal text the user types and suggestion items the user chose.
 *
 * Line breaks are represented as newline characters (`\n`) within a unified
 * text segment. The dimension of the lines in the editor is thereby "removed"
 * and could only be restored by post-processing the segments with newline
 * characters again.
 */
export interface Segment {
  /**
   * Type descriptor of segment. Is `"text"` for anything except for suggestions
   * the user chose. In that case it is the custom type of the suggestion group.
   */
  type: string | "text"
  /**
   * The user facing content visible in the editor. For text segments this is
   * just the text as is. For suggestions the user chose, this is the label of
   * the suggestion item.
   */
  content: string
  /**
   * In case of a suggestion based segment, this is the identifier of the chosen
   * suggestion item. This is used for better data processing by separating it
   * from the user facing content, similar to a typical dropdown.
   */
  // Could be improved with a generic type. But hard to establish within the Vue
  // component interface having multiple suggestion groups.
  id?: string
}
export type { SuggestionGroupOptions } from "@/shared/editor/suggestion"
</script>

<template>
  <EditorContent :editor="editor" />
</template>
