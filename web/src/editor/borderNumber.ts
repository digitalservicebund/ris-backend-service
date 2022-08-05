import { Node } from "@tiptap/vue-3"

export const BorderNumber = Node.create({
  name: "borderNumber",
  priority: 1000,
  group: "block",
  content: "border+",
  parseHTML() {
    return [{ tag: "border-number" }]
  },
  renderHTML() {
    return ["border-number", { style: "display: flex; margin-bottom: 10px" }, 0]
  },
})

export const BorderNumberNumber = Node.create({
  name: "borderNumberNumber",
  priority: 1000,
  group: "border",
  content: "inline",
  parseHTML() {
    return [{ tag: "number" }]
  },
  renderHTML() {
    return [
      "number",
      { style: "padding-top: 10px; padding-left: 10px; min-width: 40px;" },
      0,
    ]
  },
})

export const BorderNumberContent = Node.create({
  name: "borderNumberContent",
  priority: 1000,
  group: "border",
  content: "inline",
  parseHTML() {
    return [{ tag: "content" }]
  },
  renderHTML() {
    return ["content", 0]
  },
})
