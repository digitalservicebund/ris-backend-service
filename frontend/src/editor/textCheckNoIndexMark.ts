import { Mark } from "@tiptap/vue-3"
import { NoIndexTagName } from "@/types/languagetool"

export const NoIndexTagMark = Mark.create({
  name: NoIndexTagName,
  priority: 1000,
  group: "inline",

  parseHTML() {
    return [{ tag: "noindex" }]
  },

  renderHTML() {
    return [
      "noindex",
      {
        class: "noindex",
      },
      0,
    ]
  },
})
