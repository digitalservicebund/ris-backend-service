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
  category?: string
  service?: TextCheckService
}

export interface TextCheckService {
  loading: Ref<boolean>
  matches: Match[]
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

  ignoreWordGlobally(word: string): Promise<boolean>

  removeGloballyIgnoredWord(word: string): Promise<boolean>

  toggleMatchIgnoredStatus(
    matchId: number,
    isIgnored: boolean,
    state: EditorState,
    /* eslint-disable @typescript-eslint/no-explicit-any */
    dispatch: ((args?: any) => any) | undefined,
  ): void
}

export const TextCheckTagName = "textCheck"
