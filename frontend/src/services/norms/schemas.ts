import { MetadatumType, MetadataSectionName, Norm } from "@/domain/norm"

export type NormListResponseSchema = {
  guid: string
  officialLongTitle: string
}[]

export interface NormResponseSchema extends Omit<Norm, "metadataSections"> {
  metadataSections: MetadataSectionSchema[]
}

export interface MetadatumSchema {
  type: MetadatumType
  value: string
  order: number
}

export interface MetadataSectionSchema {
  name: MetadataSectionName
  order: number
  metadata?: MetadatumSchema[] | null
  sections?: MetadataSectionSchema[] | null
}
