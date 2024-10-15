import { CommandProps } from "@tiptap/core"
import { Node as ProsemirrorNode, Fragment } from "prosemirror-model"
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
  const numberNodeType = schema.nodes.borderNumberNumber
  const contentNodeType = schema.nodes.borderNumberContent
  const borderNumberNodeType = schema.nodes.borderNumber

  let modified = false

  const nodesWithoutBorderNumbers: { node: ProsemirrorNode; pos: number }[] = []
  state.doc.nodesBetween(initialFrom, initialTo, (node, pos, parent) => {
    const isTopLevelNode = parent && parent.type === state.schema.topNodeType

    const isBorderNumberNode =
      node.type === borderNumberNodeType || node.hasMarkup(borderNumberNodeType)

    const isEmptyParagraph =
      node.type === paragraphNodeType &&
      node.childCount === 0 &&
      node.textContent?.trim() === ""

    if (!isTopLevelNode || isBorderNumberNode || isEmptyParagraph) return

    nodesWithoutBorderNumbers.push({ node, pos })
  })

  const reversedNodes = [...nodesWithoutBorderNumbers].reverse()
  for (const { node, pos } of reversedNodes) {
    const numberNode = numberNodeType.create(
      null,
      numberNodeType.schema.text("0"),
    )
    const contentNode = contentNodeType.create(null, Fragment.from(node))
    const borderNumberNode = borderNumberNodeType.create(null, [
      numberNode,
      contentNode,
    ])

    const nodeStart = pos
    const nodeEnd = pos + node.nodeSize

    tr.replaceWith(nodeStart, nodeEnd, borderNumberNode)

    modified = true
  }

  void nextTick().then(() => BorderNumberService.makeBorderNumbersSequential())

  if (modified && dispatch) {
    dispatch(tr)
  }
  return modified
}

export default addBorderNumbers
