import { TableCell } from "@tiptap/extension-table-cell"

/**
 * Definiert die vier Border-Attribute (Top, Right, Bottom, Left) für Tabellenzellen.
 * @returns Ein Objekt mit Attributdefinitionen für addAttributes().
 */
export const getBorderAttributes = () => {
  return {
    borderTopValue: {
      default: null,
      renderHTML: (attributes: { borderTopValue: string }) => {
        const value = attributes.borderTopValue
        if (!value) return null
        return { style: `border-top: ${value}` }
      },
      parseHTML: (element: { style: { borderTop: string } }) =>
        element.style.borderTop || null,
    },
    borderRightValue: {
      default: null,
      renderHTML: (attributes: { borderRightValue: string }) => {
        const value = attributes.borderRightValue
        if (!value) return null
        return { style: `border-right: ${value}` }
      },
      parseHTML: (element: { style: { borderRight: string } }) =>
        element.style.borderRight || null,
    },
    borderBottomValue: {
      default: null,
      renderHTML: (attributes: { borderBottomValue: string }) => {
        const value = attributes.borderBottomValue
        if (!value) return null
        return { style: `border-bottom: ${value}` }
      },
      parseHTML: (element: { style: { borderBottom: string } }) =>
        element.style.borderBottom || null,
    },
    borderLeftValue: {
      default: null,
      renderHTML: (attributes: { borderLeftValue: string }) => {
        const value = attributes.borderLeftValue
        if (!value) return null
        return { style: `border-left: ${value}` }
      },
      parseHTML: (element: { style: { borderLeft: string } }) =>
        element.style.borderLeft || null,
    },
    textAlignValue: {
      default: null,
      renderHTML: (attributes: { textAlignValue: string }) => {
        const value = attributes.textAlignValue
        if (!value) return null
        return { style: `vertical-align: ${value}` }
      },
    },
  }
}

/**
 * Erweitert die Standard-TableCell Extension und verwendet die getBorderAttributes Logik.
 */
export const CustomTableCell = TableCell.extend({
  addAttributes() {
    const parentAttributes = this.parent?.() || {}

    return {
      ...parentAttributes,
      ...getBorderAttributes(),
    }
  },
})
