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
    if (!node.children) return
    for (const childId of node.children) {
      this.traverse(this.nodes[childId], orderedNodes)
    }
  }

  public getOrderedNodes() {
    const orderedNodes: SubjectNode[] = []
    this.traverse(this.root, orderedNodes)
    return orderedNodes
  }
}

export type SubjectNode = {
  id: string
  stext: string
  parent?: string
  children?: string[]
  depth?: number
  isExpanded: boolean
}

export const mockSubjectNodes: SubjectNode[] = [
  {
    id: "root",
    stext: "Alle Sachgebiete anzeigen",
    parent: undefined,
    children: ["01-01", "01-02"],
    depth: 0,
    isExpanded: false,
  },
  {
    id: "01-01",
    stext: "Text123",
    parent: "root",
    children: [],
    depth: 1,
    isExpanded: false,
  },
  {
    id: "01-02",
    stext: "Text456",
    parent: "root",
    children: ["02-01"],
    depth: 1,
    isExpanded: false,
  },
  {
    id: "02-01",
    stext: "Text789",
    parent: "01-02",
    children: [],
    depth: 2,
    isExpanded: false,
  },
]
