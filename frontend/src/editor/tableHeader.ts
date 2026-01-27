import { TableHeader } from "@tiptap/extension-table-header"
import { createCellCommands } from "./tableUtil"

export const CustomTableHeader = TableHeader.extend({
  addAttributes() {
    return {
      ...this.parent?.(),
      style: {
        default: null,
        parseHTML: (element) => element.getAttribute("style") || null,
        renderHTML: (attributes) =>
          attributes.style ? { style: attributes.style } : {},
      },
    }
  },

  addCommands() {
    return createCellCommands()
  },
})
