import { Node } from "@tiptap/vue-3"
import "../styles/border-numbers.css"
import handleBackspace from "@/editor/shortcuts/handleBackspace"

declare module "@tiptap/core" {
  interface Commands<ReturnType> {
    removeBorderNumbers: {
      removeBorderNumbers: () => ReturnType
    }
    addBorderNumbers: {
      addBorderNumbers: () => ReturnType
    }
    paragraph: {
      getParagraph: () => ReturnType
    }
    borderNumber: {
      setBorderNumber: () => ReturnType
    }
    borderNumberNumber: {
      setBorderNumberNumber: () => ReturnType
    }
    borderNumberContent: {
      setBorderNumberContent: () => ReturnType
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
    return ["border-number", {}, 0]
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
    return ["number", {}, 0]
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
