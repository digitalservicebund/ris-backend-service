import { CommandProps } from "@tiptap/core"
import {
  Node as ProsemirrorNode,
  NodeType,
  Schema as ProsemirrorSchema,
} from "prosemirror-model"
import { TextSelection } from "prosemirror-state"
import { nextTick } from "vue"
import BorderNumberService from "@/services/borderNumberService"

/**
 * Main command handler to add borderNumber nodes (with default value "0").
 */
function addBorderNumbers({ state, dispatch }: CommandProps): boolean {
  const { tr, selection } = state
  const { from: initialFrom, to: initialTo } = selection
  const schema = state.schema

  const paragraphNodeType = schema.nodes.paragraph
  const contentNodeType = schema.nodes.borderNumberContent

  let modified = false
  let shift = 0
  let updatedFrom = initialFrom

  state.doc.nodesBetween(initialFrom, initialTo, (node, pos) => {
    if (hasBorderNumber(state, pos, schema)) {
      return
    }

    if (isParagraphWithContent(node, paragraphNodeType)) {
      const borderNumberNode = createBorderNumberNode(
        node,
        schema,
        contentNodeType,
      )
      const currentPos = pos + shift

      tr.replaceWith(currentPos, currentPos + node.nodeSize, borderNumberNode)

      const addedNodeSize = borderNumberNode.nodeSize - node.nodeSize
      shift += addedNodeSize

      const isFirstBorderNumber = pos < initialFrom
      if (isFirstBorderNumber) {
        /** This solution is not perfect. The tr.doc only contains the default "0".
        The recalculated border numbers are not part of the tr.doc, hence the actual number of digits
        (after the recalculation) is unknown and therefore the actual addedNodeSize. This might result in a
         slightly shifted cursor position **/
        updatedFrom = initialFrom + addedNodeSize
      }

      modified = true
    }
  })

  void nextTick().then(() => BorderNumberService.makeBorderNumbersSequential())

  if (modified && dispatch) {
    const textSelection = TextSelection.create(tr.doc, Math.max(1, updatedFrom))
    tr.setSelection(textSelection)
    dispatch(tr)
  }
  return modified
}

/**
 * Checks whether the node at the given position or its parent is a borderNumber or
 * borderNumberContent node, using the position to resolve the parent.
 *
 * @param state - The editor state.
 * @param pos - The position of the node in the document.
 * @param schema - The document schema to access node types.
 * @returns True if the node or its parent is a borderNumber or borderNumberContent node, false otherwise.
 */
function hasBorderNumber(
  state: CommandProps["state"],
  pos: number,
  schema: ProsemirrorSchema,
): boolean {
  const resolvedPos = state.doc.resolve(pos)
  const node = resolvedPos.nodeAfter
  const parent = resolvedPos.parent

  const borderNumberContentNodeType = schema.nodes.borderNumberContent
  const borderNumberNodeType = schema.nodes.borderNumber

  return (
    node?.type === borderNumberContentNodeType ||
    node?.type === borderNumberNodeType ||
    parent.type === borderNumberContentNodeType ||
    parent.type === borderNumberNodeType
  )
}

/**
 * Checks if the given node is a paragraph with content.
 */
function isParagraphWithContent(
  node: ProsemirrorNode,
  paragraphNodeType: NodeType,
): boolean {
  return node.type === paragraphNodeType && node.content.size !== 0
}

/**
 * Creates a border number node based on the provided parameters.
 */
function createBorderNumberNode(
  node: ProsemirrorNode,
  schema: ProsemirrorSchema,
  contentNodeType: NodeType,
): ProsemirrorNode {
  const borderNumberNodeType = schema.nodes.borderNumber
  const numberNodeType = schema.nodes.borderNumberNumber

  const paragraphNode = contentNodeType.schema.nodes.paragraph.create(
    node.attrs || {},
    node.content,
  )
  const numberNode = numberNodeType.create(
    {},
    borderNumberNodeType.schema.text("0"),
  )
  const contentNode = contentNodeType.create({}, paragraphNode)

  return borderNumberNodeType.create({}, [numberNode, contentNode])
}

export default addBorderNumbers
