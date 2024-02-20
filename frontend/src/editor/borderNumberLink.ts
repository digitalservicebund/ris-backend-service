import { Node } from "@tiptap/vue-3"

export const BorderNumberLink = Node.create({
  name: "borderNumberLink",
  priority: 1000,
  group: "inline",
  atom: true,
  inline: true,
  selectable: false,
  content: "text*",
  parseHTML() {
    return [{ tag: "border-number-link" }]
  },
  renderHTML({ node }) {
    return [
      "border-number-link",
      {
        style:
          "color: #003350; font-weight: bold; text-decoration: underline; padding: 2px; font-style: italic",
        contenteditable: false,
      },
      `Rn. ${node.textContent}`,
    ]
  },
})
