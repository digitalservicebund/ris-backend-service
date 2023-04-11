import { MetadatumType, MetadataSectionName } from "@/domain/Norm"

export interface MetadatumSchema {
  type: MetadatumType
  value: string
  order: number
}

export interface MetadataSectionSchema {
  name: MetadataSectionName
  order: number
  metadata?: MetadatumSchema[]
  sections?: MetadataSectionSchema[] | null
}
