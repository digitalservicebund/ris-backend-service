import { Editor } from "@tiptap/core"
import { EditorState } from "prosemirror-state"
import { Ref } from "vue"
import { ResponseError } from "@/services/httpClient"

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
  ignoredTextCheckWords?: IgnoredTextCheckWord[]
  isIgnored?: boolean
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

export interface IgnoredTextCheckWordRequest {
  word: string
}

export interface IgnoredTextCheckWord {
  id?: string
  word: string
  type: DocumentationType
}

export type DocumentationType = "global" | "global_jdv" | "documentation_unit"

export type TextCheckExtensionOptions = {
  service?: TextCheckService
}

export interface TextCheckService {
  loading: Ref<boolean>
  selectedMatch: Ref<Match | undefined>
  responseError: Ref<ResponseError | undefined>

  checkCategory(editor: Editor, category?: string): Promise<void>

  handleSelection(state: EditorState): boolean

  selectMatch(matchId?: number): void

  replaceMatch(
    matchId: number,
    text: string,
    state: EditorState,
    /* eslint-disable @typescript-eslint/no-explicit-any */
    dispatch: ((args?: any) => any) | undefined,
  ): void

  clearSelectedMatch(): void

  ignoreWord(word: string): Promise<boolean>

  removeIgnoredWord(word: string): Promise<boolean>

  localIgnoreToggleHappened(): void

  ignoreWordGlobally(word: string): Promise<boolean>

  removeGloballyIgnoredWord(word: string): Promise<boolean>

  /**
   * Updates a match mark in text based on its ignored status
   */
  updateIgnoredMark(
    match: Match,
    state: EditorState,
    /* eslint-disable @typescript-eslint/no-explicit-any */
    dispatch: ((args?: any) => any) | undefined,
  ): void

  /**
   * Updates all matches marks in text based on their ignored status
   */
  updatedMatchesInText(
    state: EditorState,
    /* eslint-disable @typescript-eslint/no-explicit-any */
    dispatch: ((args?: any) => any) | undefined,
  ): void

  updatedMatchesInText(
    state: EditorState,
    dispatch: ((args?: any) => any) | undefined,
  ): void

  /**
   * Add an ignored word to all matching entries in the store
   */
  addIgnoredWordToMatches(ignoredTextCheckWord: IgnoredTextCheckWord): void

  /**
   * Remove ignored words of given type from all matching entries in the store
   */
  removeIgnoredWordFromMatches(
    word: string,
    ignoredType: DocumentationType,
  ): void
}

export const TextCheckTagName = "textCheck"
