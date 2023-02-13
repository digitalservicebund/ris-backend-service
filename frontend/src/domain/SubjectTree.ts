interface NodesMap {
  [key: string]: SubjectNode
}

export default class SubjectTree {
  public root: SubjectNode
  public nodes: NodesMap

  constructor(nodes: SubjectNode[]) {
    this.nodes = {}
    nodes.forEach((node) => (this.nodes[node.id] = node))
    this.root = this.nodes["root"]
  }

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
}

export const mockSubjectNodes: SubjectNode[] = [
  {
    id: "root",
    stext: "Alle Sachgebiete anzeigen",
    children: ["01-01", "01-02"],
    depth: 0,
    isExpanded: false,
  },
  {
    id: "01-01",
    stext: "Text123",
    children: [],
    depth: 1,
    isExpanded: false,
  },
  {
    id: "01-02",
    stext: "Text456",
    children: ["02-01"],
    depth: 1,
    isExpanded: false,
  },
  {
    id: "02-01",
    stext: "Text789",
    children: [],
    depth: 2,
    isExpanded: false,
  },
]
