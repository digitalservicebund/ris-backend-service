import { Node, nodeInputRule } from "@tiptap/vue-3"

export interface BorderNumberLinkOptions {
  validNumbers: string[]
}
export const BorderNumberLink = Node.create({
  name: "borderNumberLink",
  priority: 1000,
  group: "inline",
  atom: true,
  inline: true,
  selectable: false,
  content: "text*",
  addAttributes() {
    return {
      nr: {
        default: null,
        parseHTML: (element) => element.getAttribute("nr"),
        renderHTML: (attributes) => {
          if (!attributes.nr) {
            return {}
          }

          return {
            nr: attributes.nr,
          }
        },
      },
    }
  },
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
      `Rn. ${node.attrs.nr ?? node.textContent}`,
    ]
  },
  addKeyboardShortcuts() {
    return {
      Backspace: () => {
        return this.editor.commands.command(({ tr, state }) => {
          let isBorderNumberLink = false
          const { selection } = state
          const { empty, anchor } = selection

          if (!empty) {
            return false
          }

          state.doc.nodesBetween(anchor - 1, anchor, (node, pos) => {
            if (node.type.name === this.name) {
              isBorderNumberLink = true
              tr.insertText("##" || "", pos, pos + node.nodeSize)

              return false
            }
          })

          return isBorderNumberLink
        })
      },
    }
  },
  addInputRules() {
    return [
      nodeInputRule({
        find: /(##(\d+))\s$/,
        type: this.type,
        getAttributes(match) {
          console.log(match)
          return { nr: match[2] }
        },
      }),
    ]
  },
})
