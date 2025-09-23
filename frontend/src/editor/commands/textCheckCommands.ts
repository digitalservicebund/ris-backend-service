import { Editor } from "@tiptap/core"
import { EditorState } from "prosemirror-state"
import { ref } from "vue"
import { ServiceResponse } from "@/services/httpClient"
import languageToolService from "@/services/textCheckService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import {
  DocumentationType,
  IgnoredTextCheckWord,
  TextCheckCategoryResponse,
  TextCheckService,
  TextCheckTagName,
} from "@/types/textCheck"

class NeurisTextCheckService implements TextCheckService {
  loading = ref(false)
  selectedMatch = ref()
  responseError = ref()

  category: string // text editor label category where matches are stored

  private readonly store = useDocumentUnitStore()

  constructor(category: string) {
    this.category = category
  }

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
  checkCategory = async (editor: Editor) => {
    this.loading.value = true
    this.responseError.value = undefined

    if (
      this.store.documentUnit?.uuid == undefined ||
      this.category == undefined
    ) {
      return
    }
    await this.store.updateDocumentUnit()

    const languageToolCheckResponse: ServiceResponse<TextCheckCategoryResponse> =
      await languageToolService.checkCategory(
        this.store.documentUnit?.uuid,
        this.category,
      )

    if (languageToolCheckResponse.status == 200) {
      this.store.matches.set(
        this.category,
        languageToolCheckResponse.data!.matches,
      )
      editor.commands.setContent(
        languageToolCheckResponse.data!.htmlText,
        true,
        { preserveWhitespace: "full" },
      )
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
   * @param state
   * @param dispatch
   */
  updateIgnoredMark = (
    matchId: number,
    state: EditorState,
    dispatch: ((args?: any) => any) | undefined,
  ): void => {
    const { tr, schema } = state

    const matches = this.store.matches.get(this.category)

    matches?.forEach((match) => {
      const isIgnored = (match.ignoredTextCheckWords?.length ?? 0) > 0

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
    })

    if (dispatch) {
      dispatch(tr)
    }
  }

  /**
   * Updates the ignored status of a match by id
   * @param state
   * @param dispatch
   */
  updatedMatchesInText = (
    state: EditorState,
    dispatch: ((args?: any) => any) | undefined,
  ): void => {
    const matches = this.store.matches.get(this.category)

    matches?.forEach((match) => {
      this.updateIgnoredMark(match.id, state, dispatch)
    })
  }

  /**
   * Selects a match by id, if no id is provided or the id is not found, the previously selected match is cleared
   * @param matchId
   */
  selectMatch = (matchId?: number) => {
    if (matchId && this.category) {
      const matches = this.store.matches.get(this.category) ?? []
      const selectedMatch = matches.find((match) => match.id === matchId)

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
    if (this.store.documentUnit?.uuid) {
      const response: ServiceResponse<IgnoredTextCheckWord> =
        await languageToolService.addLocalIgnore(
          this.store.documentUnit?.uuid,
          word,
        )

      if (response.status >= 300) {
        this.responseError.value = response.error
      } else if (response.data) {
        this.addIgnoredWordToMatches(word, response.data)
        return true
      }
    }
    return false
  }

  removeIgnoredWord = async (word: string): Promise<boolean> => {
    if (this.store.documentUnit?.uuid) {
      const response: ServiceResponse<void> =
        await languageToolService.removeLocalIgnore(
          this.store.documentUnit?.uuid,
          word,
        )

      if (response.status >= 300) {
        this.responseError.value = response.error
        return false
      } else {
        this.removeIgnoredWordFromMatches(word, "documentation_unit")
        return true
      }
    }
    return false
  }

  addIgnoredWordToMatches = (
    word: string,
    ignoredTextCheckWord: IgnoredTextCheckWord,
  ) => {
    for (const matchList of this.store.matches.values()) {
      matchList.forEach((match) => {
        if (match.word == word && ignoredTextCheckWord) {
          match.ignoredTextCheckWords ??= []

          const alreadyExists = match.ignoredTextCheckWords.some(
            (ignored) => ignored.id === ignoredTextCheckWord.id,
          )

          if (!alreadyExists) {
            match.ignoredTextCheckWords.push(ignoredTextCheckWord)
          }
        }
      })
    }
  }

  removeIgnoredWordFromMatches = (
    word: string,
    ignoredType: DocumentationType,
  ) => {
    for (const matchList of this.store.matches.values()) {
      matchList.forEach((match) => {
        if (match.word === word) {
          if (match.ignoredTextCheckWords) {
            match.ignoredTextCheckWords = match.ignoredTextCheckWords.filter(
              ({ type }) => type !== ignoredType,
            )
          }
        }
      })
    }
  }

  ignoreWordGlobally = async (word: string): Promise<boolean> => {
    const response: ServiceResponse<IgnoredTextCheckWord> =
      await languageToolService.addGlobalIgnore(word)

    if (response.status >= 300) {
      this.responseError.value = response.error
    } else if (response.data) {
      this.addIgnoredWordToMatches(word, response.data)
    }
    return false
  }

  removeGloballyIgnoredWord = async (word: string): Promise<boolean> => {
    const response: ServiceResponse<void> =
      await languageToolService.removeGlobalIgnore(word)

    if (response.status >= 300) {
      this.responseError.value = response.error
    } else if (response.status == 200) {
      this.removeIgnoredWordFromMatches(word, "global")
    }
    return false
  }
}

export { NeurisTextCheckService }
