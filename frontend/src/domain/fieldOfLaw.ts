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

export function getDescendants(node: FieldOfLawNode) {
  const collect: FieldOfLawNode[] = []
  const collectRecursively = (
    node: FieldOfLawNode,
    collect: FieldOfLawNode[]
  ) => {
    collect.push(node)
    node.children.forEach((child) => collectRecursively(child, collect))
  }
  collectRecursively(node, collect)
  return collect
}
