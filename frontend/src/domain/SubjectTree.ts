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

  public getNodesOrderedByDepthFirstSearch() {
    const orderedNodes: SubjectNode[] = []
    this.traverse(this.root, orderedNodes)
    return orderedNodes
  }

  public toggleNode(node: SubjectNode) {
    if (!node.isLeaf && (node.children.length === 0 || node.inDirectPathMode)) {
      SubjectsService.getChildrenOf(node.subjectFieldNumber).then(
        (response) => {
          if (!response.data) return
          node.children = response.data
        }
      )
    }
    if (node.inDirectPathMode) {
      node.inDirectPathMode = false
    } else {
      node.isExpanded = !node.isExpanded
    }
  }

  private expandTraversal(node: SubjectNode, directPathMode: boolean) {
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

export type SubjectNode = {
  subjectFieldNumber: string
  subjectFieldText: string
  linkedFields?: string[]
  children: SubjectNode[]
  depth: number
  isExpanded: boolean
  inDirectPathMode?: boolean
  isLeaf: boolean
}

export const ROOT_ID = "root"

export function buildRoot(children: SubjectNode[] = []): SubjectNode {
  return {
    subjectFieldNumber: ROOT_ID,
    subjectFieldText: "Alle Sachgebiete anzeigen",
    children: children,
    depth: 0,
    isExpanded: false,
    isLeaf: false,
  }
}
