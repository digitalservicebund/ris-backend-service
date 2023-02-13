import SubjectsService from "@/services/subjectsService"

interface NodesMap {
  [key: string]: SubjectNode
}

export default class SubjectTree {
  public root: SubjectNode
  public nodes: NodesMap

  constructor(root: SubjectNode) {
    this.nodes = {}
    this.nodes["root"] = root
    this.root = this.nodes["root"]
  }

  // Depth-first search (recursion) fits exactly to the tree structure we want to display
  private traverse(node: SubjectNode, orderedNodes: SubjectNode[]) {
    orderedNodes.push(node)
    if (!node.children || !node.isExpanded) return
    for (const childId of node.children) {
      this.traverse(this.nodes[childId], orderedNodes)
    }
  }

  public getOrderedNodes() {
    const orderedNodes: SubjectNode[] = []
    this.traverse(this.root, orderedNodes)
    return orderedNodes
  }

  public toggleNode(nodeId: string) {
    const node = this.nodes[nodeId]
    if (!node.isLeaf && !node.children) {
      SubjectsService.getChildrenOf(nodeId).then((response) => {
        if (!response.data) return
        response.data.forEach((node) => (this.nodes[node.id] = node))
        node.children = response.data.map((node) => node.id)
      })
    }
    this.nodes[nodeId].isExpanded = !this.nodes[nodeId].isExpanded
  }
}

export type SubjectNode = {
  id: string
  stext: string
  // parent?: string
  children?: string[]
  depth: number
  isExpanded: boolean
  isLeaf: boolean
}
