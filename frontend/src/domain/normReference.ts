import { NormAbbreviation } from "./normAbbreviation"

export type NormReference = {
  normAbbreviation?: NormAbbreviation
  singleNorm?: string
  dateOfVersion?: string
  dateOfRelevance?: string
}

export default NormReference
