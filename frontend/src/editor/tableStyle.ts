import { Extension } from "@tiptap/core"
import "../styles/tables.css"

export const TableStyle = Extension.create({
  addGlobalAttributes() {
    return [
      {
        types: ["table", "tableCell", "tableHeader"],
        attributes: {
          style: {
            renderHTML: (attributes: { style?: string }) => {
              const existingStyle = attributes.style || ""
              const invisibleTableClass =
                existingStyle.includes("border") ||
                existingStyle.includes("border-bottom") ||
                existingStyle.includes("border-top") ||
                existingStyle.includes("border-right") ||
                existingStyle.includes("border-left")
                  ? ""
                  : "invisible-table"

              return {
                class: invisibleTableClass,
                style: existingStyle,
              }
            },
          },
        },
      },
    ]
  },
})
