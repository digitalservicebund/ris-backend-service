import FieldOfLawService from "@/services/fieldOfLawService"

export default class FieldOfLawTree {
  public root: FieldOfLawNode

  constructor(root: FieldOfLawNode) {
    this.root = root
  }

  // Depth-first search (recursion) fits exactly to the tree structure we want to display
  private traverse(node: FieldOfLawNode, orderedNodes: FieldOfLawNode[]) {
    orderedNodes.push(node)
    if (!node.children || !node.isExpanded) return
    for (const child of node.children) {
      this.traverse(child, orderedNodes)
    }
  }

  public getNodesOrderedByDepthFirstSearch() {
    const orderedNodes: FieldOfLawNode[] = []
    this.traverse(this.root, orderedNodes)
    return orderedNodes
  }

  public toggleNode(node: FieldOfLawNode) {
    if (!node.isLeaf && (node.children.length === 0 || node.inDirectPathMode)) {
      FieldOfLawService.getChildrenOf(node.identifier).then((response) => {
        if (!response.data) return
        node.children = response.data
      })
    }
    if (node.inDirectPathMode) {
      node.inDirectPathMode = false
    } else {
      node.isExpanded = !node.isExpanded
    }
  }

  private expandTraversal(node: FieldOfLawNode, directPathMode: boolean) {
    node.isExpanded = true
    node.inDirectPathMode = directPathMode
    for (const child of node.children) {
      this.expandTraversal(child, directPathMode)
    }
  }

  public expandAll(directPathMode = false) {
    this.expandTraversal(this.root, directPathMode)
  }
}

export type FieldOfLawNode = {
  identifier: string
  subjectFieldText: string
  linkedFields?: string[]
  children: FieldOfLawNode[]
  depth: number
  isExpanded: boolean
  inDirectPathMode?: boolean
  isLeaf: boolean
}

export const ROOT_ID = "root"

export function buildRoot(children: FieldOfLawNode[] = []): FieldOfLawNode {
  return {
    identifier: ROOT_ID,
    subjectFieldText: "Alle Sachgebiete anzeigen",
    children: children,
    depth: 0,
    isExpanded: false,
    isLeaf: false,
  }
}
