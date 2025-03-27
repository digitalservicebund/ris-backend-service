import DocumentationOffice from "@/domain/documentationOffice"
import { TextCheckService } from "@/editor/commands/textCheckCommands"

export interface Replacement {
  value: string
}

export interface Context {
  text: string
  offset: number
  length: number
}

export interface Type {
  typeName: string
}

export interface Category {
  id: string
  name: string
}

export interface Rule {
  id: string
  description: string
  issueType: string
  category: Category
}

export interface Match {
  id: number
  message: string
  shortMessage: string
  replacements: Replacement[]
  offset: number
  htmlOffset?: number
  length: number
  context: Context
  sentence: string
  type: Type
  rule: Rule
  ignoreForIncompleteSentence: boolean
  contextForSureMatch: number
  word: string
  category: string
  ignoredWord?: IgnoredTextCheckWord
}

export interface TextCheckResponse {
  matches: Match[]
}

export interface TextCheckAllResponse {
  suggestions: Suggestion[]
  categoryTypes: string[]
  totalTextCheckErrors: number
}

export interface TextCheckCategoryResponse {
  htmlText: string
  matches: Match[]
}

export interface Suggestion {
  word: string
  matches: Match[]
}

export interface IgnoredTextCheckWord {
  id?: string
  word: string
  documentationOffice: DocumentationOffice
}

export type TextCheckExtensionOptions = {
  category?: string
  service?: TextCheckService
}

export const TextCheckTagName = "textCheck"
