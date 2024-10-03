import { CommandProps } from "@tiptap/core"
import { Dispatch } from "@tiptap/vue-3"
import {
  Node as ProsemirrorNode,
  NodeType,
  Schema as ProsemirrorSchema,
} from "prosemirror-model"
import { TextSelection, Transaction } from "prosemirror-state"
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
    if (isBorderNumberContent(state, pos, contentNodeType)) {
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

      if (pos < initialFrom) {
        updatedFrom += addedNodeSize
      }

      modified = true
    }
  })

  void nextTick().then(() => BorderNumberService.makeBorderNumbersSequential())

  handlePostModification(modified, dispatch, tr, updatedFrom)

  return modified
}

/**
 * Checks if the node at the given position is a border number content node.
 */
function isBorderNumberContent(
  state: CommandProps["state"],
  pos: number,
  contentNodeType: NodeType,
): boolean {
  return state.doc.resolve(pos).parent.type === contentNodeType
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
    {},
    node.content,
  )
  const numberNode = numberNodeType.create(
    {},
    borderNumberNodeType.schema.text("0"),
  )
  const contentNode = contentNodeType.create({}, paragraphNode)

  return borderNumberNodeType.create({}, [numberNode, contentNode])
}

/**
 * Handles actions that need to occur after modifying the document.
 */
function handlePostModification(
  modified: boolean,
  dispatch: Dispatch,
  tr: Transaction,
  updatedFrom: number,
): void {
  if (modified && dispatch) {
    const textSelection = TextSelection.create(tr.doc, updatedFrom - 2)
    tr.setSelection(textSelection)
    dispatch(tr)
  }
}

export default addBorderNumbers
