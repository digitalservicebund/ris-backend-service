import { ListItem } from "@tiptap/extension-list-item"

export const CustomListItem = ListItem.extend({
  addAttributes() {
    return {
      ...this.parent?.(),
      style: {
        default: null,
        parseHTML: (element) => {
          return element.getAttribute("style")
        },
        renderHTML: (attributes) => {
          return { style: attributes.style }
        },
      },
    }
  },
  addInputRules() {
    return []
  },
})
