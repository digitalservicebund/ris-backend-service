import { TableHeader } from "@tiptap/extension-table-header"
import { getBorderAttributes } from "./tableCell"

/**
 * Erweitert die Standard-TableHeader Extension und verwendet die getBorderAttributes Logik.
 */
export const CustomTableHeader = TableHeader.extend({
  addAttributes() {
    const parentAttributes = this.parent?.() || {}

    return {
      ...parentAttributes,
      ...getBorderAttributes(),
    }
  },
})
