import { Table, TableOptions, TableView } from "@tiptap/extension-table"
import "../styles/tables.css"
import { Node } from "prosemirror-model"
import { hasAllBorders } from "./tableUtil"

let oldBorder: number | undefined = undefined
let resetReplacementTimeOut: NodeJS.Timeout
const resetOldBorder = () => {
  oldBorder = undefined
}

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
      border: {
        parseHTML: (element) => {
          const borderValue = element.getAttribute("border")
          if (borderValue) {
            oldBorder = Number.parseInt(borderValue) || undefined
          }
          resetReplacementTimeOut = setTimeout(resetOldBorder, 2000)
        },
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
            renderHTML: (attributes) => {
              clearTimeout(resetReplacementTimeOut)
              resetReplacementTimeOut = setTimeout(resetOldBorder, 2000)

              let existingStyle = attributes.style || ""

              if (oldBorder && !existingStyle.includes("border")) {
                existingStyle += existingStyle == "" ? "" : "; "
                existingStyle +=
                  "border: " + oldBorder + "px solid rgb(0, 0, 0)"
              }

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
