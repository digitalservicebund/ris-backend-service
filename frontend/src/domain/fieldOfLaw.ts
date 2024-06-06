export type Norm = {
  abbreviation: string
  singleNormDescription: string
}

export type FieldOfLaw = {
  identifier: string
  text: string
  linkedFields?: string[]
  norms: Norm[]
  children: FieldOfLaw[]
  parent?: FieldOfLaw
  hasChildren: boolean
}

export function buildRoot(): FieldOfLaw {
  return {
    identifier: "root",
    text: "Alle Sachgebiete",
    children: [],
    norms: [],
    hasChildren: true,
  }
}

export function createNode(identifier: string): FieldOfLaw {
  return {
    identifier: identifier,
    text: "",
    children: [],
    norms: [],
    hasChildren: false,
  }
}
