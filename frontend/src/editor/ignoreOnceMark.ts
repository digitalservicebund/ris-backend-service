import { Mark } from "@tiptap/vue-3"

export const IgnoreOnceTagName = "ignore-once"

export const IgnoreOnceMark = Mark.create({
  name: IgnoreOnceTagName,
  // todo: check if we need this?
  priority: 1010,
  inclusive: true,
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
