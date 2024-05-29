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
  childrenCount: number
  readonly hasChildren: boolean
  isExpanded?: boolean
  inDirectPathMode?: boolean
}

export type FieldOfLawComboboxItem = {
  label: string
  text: string
}

export const ROOT_ID = "root"

export function buildRoot(): FieldOfLawNode {
  return {
    identifier: ROOT_ID,
    text: "Alle Sachgebiete",
    children: [],
    childrenCount: -1,
    norms: [],
    isExpanded: false,
    get hasChildren() {
      return this.children.length > 0
    },
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
