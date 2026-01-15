import { Node } from "@tiptap/core"
import { VueNodeViewRenderer } from "@tiptap/vue-3"

import HighlightComponent from "./ExtractionHighlight.vue"

const attributeMap = new Map<string, string>([
  ["extractionId", "data-extraction-id"],
  ["extractionClass", "data-extraction-class"],
  ["normalizedValue", "data-normalized-value"],
  ["targetPath", "data-target-path"],
])

const sharedAttributes = () => ({
  ...attributeMap.entries().reduce(
    (attrs, [attrName, dataAttr]) => ({
      ...attrs,
      [attrName]: {
        default: null,
        parseHTML: (element: HTMLElement) => element.getAttribute(dataAttr),
        renderHTML: (attributes: Record<string, string | null>) =>
          attributes[attrName] ? { [dataAttr]: attributes[attrName] } : {},
      },
    }),
    {} as Record<string, unknown>,
  ),
  tagName: {
    default: null,
    parseHTML: (element: HTMLElement) => element.tagName.toLowerCase(),
    renderHTML: () => ({}),
  },
})

const ExtractionHighlightInline = Node.create({
  name: "extractionHighlightInline",
  group: "inline",
  inline: true,
  content: "inline*",
  addAttributes: () => sharedAttributes(),
  parseHTML: () => [{ tag: "mark[data-extraction-id]" }],
  renderHTML: ({ HTMLAttributes }) => ["mark", HTMLAttributes, 0],
  addNodeView: () => VueNodeViewRenderer(HighlightComponent),
})

const ExtractionHighlightSection = Node.create({
  name: "extractionHighlightSection",
  group: "block",
  content: "block+",
  addAttributes: () => sharedAttributes(),
  parseHTML: () => [{ tag: "div[data-extraction-id]" }],
  renderHTML: ({ HTMLAttributes }) => ["div", HTMLAttributes, 0],
  addNodeView: () => VueNodeViewRenderer(HighlightComponent),
})

export default [ExtractionHighlightInline, ExtractionHighlightSection]
