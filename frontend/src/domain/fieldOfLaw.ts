export type Norm = {
  abbreviation: string
  singleNormDescription: string
}

export type FieldOfLawNode = {
  identifier: string
  text: string
  linkedFields?: string[]
  norms: Norm[]
  children: FieldOfLawNode[]
  parent?: FieldOfLawNode
  hasChildren: boolean
  isExpanded?: boolean
  inDirectPathMode?: boolean
}

export type FieldOfLawComboboxItem = {
  label: string
  text: string
}

export function buildRoot(): FieldOfLawNode {
  return {
    identifier: "root",
    text: "Alle Sachgebiete",
    children: [],
    norms: [],
    hasChildren: true,
    isExpanded: false,
  }
}

export function getDescendants(node: FieldOfLawNode) {
  const collect: FieldOfLawNode[] = []
  const collectRecursively = (
    node: FieldOfLawNode,
    collect: FieldOfLawNode[],
  ) => {
    collect.push(node)
    node.children.forEach((child) => collectRecursively(child, collect))
  }
  collectRecursively(node, collect)
  return collect
}
