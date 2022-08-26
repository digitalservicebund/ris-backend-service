import { OrderedList } from "@tiptap/extension-ordered-list"

export const CustomOrderedList = OrderedList.extend({
  addAttributes() {
    return {
      type: {
        default: null,
        parseHTML: (element) => element.getAttribute("type"),
        renderHTML: (attributes) => {
          return {
            type: attributes.type,
          }
        },
      },
    }
  },
})
