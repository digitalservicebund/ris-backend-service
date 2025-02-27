import { Mark } from "@tiptap/vue-3"

export const TextCheck = Mark.create({
  name: "textCheck",
  priority: 1000,
  group: "block",
  parseHTML() {
    return [{ tag: "text-check" }]
  },
  renderHTML() {
    return ["text-check", { class: "lt" }, 0]
  },
})
