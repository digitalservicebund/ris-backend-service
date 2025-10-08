import "@/styles/ignore-once.scss"
import { Mark } from "@tiptap/vue-3"
import { IgnoreOnceTagName } from "@/types/textCheck"

export const IgnoreOnceMark = Mark.create({
  name: IgnoreOnceTagName,
  priority: 1000,
  group: "inline",
  parseHTML() {
    return [
      {
        tag: "ignore-once",
      },
    ]
  },
  renderHTML({ HTMLAttributes }) {
    return ["ignore-once", HTMLAttributes, 0]
  },
})
