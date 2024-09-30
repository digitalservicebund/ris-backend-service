import { Node } from "@tiptap/vue-3"
import removeBorderNumbers from "@/editor/commands/removeBorderNumbers"
import { handleBackspace } from "@/editor/shortcuts/handleBackspace"

declare module "@tiptap/core" {
  interface Commands<ReturnType> {
    removeBorderNumbers: {
      removeBorderNumbers: () => ReturnType
    }
  }
}

export const BorderNumber = Node.create({
  name: "borderNumber",
  priority: 1000,
  group: "block",
  content: "borderNumberNumber borderNumberContent",
  parseHTML() {
    return [{ tag: "border-number" }]
  },
  renderHTML() {
    return ["border-number", { style: "display: flex; margin-bottom: 10px" }, 0]
  },
  addCommands() {
    return {
      removeBorderNumbers: () => removeBorderNumbers,
    }
  },
  addKeyboardShortcuts() {
    return {
      Backspace: ({ editor }) => handleBackspace(editor),
    }
  },
})

export const BorderNumberNumber = Node.create({
  name: "borderNumberNumber",
  priority: 1000,
  group: "border",
  content: "inline*",
  parseHTML() {
    return [{ tag: "number" }]
  },
  renderHTML() {
    return [
      "number",
      { style: "padding-left: 10px; min-width: 40px; editable: false" },
      0,
    ]
  },
})

export const BorderNumberContent = Node.create({
  name: "borderNumberContent",
  priority: 1000,
  group: "border",
  content: "block*",
  parseHTML() {
    return [{ tag: "content" }]
  },
  renderHTML() {
    return ["content", 0]
  },
})
