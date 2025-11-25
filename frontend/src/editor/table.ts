import { Table } from "@tiptap/extension-table"
import "../styles/tables.css"
import { hasAllBorders } from "./tableUtil"

/**
 * Erweitert die Tiptap Table Extension, um die Logik für die 'invisible-table-cell'-Klasse
 * auf Tabellenknoten, Header und Zellen anzuwenden.
 */
export const CustomTable = Table.extend({
  addGlobalAttributes() {
    return [
      {
        // apply to table container + cells + headers
        types: ["table", "tableCell", "tableHeader"],
        attributes: {
          style: {
            renderHTML: (attributes) => {
              const existingStyle = attributes.style || ""

              const allBorders = hasAllBorders(existingStyle)
              const invisibleClass = allBorders ? "" : "invisible-table-cell"

              return {
                class: invisibleClass,
                style: existingStyle,
              }
            },
          },
        },
      },
      {
        types: ["tableRow"],
        attributes: {
          style: {
            renderHTML: (attributes) => {
              return {
                style: attributes.style,
              }
            },
          },
        },
      },
    ]
  },

  addOptions() {
    return {
      ...this.parent?.(),
      resizable: true,
      allowTableNodeSelection: true,
    }
  },
})
