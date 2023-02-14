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
    if (!node.isLeaf && node.children.length === 0) {
      SubjectsService.getChildrenOf(node.subjectFieldNumber).then(
        (response) => {
          if (!response.data) return
          node.children = response.data
        }
      )
    }
    node.isExpanded = !node.isExpanded
  }

  private expandTraversal(node: SubjectNode) {
    node.isExpanded = true
    for (const child of node.children) {
      this.expandTraversal(child)
    }
  }

  public expandAll() {
    this.expandTraversal(this.root)
  }
}

export type SubjectNode = {
  subjectFieldNumber: string
  subjectFieldText: string
  // parent?: string
  children: SubjectNode[]
  depth: number
  isExpanded: boolean
  isLeaf: boolean
}

export function buildRoot(children: SubjectNode[] = []): SubjectNode {
  return {
    subjectFieldNumber: "root",
    subjectFieldText: "Alle Sachgebiete anzeigen",
    children: children,
    depth: 0,
    isExpanded: false,
    isLeaf: false,
  }
}
