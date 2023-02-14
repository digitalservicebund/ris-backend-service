import SubjectsService from "@/services/subjectsService"

export default class SubjectTree {
  public root: SubjectNode

  constructor(root: SubjectNode) {
    this.root = root
  }

  // Depth-first search (recursion) fits exactly to the tree structure we want to display
  private traverse(node: SubjectNode, orderedNodes: SubjectNode[]) {
    orderedNodes.push(node)
    if (!node.children || !node.isExpanded) return
    for (const child of node.children) {
      this.traverse(child, orderedNodes)
    }
  }

  public getOrderedNodes() {
    const orderedNodes: SubjectNode[] = []
    this.traverse(this.root, orderedNodes)
    return orderedNodes
  }

  public toggleNode(node: SubjectNode) {
    if (!node.isLeaf && !node.children) {
      SubjectsService.getChildrenOf(node.id).then((response) => {
        if (!response.data) return
        node.children = response.data
      })
    }
    node.isExpanded = !node.isExpanded
  }
}

export type SubjectNode = {
  id: string
  stext: string
  // parent?: string
  children?: SubjectNode[]
  depth: number
  isExpanded: boolean
  isLeaf: boolean
}
