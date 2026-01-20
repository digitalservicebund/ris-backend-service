import { Table, TableOptions, TableView } from "@tiptap/extension-table"
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
}

/**
 * Erweitert die Tiptap Table Extension, um die Logik für die 'invisible-table-cell'-Klasse
 * auf Tabellenknoten, Header und Zellen anzuwenden.
 */
export const CustomTable = Table.extend({
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
  addGlobalAttributes() {
    return [
      {
        // apply to table container + cells + headers
        types: ["tableCell", "tableHeader"],
        attributes: {
          style: {
            parseHTML: (element) => {
              const table = element.closest("table")
              const oldBorder = table?.getAttribute("border") ?? undefined

              const existingStyle = element.getAttribute("style") || ""

              if (oldBorder && !existingStyle.includes("border")) {
                element.style.border = `${oldBorder}px solid rgb(0, 0, 0)`
              }

              return element.style.cssText
            },
            renderHTML: (attributes) => {
              const existingStyle = attributes.style || ""

              const allBorders = hasAllBorders(existingStyle)
              const invisibleClass = allBorders ? "" : "invisible-table-cell"

              attributes.style = existingStyle

              return {
                class: invisibleClass,
                style: existingStyle,
              }
            },
          },
          valign: {
            renderHTML: (attributes) => {
              let existingStyle = attributes.style || ""

              if (attributes.valign) {
                existingStyle += existingStyle == "" ? "" : "; "
                existingStyle += "vertical-align: " + attributes.valign + ";"
              }

              attributes.style = existingStyle
              attributes.valign = null

              return {
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
      ...(this.parent?.() as TableOptions),
      View: CustomTableView,
      resizable: true,
      allowTableNodeSelection: true,
    }
  },
})
