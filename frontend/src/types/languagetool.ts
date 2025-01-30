import LanguageTool from "@/editor/languagetool/languageTool"

export interface Software {
  name: string
  version: string
  buildDate: string
  apiVersion: number
  premium: boolean
  premiumHint: string
  status: string
}

export interface Warnings {
  incompleteResults: boolean
}

export interface DetectedLanguage {
  name: string
  code: string
  confidence: number
}

export interface Language {
  name: string
  code: string
  detectedLanguage: DetectedLanguage
}

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
  message: string
  shortMessage: string
  replacements: Replacement[]
  offset: number
  length: number
  context: Context
  sentence: string
  type: Type
  rule: Rule
  ignoreForIncompleteSentence: boolean
  contextForSureMatch: number
  content: string
  word: string
}

export interface TextCheckResponse {
  matches: Match[]
}

export interface TextCheckAllResponse {
  suggestions: Suggestion[]
}

export interface Suggestion {
  word: string
  matches: Match[]
}

export interface TextNodesWithPosition {
  text: string
  from: number
  to: number
}

export interface LanguageToolOptions {
  language: string
  automaticMode: boolean
  documentId: string | number | undefined
  textToolEnabled: boolean
}

export interface LanguageToolStorage {
  loading?: boolean
  matchRange?: { from: number; to: number }
  languageToolService?: LanguageTool
}

export enum LanguageToolHelpingWords {
  LanguageToolTransactionName = "languageToolTransaction",
  MatchUpdatedTransactionName = "matchUpdated",
  MatchRangeUpdatedTransactionName = "matchRangeUpdated",
  LoadingTransactionName = "languageToolLoading",
}
