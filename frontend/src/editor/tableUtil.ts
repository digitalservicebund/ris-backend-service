import { CommandProps } from "@tiptap/core"
import { Node as ProseMirrorNode } from "@tiptap/pm/model"
import { EditorState } from "@tiptap/pm/state"
import { CellSelection } from "@tiptap/pm/tables"

type BorderSide = "top" | "right" | "bottom" | "left"
type VerticalAlign = "top" | "middle" | "bottom"

declare module "@tiptap/core" {
  interface Commands<ReturnType> {
    tableCellBorders: {
      clearCellBorders: () => ReturnType
      setBorder: (side: BorderSide, value: string) => ReturnType
      setBorderAll: (value: string) => ReturnType
      setVerticalAlign: (value: VerticalAlign) => ReturnType
    }
  }
}

/**
 * Convert any selection (cursor inside a cell or real CellSelection)
 * into a proper CellSelection.
 */
function getCellSelection(state: EditorState): CellSelection | null {
  const sel = state.selection

  // Already a valid CellSelection
  if (sel instanceof CellSelection) {
    return sel
  }

  // Cursor inside a cell
  const $pos = sel.$anchor

  for (let depth = $pos.depth; depth > 0; depth--) {
    const node = $pos.node(depth)
    if (!node) continue

    // Check if this depth is a table cell or table header
    const nodeName = node.type?.name
    if (nodeName === "tableCell" || nodeName === "tableHeader") {
      // Correct: cell position, not cursor position!
      const cellPos = $pos.before(depth)
      const $cell = state.doc.resolve(cellPos)

      // Correct: always use the resolved cell node
      return new CellSelection($cell, $cell)
    }
  }

  // No cell found
  return null
}

/**
 * Shared command implementations for tableCell + tableHeader.
 */
export function createCellCommands() {
  return {
    clearCellBorders:
      () =>
      ({ tr, state, dispatch }: CommandProps): boolean => {
        const selection = getCellSelection(state)
        if (!selection) return false

        selection.forEachCell((node: ProseMirrorNode, pos: number) => {
          const cleanedStyle = (node.attrs.style || "")
            .split(";")
            .map((s: string) => s.trim())
            .filter(Boolean)
            .filter((s: string) => !s.startsWith("border"))
            .join("; ")

          tr.setNodeMarkup(pos, undefined, {
            ...node.attrs,
            style: cleanedStyle,
          })
        })

        if (tr.docChanged && dispatch) {
          dispatch(tr)
          return true
        }

        return false
      },

    setBorder:
      (side: BorderSide, value: string) =>
      ({ tr, state, dispatch }: CommandProps): boolean => {
        const selection = getCellSelection(state)
        if (!selection) return false

        selection.forEachCell((node: ProseMirrorNode, pos: number) => {
          const styles = (node.attrs.style || "")
            .split(";")
            .map((s: string) => s.trim())
            .filter(Boolean)
            .filter((s: string) => !s.startsWith(`border-${side}`))

          styles.push(`border-${side}: ${value}`)

          tr.setNodeMarkup(pos, undefined, {
            ...node.attrs,
            style: styles.join("; "),
          })
        })

        if (tr.docChanged && dispatch) {
          dispatch(tr)
          return true
        }

        return false
      },

    setBorderAll:
      (value: string) =>
      ({ tr, state, dispatch }: CommandProps): boolean => {
        const selection = getCellSelection(state)
        if (!selection) return false

        selection.forEachCell((node: ProseMirrorNode, pos: number) => {
          const styles = (node.attrs.style || "")
            .split(";")
            .map((s: string) => s.trim())
            .filter(Boolean)
            .filter((s: string) => !s.startsWith("border"))

          styles.push(
            `border-top: ${value}`,
            `border-right: ${value}`,
            `border-bottom: ${value}`,
            `border-left: ${value}`,
          )

          tr.setNodeMarkup(pos, undefined, {
            ...node.attrs,
            style: styles.join("; "),
          })
        })

        if (tr.docChanged && dispatch) {
          dispatch(tr)
          return true
        }

        return false
      },

    setVerticalAlign:
      (value: VerticalAlign) =>
      ({ tr, state, dispatch }: CommandProps): boolean => {
        const selection = getCellSelection(state)
        if (!selection) return false

        selection.forEachCell((node: ProseMirrorNode, pos: number) => {
          const styles = (node.attrs.style || "")
            .split(";")
            .map((s: string) => s.trim())
            .filter(Boolean)
            .filter((s: string) => !s.startsWith("vertical-align"))

          styles.push(`vertical-align: ${value}`)

          tr.setNodeMarkup(pos, undefined, {
            ...node.attrs,
            style: styles.join("; "),
          })
        })

        if (tr.docChanged && dispatch) {
          dispatch(tr)
          return true
        }

        return false
      },
  }
}

export function hasAllBorders(style: string): boolean {
  const s = style.replace(/\s+/g, "")

  return (
    s.includes("border-top:") &&
    s.includes("border-right:") &&
    s.includes("border-bottom:") &&
    s.includes("border-left:")
  )
}
