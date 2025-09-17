import { Editor } from "@tiptap/core"
import { EditorState } from "prosemirror-state"
import { ref } from "vue"
import { ServiceResponse } from "@/services/httpClient"
import languageToolService from "@/services/textCheckService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import {
  IgnoredTextCheckWord,
  Match,
  TextCheckCategoryResponse,
  TextCheckService,
  TextCheckTagName,
} from "@/types/textCheck"

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
   * Function to update the selected match by text check tags, will reset selected state if not a text check tag
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

    this.selectMatch(Number(textCheckMark.attrs.id))
    return true
  }

  /**
   * Performs a spell check on a given {@link category}. The documentation unit is saved beforehand
   * @param editor
   * @param category
   */
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
      editor.commands.setContent(languageToolCheckResponse.data!.htmlText, true)
    } else if (languageToolCheckResponse.error) {
      this.responseError.value = languageToolCheckResponse.error
    }

    this.loading.value = false
  }

  /**
   * Finds the match with the id, removes the text-check html tag and sets the given text
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

  /**
   * Updates the ignored status of a match by id
   * @param matchId match id to update status to
   * @param isIgnored the new status
   * @param state
   * @param dispatch
   */
  toggleMatchIgnoredStatus = (
    matchId: number,
    isIgnored: boolean,
    state: EditorState,
    dispatch: ((args?: any) => any) | undefined,
  ): void => {
    const { tr, schema } = state

    state.doc.descendants((node, pos) => {
      if (
        node.isText &&
        node.marks.some(
          (mark) =>
            mark.type.name === TextCheckTagName &&
            mark.attrs.id === matchId.toString(),
        )
      ) {
        const updatedMarks = node.marks.map((mark) => {
          if (
            mark.type.name === TextCheckTagName &&
            mark.attrs.id === matchId.toString()
          ) {
            return mark.type.create({
              ...mark.attrs,
              ignored: isIgnored,
            })
          }
          return mark
        })

        const updatedText = schema.text(node.text ?? "", updatedMarks)
        tr.replaceWith(pos, pos + node.nodeSize, updatedText)
      }
    })

    if (dispatch) {
      dispatch(tr)
    }
  }

  /**
   * Selects a match by id, if no id is provided or the id is not found, the previously selected match is cleared
   * @param matchId
   */
  selectMatch = (matchId?: number) => {
    if (matchId) {
      const selectedMatch = this.matches.find((match) => match.id === matchId)
      if (selectedMatch) {
        this.selectedMatch.value = selectedMatch
        return
      }
    }
    this.clearSelectedMatch()
  }

  clearSelectedMatch = () => {
    this.selectedMatch.value = undefined
  }

  ignoreWord = async (word: string): Promise<boolean> => {
    const store = useDocumentUnitStore()

    if (store.documentUnit?.uuid) {
      const response: ServiceResponse<IgnoredTextCheckWord> =
        await languageToolService.addLocalIgnore(store.documentUnit?.uuid, word)

      if (response.status >= 300) {
        this.responseError.value = response.error
      } else {
        this.selectedMatch.value.ignoredTextCheckWords ??= []
        this.selectedMatch.value.ignoredTextCheckWords.push(response.data)
        return true
      }
    }
    return false
  }

  removeIgnoredWord = async (word: string): Promise<boolean> => {
    const store = useDocumentUnitStore()

    if (store.documentUnit?.uuid) {
      const response: ServiceResponse<void> =
        await languageToolService.removeLocalIgnore(
          store.documentUnit?.uuid,
          word,
        )

      if (response.status >= 300) {
        this.responseError.value = response.error
        return false
      } else {
        this.selectedMatch.value.ignoredTextCheckWords = (
          this.selectedMatch.value
            .ignoredTextCheckWords as IgnoredTextCheckWord[]
        ).filter(({ type }) => type !== "documentation_unit")
        return true
      }
    }
    return false
  }

  ignoreWordGlobally = async (word: string): Promise<boolean> => {
    const response: ServiceResponse<IgnoredTextCheckWord> =
      await languageToolService.addGlobalIgnore(word)

    if (response.status >= 300) {
      this.responseError.value = response.error
    } else {
      this.selectedMatch.value.ignoredTextCheckWords ??= []
      this.selectedMatch.value.ignoredTextCheckWords.push(response.data)
      return true
    }
    return false
  }

  removeGloballyIgnoredWord = async (word: string): Promise<boolean> => {
    const response: ServiceResponse<void> =
      await languageToolService.removeGlobalIgnore(word)

    if (response.status >= 300) {
      this.responseError.value = response.error
    } else {
      this.selectedMatch.value.ignoredTextCheckWords = (
        this.selectedMatch.value.ignoredTextCheckWords as IgnoredTextCheckWord[]
      ).filter(({ type }) => type !== "global")
      return true
    }
    return false
  }
}

export { NeurisTextCheckService }
