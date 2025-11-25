import { Table } from "@tiptap/extension-table"
import { Plugin, PluginKey } from "@tiptap/pm/state"
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

  addProseMirrorPlugins() {
    return [
      ...(this.parent?.() || []),
      new Plugin({
        key: new PluginKey("tableBorderPreservation"),
        props: {
          transformPastedHTML(html) {
            // Parse the pasted HTML
            const parser = new DOMParser()
            const doc = parser.parseFromString(html, "text/html")

            // Find all table cells and headers
            const tableCells = doc.querySelectorAll("td, th")

            tableCells.forEach((cell) => {
              const element = cell as HTMLElement
              const currentStyle = element.getAttribute("style") || ""

              // Check if the cell has any border styles
              const hasBorders = currentStyle.includes("border")

              if (hasBorders) {
                // Extract existing border styles
                const styleDeclarations = currentStyle.split(";")

                let hasTop = false
                let hasRight = false
                let hasBottom = false
                let hasLeft = false

                // Check which borders are explicitly defined
                styleDeclarations.forEach((declaration) => {
                  const trimmed = declaration.trim()
                  if (trimmed.startsWith("border-top:")) hasTop = true
                  if (trimmed.startsWith("border-right:")) hasRight = true
                  if (trimmed.startsWith("border-bottom:")) hasBottom = true
                  if (trimmed.startsWith("border-left:")) hasLeft = true
                  if (
                    trimmed.startsWith("border:") &&
                    !trimmed.startsWith("border-")
                  ) {
                    // Generic border property affects all sides
                    hasTop = hasRight = hasBottom = hasLeft = true
                  }
                })

                // If some borders are missing but at least one exists, restore all borders
                if (
                  (hasTop || hasRight || hasBottom || hasLeft) &&
                  !(hasTop && hasRight && hasBottom && hasLeft)
                ) {
                  // Find a border value to use as default
                  let defaultBorder = "1px solid black"

                  // Try to extract existing border value
                  const borderMatch = currentStyle.match(
                    /border(?:-\w+)?\s*:\s*([^;]+)/,
                  )
                  if (borderMatch) {
                    defaultBorder = borderMatch[1].trim()
                  }

                  // Build complete border styles
                  const otherStyles = styleDeclarations
                    .filter((s) => !s.trim().startsWith("border"))
                    .filter((s) => s.trim() !== "")

                  // Add all four borders explicitly
                  const newStyle = [
                    ...otherStyles,
                    `border-top: ${defaultBorder}`,
                    `border-right: ${defaultBorder}`,
                    `border-bottom: ${defaultBorder}`,
                    `border-left: ${defaultBorder}`,
                  ].join("; ")

                  element.setAttribute("style", newStyle)
                }
              }
            })

            return doc.body.innerHTML
          },
        },
      }),
    ]
  },
})
