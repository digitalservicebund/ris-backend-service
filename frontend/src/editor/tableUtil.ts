import { CommandProps } from "@tiptap/core"
import { Node as ProseMirrorNode } from "@tiptap/pm/model"
import { EditorState, Transaction } from "@tiptap/pm/state"
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

  if (sel instanceof CellSelection) {
    return sel
  }

  // Cursor inside a cell
  const $pos = sel.$anchor

  for (let depth = $pos.depth; depth > 0; depth--) {
    const node = $pos.node(depth)
    if (!node) continue

    const nodeName = node.type?.name
    if (nodeName === "tableCell" || nodeName === "tableHeader") {
      const cellPos = $pos.before(depth)
      const $cell = state.doc.resolve(cellPos)

      // Always use the resolved cell node, not $pos
      return new CellSelection($cell, $cell)
    }
  }
  return null
}

// --- Cell Processing Helpers ---

function processClearCellBorders(
  node: ProseMirrorNode,
  pos: number,
  tr: Transaction,
): void {
  const cleanedStyles = (node.attrs.style || "")
    .split(";")
    .map((s: string) => s.trim())
    .filter(Boolean)
    .filter((s: string) => !s.startsWith("border"))

  tr.setNodeMarkup(pos, undefined, {
    ...node.attrs,
    style: cleanedStyles.join("; "),
  })
}

function processSetBorder(
  node: ProseMirrorNode,
  pos: number,
  tr: Transaction,
  side: BorderSide,
  value: string,
): void {
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
}

function processSetBorderAll(
  node: ProseMirrorNode,
  pos: number,
  tr: Transaction,
  value: string,
): void {
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
}

function processSetVerticalAlign(
  node: ProseMirrorNode,
  pos: number,
  tr: Transaction,
  value: VerticalAlign,
): void {
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
          processClearCellBorders(node, pos, tr)
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
          processSetBorder(node, pos, tr, side, value)
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
          processSetBorderAll(node, pos, tr, value)
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
          processSetVerticalAlign(node, pos, tr, value)
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
  const s = style.replaceAll(/\s+/g, "")

  // Check for explicit border-side declarations
  const hasExplicitBorders =
    s.includes("border-top:") &&
    s.includes("border-right:") &&
    s.includes("border-bottom:") &&
    s.includes("border-left:")

  // Check for shorthand border declaration that applies to all sides
  const hasShorthandBorder =
    /border:\s*[^;]*(?:solid|dashed|dotted|double)/i.test(style)

  return hasExplicitBorders || hasShorthandBorder
}
