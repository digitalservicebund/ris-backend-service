import { CommandProps } from "@tiptap/core"
import { NodeType } from "prosemirror-model"
import { TextSelection } from "prosemirror-state"

/**
 * Main command handler to add borderNumber nodes (with default value "0").
 */
export function addBorderNumbers({ state, dispatch }: CommandProps): boolean {
  const { tr, selection, schema } = state
  const { from: initialFrom, to: initialTo } = selection

  const paragraphNodeType: NodeType = schema.nodes.paragraph
  const borderNumberNodeType: NodeType = schema.nodes.borderNumber
  const numberNodeType: NodeType = schema.nodes.borderNumberNumber
  const contentNodeType: NodeType = schema.nodes.borderNumberContent

  let modified = false
  let shift = 0 // This will track the shift in positions due to node insertions

  let updatedFrom = initialFrom
  let updatedTo = initialTo
  console.log("initialFrom: ", initialFrom)
  console.log("initialTo: ", initialTo)

  // Loop through the selected range and process paragraph nodes
  state.doc.nodesBetween(initialFrom, initialTo, (node, pos) => {
    const isBorderNumberContent =
      state.doc.resolve(pos).parent.type === contentNodeType

    if (isBorderNumberContent) {
      return
    }
    const isParagraphWithContent =
      node.type === paragraphNodeType && node.content.size != 0

    if (isParagraphWithContent) {
      const numberNode = numberNodeType.create({}, schema.text("1"))
      const contentNode = contentNodeType.create({}, node.content)
      const borderNumberNode = borderNumberNodeType.create({}, [
        numberNode,
        contentNode,
      ])

      // Calculate the new position considering the current shift
      const currentPos = pos + shift
      console.log("pos: ", pos)
      console.log("shift: ", shift)
      console.log("currentPos: ", currentPos)
      console.log("currentPos + nodeSize: ", currentPos + node.nodeSize)

      // Replace the paragraph node with the <border-number> node
      tr.replaceWith(currentPos, currentPos + node.nodeSize, borderNumberNode)

      // Track the size difference caused by the replacement
      const addedNodeSize = borderNumberNode.nodeSize - node.nodeSize - 1
      shift += addedNodeSize

      // Update the newFrom and newTo based on the added node sizes
      if (pos < initialFrom) {
        updatedFrom += addedNodeSize
        updatedTo += addedNodeSize
      } else if (pos < initialTo) {
        updatedTo += addedNodeSize
      }
      modified = true
    }
  })

  // If changes were made, dispatch the transaction
  if (modified && dispatch) {
    console.log("---final--------")
    console.log("newFrom: ", updatedFrom)
    console.log("newTo: ", updatedTo)
    // Adjust the selection to point inside the borderNumberContent node
    const resolvedNewFrom = tr.doc.resolve(updatedFrom)
    const resolvedNewTo = tr.doc.resolve(updatedTo)
    console.log("resolvedNewFrom: ", resolvedNewFrom)
    console.log("resolvedNewTo: ", resolvedNewTo)
    // Adjust selection to be inside the borderNumberContent node (first valid position)
    const contentPosFrom = resolvedNewFrom.start(resolvedNewFrom.depth)
    const contentPosTo = resolvedNewTo.start(resolvedNewTo.depth)
    console.log("contentPosFrom: ", contentPosFrom)
    console.log("contentPosTo: ", contentPosTo)

    // Create a new selection inside the contentNode of borderNumber
    const textSelection = TextSelection.create(
      tr.doc,
      resolvedNewFrom.pos,
      resolvedNewTo.pos,
    )

    // Set the new selection and dispatch the transaction
    tr.setSelection(textSelection)
    dispatch(tr)

    return true
  }

  return false
}
