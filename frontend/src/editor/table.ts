import { Table, TableView } from "@tiptap/extension-table"
import "../styles/tables.css"
import { Node } from "prosemirror-model"
import { hasAllBorders } from "./tableUtil"

/**
 * Notwendig, weil das resizable: true die styles (border) der Tabelle entfernt.
 */
class CustomTableView extends TableView {
  constructor(node: Node, cellMinWidth: number) {
    super(node, cellMinWidth)
    this.applyAttributes()
  }

  applyAttributes() {
    Object.entries(this.node.attrs).forEach(([key, value]) => {
      if (value == null) {
        this.table.removeAttribute(key)
      } else {
        this.table.setAttribute(key, String(value))
      }
    })
  }

  update(node: Node) {
    super.update(node)
    this.applyAttributes()
    return true
  }
}

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
      View: CustomTableView,
      resizable: true,
      allowTableNodeSelection: true,
    }
  },
})
