import { EditorState, Plugin, PluginKey, Transaction } from "prosemirror-state"
import { ReplaceStep, Step } from "prosemirror-transform"
import { IgnoreOnceTagName } from "./ignoreOnceMark"
import { TextCheckTagName } from "@/types/textCheck"

// Plugin to remove HTML tags, Marks in ProseMirror lingo, from text
// that is being edited.

const removeTagsOnTypingKey = new PluginKey("removeTagsOnTyping")

const removeTagsOnTypingPlugin = new Plugin({
  key: removeTagsOnTypingKey,
  appendTransaction: (
    transactions: readonly Transaction[],
    oldState: EditorState,
    newState: EditorState,
  ) => {
    if (!isDocumentChanged(transactions, oldState, newState)) {
      return null
    }

    if (!hasTextCheckMarksInDocument(newState)) {
      return null
    }

    let modified = false
    const newStateTransaction = newState.tr

    transactions.forEach((transaction) => {
      // If transaction has multiple steps,
      // likely some kind of a bulk operation
      // so skip processing.
      if (transaction.steps.length > 1 || transaction.steps.length === 0) {
        return
      }

      let { realFrom, realTo } = findCorrectPosition(transaction.steps[0])
      // Safety check -------------------------------------------
      // When using backspace on the last row with no characters,
      // then realFrom and realTo can be out of bounds and their
      // use in fetching a node will cause error and prevent a
      // deletion or in this case row deletion.
      if (realFrom === 0 && realTo === 0) {
        return
      } else if (
        realFrom === newState.doc.content.size - 1 &&
        newState.doc.content.size === realTo
      ) {
        // Last character deletion is a tricky case
        // because even though realFrom and realTo are valid and in range,
        // they find a node before last that is not a text node.
        // So I am forcing the position to be before last character
        // because then I find the last text node correctly.
        realFrom = newState.doc.content.size - 2
        realTo = realFrom
      } else if (
        newState.doc.content.size >= realFrom &&
        newState.doc.content.size >= realTo
      ) {
        // Normal case, all good
      } else if (
        newState.doc.content.size >= realFrom &&
        newState.doc.content.size <= realTo
      ) {
        // From is in the new document but to is not anymore.
        realTo = realFrom
      } else if (
        newState.doc.content.size <= realFrom &&
        newState.doc.content.size >= realTo
      ) {
        // To is in the new document but from is not anymore.
        realFrom = realTo
      } else {
        // Both from and to are out of bounds, skip.
        return
      }

      newState.doc.nodesBetween(realFrom, realTo, (node, pos) => {
        if (node.isText) {
          newStateTransaction.removeMark(
            pos,
            pos + node.nodeSize,
            newState.schema.marks[TextCheckTagName],
          )
          newStateTransaction.removeMark(
            pos,
            pos + node.nodeSize,
            newState.schema.marks[IgnoreOnceTagName],
          )
          modified = true
        }
      })
    })

    return modified ? newStateTransaction : null
  },
})

function findCorrectPosition(step: Step): { realFrom: number; realTo: number } {
  if (!(step instanceof ReplaceStep)) {
    return { realFrom: 0, realTo: 0 }
  }

  const stepFrom = step.from
  const stepTo = step.to
  const deletedSize = stepTo - stepFrom
  const insertedSize = step.slice.size

  const realFrom = stepFrom
  let realTo = 0
  if (deletedSize === 0 && insertedSize > 0) {
    // Insertion
    realTo = stepFrom + insertedSize
  } else if (deletedSize > 0 && insertedSize === 0) {
    // Deletion
    realTo = stepTo
  } else {
    // Replacement
    realTo = stepTo + insertedSize
  }

  return { realFrom, realTo }
}

function isDocumentChanged(
  transactions: readonly Transaction[],
  oldState: EditorState,
  newState: EditorState,
): boolean {
  const isChanged = transactions.some((transaction) => transaction.docChanged)
  // console.log("isChanged:", isChanged)

  const sizeDiff = Math.abs(
    newState.doc.content.size - oldState.doc.content.size,
  )

  return isChanged || sizeDiff > 0
}

function hasTextCheckMarksInDocument(state: EditorState): boolean {
  const textCheckMark = state.schema.marks[TextCheckTagName]

  let hasCustomMarks = false

  state.doc.descendants((node) => {
    if (node.isText) {
      const hasTextCheck = textCheckMark?.isInSet(node.marks)

      if (hasTextCheck) {
        hasCustomMarks = true
      }
    }
  })

  return hasCustomMarks
}

export { removeTagsOnTypingPlugin }
