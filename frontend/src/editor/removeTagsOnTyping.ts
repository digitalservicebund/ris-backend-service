import { EditorState, Plugin, PluginKey, Transaction } from "prosemirror-state"
import { TextCheckTagName } from "@/types/textCheck"

// Plugin to remove HTML tags, Marks in roseMirror lingo, from text
// that is being edited.

const removeTagsOnTypingKey = new PluginKey("removeTagsOnTyping")

export const removeTagsOnTypingPlugin = new Plugin({
  key: removeTagsOnTypingKey,
  appendTransaction: (
    transactions: readonly Transaction[],
    oldState: EditorState,
    newState: EditorState,
  ) => {
    const { schema } = newState
    const markType = schema.marks[TextCheckTagName]

    if (!markType) return null

    const isDocumentChanged = transactions.some(
      (transaction) => transaction.docChanged,
    )
    if (!isDocumentChanged) {
      return null
    }

    let modified = false
    const newStateTransaction = newState.tr

    transactions.forEach((transaction) => {
      const { from, to } = transaction.selection
      let oldNodeText: string | undefined = ""

      if (!oldState?.doc) return

      // If the difference between newState and oldState is greater than 1 char, then it is
      // not user typing text and this plugin should not care about it.
      const testSizeDifference = Math.abs(
        oldState.doc.nodeSize - newState.doc.nodeSize,
      )
      if (testSizeDifference > 1) return

      // Check if we can query the old state with from and to
      // if not skip it altogether.
      const min = 0
      const max = oldState.doc.content.size
      const safeFrom = Math.max(min, from - 1)
      const safeTo = Math.min(max, to + 1)

      if (safeFrom <= safeTo) {
        oldState.doc.nodesBetween(safeFrom, safeTo, (node) => {
          oldNodeText = node.text
        })
      }

      newState.doc.nodesBetween(from - 1, to + 1, (node, pos) => {
        if (!node.isText) return
        if (node.text == oldNodeText) return

        const hasMark = markType.isInSet(node.marks)

        if (hasMark) {
          newStateTransaction.removeMark(pos, pos + node.nodeSize, markType)
          modified = true
        }
      })
    })

    return modified ? newStateTransaction : null
  },
})
