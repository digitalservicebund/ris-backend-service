import { TableCell } from "@tiptap/extension-table-cell"

/**
 * Erweitert die Standard-TableCell Extension, um individuelle Rahmen-Attribute (td) zu speichern.
 * Diese Attribute werden über setCellAttribute gesetzt.
 */
export const CustomTableCell = TableCell.extend({
  addAttributes() {
    const parentAttributes = this.parent?.() || {}

    return {
      ...parentAttributes,
      backgroundColor: {
        default: null,
        renderHTML: (attributes) => {
          if (!attributes.backgroundColor) {
            return {}
          }

          return {
            style: `background-color: ${attributes.backgroundColor}`,
          }
        },
        parseHTML: (element) => {
          return element.style.backgroundColor.replace(/['"]+/g, "")
        },
      },
      borderTopValue: {
        default: null,
        renderHTML: (attributes) => {
          const value = attributes.borderTopValue
          if (!value) return {}
          return { style: `border-top: ${value}` }
        },
        parseHTML: (element) => element.style.borderTop || null,
      },
      borderRightValue: {
        default: null,
        renderHTML: (attributes) => {
          const value = attributes.borderRightValue
          if (!value) return {}
          return { style: `border-right: ${value}` }
        },
        parseHTML: (element) => element.style.borderRight || null,
      },
      borderBottomValue: {
        default: null,
        renderHTML: (attributes) => {
          const value = attributes.borderBottomValue
          if (!value) return {}
          return { style: `border-bottom: ${value}` }
        },
        parseHTML: (element) => element.style.borderBottom || null,
      },
      borderLeftValue: {
        default: null,
        renderHTML: (attributes) => {
          const value = attributes.borderLeftValue
          if (!value) return {}
          return { style: `border-left: ${value}` }
        },
        parseHTML: (element) => element.style.borderLeft || null,
      },
    }
  },
})
