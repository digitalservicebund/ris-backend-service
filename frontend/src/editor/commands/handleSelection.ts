import { CommandProps } from "@tiptap/core"
import { TextSelection } from "prosemirror-state"

/**
 * Main command handler to select the entire border number node.
 * Solves the issue that a border number could only be copied correctly
 * when selecting the previous node, too.
 */
export function handleSelection({ state, dispatch }: CommandProps): boolean {
  const { from, to } = state.selection
  const schema = state.schema
  const borderNumberNodeType = schema.nodes.borderNumber

  const resolvedFrom = state.doc.resolve(from)

  const parentNode = resolvedFrom.node(resolvedFrom.depth - 1)
  const isSelection = from !== to

  if (
    isSelection &&
    parentNode &&
    parentNode.type === borderNumberNodeType &&
    dispatch
  ) {
    const startOfBorderNumber = resolvedFrom.start(resolvedFrom.depth - 1)

    const adjustedSelection = TextSelection.create(
      state.doc,
      startOfBorderNumber,
      to,
    )

    dispatch(state.tr.setSelection(adjustedSelection))

    return true
  }

  return false
}
