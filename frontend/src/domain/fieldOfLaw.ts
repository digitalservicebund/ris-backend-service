export type Page<T> = {
  content: T[]
  size: number
  totalElements: number
  totalPages: number
  number: number
  numberOfElements: number
  first: boolean
  last: boolean
}

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
  childrenCount: number
  isExpanded: boolean
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
    text: "Alle Sachgebiete anzeigen",
    children: [],
    childrenCount: 17,
    norms: [],
    isExpanded: false,
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
