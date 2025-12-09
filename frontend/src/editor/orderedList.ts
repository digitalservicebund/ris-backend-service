import { OrderedList } from "@tiptap/extension-ordered-list"

const LIST_STYLES: Record<string, string> = {
  numeric: "1",
  smallLatin: "a",
  capitalLatin: "A",
  smallRoman: "i",
  capitalRoman: "I",
  smallGreek: "α",
}

const TYPE_TO_STYLE: Record<string, string> = {
  "1": "decimal",
  a: "lower-alpha",
  A: "upper-alpha",
  i: "lower-roman",
  I: "upper-roman",
  α: "lower-greek",
}

declare module "@tiptap/core" {
  interface Commands<ReturnType> {
    customOrderedList: {
      toggleOrderedList: (
        styleOrAttributes?: string | Record<string, string>,
      ) => ReturnType
    }
  }
}

export const CustomOrderedList = OrderedList.extend({
  addAttributes() {
    return {
      ...this.parent?.(),
      type: {
        default: "1",
        parseHTML: (element) => element.getAttribute("type"),
        renderHTML: (attributes) => {
          const type = attributes.type || "1"
          const listStyleType = TYPE_TO_STYLE[type] || "decimal"
          return {
            type: type,
            style: `list-style-type: ${listStyleType};`,
          }
        },
      },
    }
  },
  addCommands() {
    return {
      toggleOrderedList:
        (styleOrAttributes?: string | Record<string, string>) =>
        ({ commands, state, tr }) => {
          let attributes: Record<string, string> = {}

          if (typeof styleOrAttributes === "string") {
            const mappedType = LIST_STYLES[styleOrAttributes]
            if (mappedType) {
              attributes = { type: mappedType }
            }
          } else if (typeof styleOrAttributes === "object") {
            attributes = styleOrAttributes
          } else if (!styleOrAttributes) {
            attributes = { type: "1" }
          }

          const { $from } = state.selection
          let currentListNode = null
          let currentListType = null
          let currentListPos = null

          for (let depth = $from.depth; depth > 0; depth--) {
            const node = $from.node(depth)
            if (node.type.name === this.name) {
              currentListNode = node
              currentListType = node.attrs.type
              currentListPos = $from.before(depth)
              break
            }
          }

          if (currentListNode && currentListType === attributes.type) {
            return commands.toggleList(
              this.name,
              this.options.itemTypeName,
              this.options.keepMarks,
            )
          }

          if (
            currentListNode &&
            currentListType !== attributes.type &&
            currentListPos !== null
          ) {
            tr.setNodeMarkup(currentListPos, undefined, {
              ...currentListNode.attrs,
              ...attributes,
            })
            return true
          }

          return commands.toggleList(
            this.name,
            this.options.itemTypeName,
            this.options.keepMarks,
            attributes,
          )
        },
    }
  },
  addInputRules() {
    return []
  },
})
