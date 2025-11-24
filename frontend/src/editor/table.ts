import { Table } from "@tiptap/extension-table"
import "../styles/tables.css"

/**
 * Erweitert die Tiptap Table Extension, um die Logik für die 'invisible-table'-Klasse
 * auf Tabellenknoten, Header und Zellen anzuwenden.
 */
export const CustomTable = Table.extend({
  addGlobalAttributes() {
    return [
      {
        types: ["table", "tableCell", "tableHeader"],
        attributes: {
          style: {
            renderHTML: (attributes) => {
              const existingStyle = attributes.style || ""

              // Split the style string into individual declarations
              const styles: string[] = existingStyle.split(";")

              const hasBorderStyle = styles.some((style) => {
                const trimmedStyle = style.trim()
                return (
                  trimmedStyle.startsWith("border") &&
                  !trimmedStyle.includes("none")
                )
              })

              const invisibleTableClass = hasBorderStyle
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
