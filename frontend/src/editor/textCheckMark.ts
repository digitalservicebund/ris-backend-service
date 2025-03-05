import { Mark } from "@tiptap/vue-3"
import { TextCheckTagName } from "@/types/languagetool"

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
        class: "lt",
      },
      0,
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
    }
  },
})
