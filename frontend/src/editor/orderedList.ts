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
      listStyleType: {
        default: "decimal",
        parseHTML: (element) => {
          const style = element.getAttribute("style") || ""
          const match = /list-style-type:\s*([^;]+)/.exec(style)
          return match ? match[1].trim() : "decimal"
        },
        renderHTML: (attributes) => {
          const listStyleType = attributes.listStyleType || "decimal"
          return {
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
              const listStyleType = TYPE_TO_STYLE[mappedType] || "decimal"
              attributes = { listStyleType }
            }
          } else if (typeof styleOrAttributes === "object") {
            attributes = styleOrAttributes
          } else if (!styleOrAttributes) {
            attributes = { listStyleType: "decimal" }
          }

          const { $from } = state.selection
          let currentListNode = null
          let currentListStyleType = null
          let currentListPos = null

          for (let depth = $from.depth; depth > 0; depth--) {
            const node = $from.node(depth)
            if (node.type.name === this.name) {
              currentListNode = node
              currentListStyleType = node.attrs.listStyleType
              currentListPos = $from.before(depth)
              break
            }
          }

          if (
            currentListNode &&
            currentListStyleType === attributes.listStyleType
          ) {
            return commands.toggleList(
              this.name,
              this.options.itemTypeName,
              this.options.keepMarks,
            )
          }

          if (
            currentListNode &&
            currentListStyleType !== attributes.listStyleType &&
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
