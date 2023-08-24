import {
  MetadatumType,
  MetadataSectionName,
  FlatMetadata,
  Norm,
} from "@/domain/norm"

type NullableType<Type> = {
  [Property in keyof Type]: Type[Property] | null
}

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

export type FlatMetadataRequestSchema = NullableType<FlatMetadata>
