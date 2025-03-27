import "@/styles/text-check.scss"
import { Mark } from "@tiptap/vue-3"
import { TextCheckTagName } from "@/types/textCheck"

export const TextCheckMark = Mark.create({
  name: TextCheckTagName,
  priority: 1000,
  group: "inline",

  parseHTML() {
    return [{ tag: "text-check" }]
  },

  renderHTML({ HTMLAttributes }) {
    return [
      "text-check",
      {
        id: HTMLAttributes.id,
        type: HTMLAttributes.type,
        ignored: HTMLAttributes.ignored,
      },
    ]
  },

  addAttributes() {
    return {
      id: {
        parseHTML: (element) => element.getAttribute("id"),
        renderHTML: (attributes) => ({
          id: attributes.id,
        }),
      },
      type: {
        parseHTML: (element) => element.getAttribute("type"),
        renderHTML: (attributes) => ({ type: attributes.type }),
      },
      ignored: {
        parseHTML: (element) => element.getAttribute("ignored"),
        renderHTML: (attributes) => ({ ignored: attributes.ignored }),
      },
    }
  },
})
