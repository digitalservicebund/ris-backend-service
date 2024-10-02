import { CommandProps } from "@tiptap/core"
import { NodeType } from "prosemirror-model"
import { TextSelection } from "prosemirror-state"
import { nextTick } from "vue"
import BorderNumberService from "@/services/borderNumberService"

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
  let numberOfAddedBorderNumbers = 0
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
      const numberNode = numberNodeType.create({}, schema.text("0"))
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

      const addedNodeSize = borderNumberNode.nodeSize - node.nodeSize

      // Adjust shift to account for the new node size
      shift += addedNodeSize

      // Update the newFrom and newTo based on the added node sizes
      if (pos < initialFrom) {
        updatedFrom += addedNodeSize
        updatedTo += addedNodeSize
      } else if (pos < initialTo) {
        updatedTo += addedNodeSize
      }

      // Increment the count of added borderNumbers
      numberOfAddedBorderNumbers++
      modified = true
    }
  })

  void nextTick().then(() => BorderNumberService.makeBorderNumbersSequential())

  // If changes were made, dispatch the transaction
  if (modified && dispatch) {
    console.log("---final--------")
    console.log("newFrom: ", updatedFrom)
    console.log("newTo: ", updatedTo)

    // Create a new selection inside the contentNode of borderNumber
    const textSelection = TextSelection.create(
      tr.doc,
      updatedFrom,
      updatedTo - numberOfAddedBorderNumbers,
    )

    // Set the new selection and dispatch the transaction
    tr.setSelection(textSelection)
    dispatch(tr)

    return true
  }

  return false
}
