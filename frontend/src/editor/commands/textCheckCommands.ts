import { Editor } from "@tiptap/core"
import { EditorState } from "prosemirror-state"
import { Ref, ref } from "vue"
import { ResponseError, ServiceResponse } from "@/services/httpClient"
import languageToolService from "@/services/languageToolService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

import {
  Match,
  TextCheckCategoryResponse,
  TextCheckTagName,
} from "@/types/languagetool"

interface TextCheckService {
  loading: Ref<boolean>
  matches: Match[]
  selectedMatch: Ref<Match | undefined>
  responseError: Ref<ResponseError | undefined>

  checkCategory(editor: Editor, category?: string): Promise<void>

  handleSelection(state: EditorState): boolean

  setMatch(matchId?: number): void

  replaceMatch(
    matchId: number,
    text: string,
    state: EditorState,
    /* eslint-disable @typescript-eslint/no-explicit-any */
    dispatch: ((args?: any) => any) | undefined,
  ): void

  clearSelectedMatch(): void
}

class NeurisTextCheckService implements TextCheckService {
  loading = ref(false)
  matches: Match[] = []
  selectedMatch = ref()
  responseError = ref()

  public static readonly isTextCheckTagSelected = (editor: Editor): boolean => {
    const { selection } = editor.state
    const node = editor.state.doc.nodeAt(selection.from)

    if (node?.marks) {
      return node.marks.some((mark) => mark.type.name === TextCheckTagName)
    }

    return false
  }

  /**
   * Function to update the selected match by text check tags, will reset selected if not a text check tag
   * @param state
   */
  handleSelection = (state: EditorState): boolean => {
    const node = state.doc.nodeAt(state.selection.from)

    if (!node?.marks) {
      this.clearSelectedMatch()
      return false
    }

    const textCheckMark = node.marks.find(
      (mark) => mark.type.name === TextCheckTagName,
    )

    if (!textCheckMark) {
      this.clearSelectedMatch()
      return false
    }

    this.setMatch(Number(textCheckMark.attrs.id))
    return true
  }

  checkCategory = async (editor: Editor, category?: string) => {
    this.loading.value = true
    this.responseError.value = undefined

    const store = useDocumentUnitStore()

    if (store.documentUnit?.uuid == undefined) {
      return
    }
    await store.updateDocumentUnit()

    const languageToolCheckResponse: ServiceResponse<TextCheckCategoryResponse> =
      await languageToolService.checkCategory(
        store.documentUnit?.uuid,
        category,
      )

    if (languageToolCheckResponse.status == 200) {
      this.matches = languageToolCheckResponse.data!.matches
      this.loading.value = false

      editor.commands.setContent(languageToolCheckResponse.data!.htmlText)
    }
    if (languageToolCheckResponse.error) {
      this.responseError.value = languageToolCheckResponse.error
    }

    this.loading.value = false
  }

  /**
   * Received match id and the text to replace, find the mark with the id, remove the text-check html tag and set the text
   * @param matchId
   * @param text
   * @param state
   * @param dispatch
   */
  replaceMatch = (
    matchId: number,
    text: string,
    state: EditorState,
    /* eslint-disable @typescript-eslint/no-explicit-any */
    dispatch: ((args?: any) => any) | undefined,
  ) => {
    const { tr } = state

    state.doc.descendants((node, pos) => {
      if (
        node.isText &&
        node.marks.some(
          (mark) =>
            mark.type.name === TextCheckTagName &&
            mark.attrs.id === matchId.toString(),
        )
      ) {
        const textCheckMark = node.marks.find(
          (mark) =>
            mark.type.name === TextCheckTagName &&
            mark.attrs.id === matchId.toString(),
        )

        if (textCheckMark) {
          tr.delete(pos, pos + node.nodeSize).insert(
            pos,
            state.schema.text(text),
          )

          if (dispatch) {
            dispatch(tr)
          }
        }
      }

      this.clearSelectedMatch()
    })
  }

  setMatch = (matchId?: number) => {
    if (matchId) {
      const selectedMatch = this.matches.find((match) => match.id === matchId)
      if (selectedMatch) {
        this.selectedMatch.value = selectedMatch
      } else {
        this.clearSelectedMatch()
      }
    } else {
      this.clearSelectedMatch()
    }
  }

  clearSelectedMatch = () => {
    this.selectedMatch.value = undefined
  }

  clearMatches = () => {
    this.matches = []
  }
}

export { NeurisTextCheckService, TextCheckService }
