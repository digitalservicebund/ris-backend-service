import { Operation } from "fast-json-patch"

export type RisJsonPatch = {
  documentationUnitVersion: number
  patch: Operation[]
  errorPaths: string[]
}
