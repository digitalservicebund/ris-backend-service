import {
  MetadatumType,
  MetadataSectionName,
  Norm,
  DocumentSectionType,
} from "@/domain/norm"

export type NormListResponseSchema = {
  guid: string
  officialLongTitle: string
}[]

export interface NormResponseSchema
  extends Omit<Norm, "metadataSections" | "documentation"> {
  metadataSections: MetadataSectionSchema[]
  documentation: (ArticleSchema | DocumentSectionSchema)[]
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

export interface DocumentationSchema {
  readonly guid: string
  heading?: string
  marker: string
  order: number
}

export type DocumentSectionSchema = DocumentationSchema & {
  heading: string
  documentation?: (ArticleSchema | DocumentSectionSchema)[]
  type: DocumentSectionType
}

export type ArticleSchema = DocumentationSchema & {
  paragraphs: ParagraphSchema[]
}

export interface ParagraphSchema {
  guid: string
  marker: string
  text: string
}
