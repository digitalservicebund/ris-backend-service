import { CommandProps } from "@tiptap/core"
import { Node as ProseMirrorNode } from "prosemirror-model"
import { Transaction, TextSelection } from "prosemirror-state"

/**
 * Main command handler to remove borderNumber nodes.
 */
function removeBorderNumbers({ state, dispatch }: CommandProps): boolean {
  const { selection, doc, tr } = state
  const { from, to } = selection

  const { isLastNodeEmpty, borderNumberPositions } = findBorderNumberPositions(
    doc,
    from,
    to,
  )

  const { modified, borderNumberSizes } = processBorderNumbers(
    tr,
    doc,
    borderNumberPositions,
  )

  if (modified && dispatch) {
    adjustSelection(
      tr,
      selection as TextSelection,
      borderNumberSizes,
      isLastNodeEmpty,
    )
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
): { isLastNodeEmpty: boolean; borderNumberPositions: number[] } {
  const borderNumberPositions: number[] = []
  let isLastNodeEmpty = false

  doc.nodesBetween(from, to, (node, pos) => {
    if (node.type.name === "borderNumber") {
      borderNumberPositions.push(pos)
    }
    isLastNodeEmpty = node.isTextblock && node.content.size === 0
  })

  return { isLastNodeEmpty, borderNumberPositions }
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
 * Calculates the adjusted starting position after processing borderNumber nodes, ensuring it's at least 1.
 */
function calculateFromPos(from: number, borderNumberSizes: number[]) {
  const result = from - borderNumberSizes[borderNumberSizes.length - 1]
  if (result <= 0) {
    return 1
  }
  return result
}

/**
 * Adjusts the document's selection based on the modified nodes.
 */
function adjustSelection(
  tr: Transaction,
  selection: TextSelection,
  borderNumberSizes: number[],
  isLastNodeEmpty: boolean,
): void {
  const { from, to } = selection
  const borderNumberSizeSum = sum(borderNumberSizes)
  const fromPos = calculateFromPos(from, borderNumberSizes)
  const toPos = to - borderNumberSizeSum - 2 * (borderNumberSizes.length - 1)

  tr.setSelection(
    TextSelection.create(tr.doc, fromPos, isLastNodeEmpty ? toPos - 1 : toPos),
  )
}

/**
 * Utility function to check if a node is empty.
 */
function isNodeEmpty(node: ProseMirrorNode): boolean {
  return node.textContent.length === 0
}

/**
 * Utility function to sum an array of numbers.
 */
function sum(numbers: number[]): number {
  return numbers.reduce((acc, size) => acc + size, 0)
}

export default removeBorderNumbers
