// src/shortcuts/backspaceShortcut.ts
import { Editor } from "@tiptap/core"

/**
 * Main handler for Backspace which removes borderNumber if needed.
 */
export function handleBackspace(editor: Editor): boolean {
  const { state } = editor
  const { selection } = state
  const { $from, $to } = selection

  const isCollapsedSelection = $from.pos === $to.pos
  const isBorderNumberContent =
    $from.node($from.depth - 1).type.name === "borderNumberContent"
  const isCursorAtStart = $from.parentOffset === 0
  const isBorderNumberNumber =
    $from.node($from.depth).type.name === "borderNumberNumber"

  // Check if the current paragraph is the first child of the borderNumberContent node
  const isFirstChild = $from.index($from.depth - 1) === 0

  if (
    (isCollapsedSelection &&
      isBorderNumberContent &&
      isFirstChild &&
      isCursorAtStart) ||
    isBorderNumberNumber
  ) {
    return editor.commands.removeBorderNumbers()
  }

  return false
}
