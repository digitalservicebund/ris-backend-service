import { CommandProps } from "@tiptap/core"
import { Node as ProseMirrorNode, NodeType } from "prosemirror-model"
import { Transaction, TextSelection } from "prosemirror-state"
import { nextTick } from "vue"
import BorderNumberService from "@/services/borderNumberService"

/**
 * Main command handler to remove borderNumber nodes.
 */
function removeBorderNumbers(
  { state, dispatch }: CommandProps,
  isFeatureEnabled: boolean,
): boolean {
  const { selection, doc, tr, schema } = state
  const { from, to } = selection
  const borderNumberNodeType: NodeType = schema.nodes.borderNumber

  const borderNumberPositions = findBorderNumberPositions(
    doc,
    from,
    to,
    borderNumberNodeType,
  )

  const { modified, borderNumberSizes } = processBorderNumbers(
    tr,
    doc,
    borderNumberPositions,
  )

  void nextTick().then(() => {
    if (isFeatureEnabled) {
      BorderNumberService.makeBorderNumbersSequential()
    }
  })

  if (modified && dispatch) {
    const totalSizeOfRemovedNodes = borderNumberSizes.reduce(
      (acc, size) => acc + size,
      0,
    )

    const selection = TextSelection.create(
      tr.doc,
      Math.max(1, from - totalSizeOfRemovedNodes),
    )
    tr.setSelection(selection)
    dispatch(tr)
    return true
  }

  return false
}

/**
 * Finds all the positions of `borderNumber` nodes between the selection range.
 */
function findBorderNumberPositions(
  doc: ProseMirrorNode,
  from: number,
  to: number,
  borderNumberType: NodeType,
): number[] {
  const borderNumberPositions: number[] = []

  doc.nodesBetween(from, to, (node, pos) => {
    if (node.type === borderNumberType) {
      borderNumberPositions.push(pos)
    }
  })

  return borderNumberPositions
}

/**
 * Processes each `borderNumber` node: either removes it if it's empty or replaces it with its content.
 */
function processBorderNumbers(
  tr: Transaction,
  doc: ProseMirrorNode,
  borderNumberPositions: number[],
): { modified: boolean; borderNumberSizes: number[] } {
  let modified = false
  const borderNumberSizes: number[] = []

  // Traverse in reverse order to avoid shifting positions
  borderNumberPositions.toReversed().forEach((pos) => {
    const borderNumberNode = doc.nodeAt(pos)

    if (borderNumberNode) {
      const contentNode = borderNumberNode.child(1)
      borderNumberSizes.push(borderNumberNode.nodeSize - contentNode.nodeSize)

      if (isNodeEmpty(contentNode)) {
        tr.delete(pos, pos + borderNumberNode.nodeSize)
      } else {
        tr.replaceWith(pos, pos + borderNumberNode.nodeSize, contentNode)
      }

      modified = true
    }
  })

  return { modified, borderNumberSizes }
}

/**
 * Utility function to check if a node is empty.
 */
function isNodeEmpty(node: ProseMirrorNode): boolean {
  return node.textContent.length === 0
}

export default removeBorderNumbers
