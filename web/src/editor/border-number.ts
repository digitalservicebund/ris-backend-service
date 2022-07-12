import { mergeAttributes, Node } from "@tiptap/vue-3"

interface BorderNumberOptions {
  HTMLAttributes: Record<string, unknown>
}

declare module "@tiptap/core" {
  interface Commands<ReturnType> {
    heading: {
      setRandnummer: (attributes: { number: number }) => ReturnType
    }
  }
}

export const BorderNumber = Node.create<BorderNumberOptions>({
  name: "borderNumber",
  priority: 1000,
  group: "block",
  content: "inline*",
  addAttributes() {
    return { number: "1" }
  },
  addOptions() {
    return {
      HTMLAttributes: {
        style: "display: flex; margin-bottom: 10px",
      },
    }
  },
  parseHTML() {
    return [{ tag: "border-number" }]
  },
  renderHTML({ HTMLAttributes }) {
    return [
      "div",
      mergeAttributes(this.options.HTMLAttributes, HTMLAttributes),
      [
        "div",
        { style: "padding-top: 10px; padding-left: 10px; min-width: 40px;" },
        HTMLAttributes.number.toString(),
      ],
      ["div", 0],
    ]
  },
  addCommands() {
    return {
      setRandnummer:
        (attributes) =>
        ({ commands }) => {
          return commands.setNode(this.name, attributes)
        },
    }
  },
})
