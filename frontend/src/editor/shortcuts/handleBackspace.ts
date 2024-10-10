import { Editor } from "@tiptap/core"
import { MarkType } from "prosemirror-model"
import { EditorState } from "prosemirror-state"

/**
 * Main handler for Backspace which removes borderNumber or borderNumberLink if needed.
 */
function handleBackspace(editor: Editor): boolean {
  const { state } = editor
  const { selection, tr } = state
  const { $from, $to } = selection

  const isCollapsedSelection = $from.pos === $to.pos
  const isBorderNumberContent =
    $from.node($from.depth - 1)?.type.name === "borderNumberContent"
  const isCursorAtStart = $from.parentOffset === 0
  const isBorderNumberNumber =
    $from.node($from.depth)?.type.name === "borderNumberNumber"

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

  const markType = editor.schema.marks.BorderNumberLink
  const hasBorderNumberLink = state.doc.rangeHasMark(
    $from.pos - 1,
    $from.pos,
    markType,
  )

  if (hasBorderNumberLink) {
    const start = calculateStart($from.pos, state, markType)
    const end = calculateEnd($from.pos, state, markType)

    tr.delete(start, end)
    editor.view.dispatch(tr)
    return true
  }

  return false
}

function calculateStart(start: number, state: EditorState, markType: MarkType) {
  while (start > 0 && state.doc.rangeHasMark(start - 1, start, markType)) {
    start--
  }
  return start
}

function calculateEnd(end: number, state: EditorState, markType: MarkType) {
  while (
    end < state.doc.content.size &&
    state.doc.rangeHasMark(end, end + 1, markType)
  ) {
    end++
  }
  return end
}

export default handleBackspace
