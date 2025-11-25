import { TableCell } from "@tiptap/extension-table-cell"
import { createCellCommands } from "./tableUtil"

export const CustomTableCell = TableCell.extend({
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
