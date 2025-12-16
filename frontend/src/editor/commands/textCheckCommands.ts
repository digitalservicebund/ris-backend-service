import { Editor } from "@tiptap/core"
import { EditorState } from "prosemirror-state"
import { ref } from "vue"
import { IgnoreOnceTagName } from "@/editor/ignoreOnceMark"
import { ServiceResponse } from "@/services/httpClient"
import languageToolService from "@/services/textCheckService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import {
  DocumentationType,
  IgnoredTextCheckWord,
  Match,
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
    }

    if (textCheckMark) {
      this.selectMatch(Number(textCheckMark.attrs.id))
      return true
    }

    return false
  }

  /**
   * Performs a spell check on a given {@link category}. The documentation unit is saved beforehand
   * @param editor
   */
  checkCategory = async (editor: Editor) => {
    this.loading.value = true
    const oldEditableValue = editor.isEditable
    editor.setEditable(false)
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
      editor.commands.setContent(languageToolCheckResponse.data!.htmlText, {
        emitUpdate: true,
        parseOptions: { preserveWhitespace: "full" },
      })
    } else if (languageToolCheckResponse.error) {
      this.responseError.value = languageToolCheckResponse.error
    }

    this.loading.value = false
    editor.setEditable(oldEditableValue)
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
    dispatch?: (tr: any) => void,
  ) => {
    const { tr, schema } = state
    state.doc.descendants((node, pos) => {
      if (
        node.isText &&
        NeurisTextCheckService.findTextCheckMark(node, matchId)
      ) {
        tr.replaceWith(pos, pos + node.nodeSize, schema.text(text))
      }
    })
    dispatch?.(tr)
    this.clearSelectedMatch()
  }

  /**
   * Updates the ignored status in text by match
   * @param match
   * @param state
   * @param dispatch
   */
  updateIgnoredMark = (
    match: Match,
    state: EditorState,
    dispatch?: (tr: any) => void,
  ) => {
    if (!match) return
    const { tr, schema } = state

    state.doc.descendants((node, pos) => {
      const mark = NeurisTextCheckService.findTextCheckMark(node, match.id)
      if (!mark) return

      const nextIgnoredStatus = NeurisTextCheckService.isMatchedIgnored(match)
      // if there is no ignored it means, false
      const prevIgnoredStatus = mark.attrs.ignored ?? false

      // Only update if the ignore value changed
      if (prevIgnoredStatus !== nextIgnoredStatus) {
        const updatedMarks = node.marks.map((m) =>
          m === mark
            ? mark.type.create({
                ...mark.attrs,
                ignored: nextIgnoredStatus,
              })
            : m,
        )

        tr.replaceWith(
          pos,
          pos + node.nodeSize,
          schema.text(node.text ?? "", updatedMarks),
        )
      }
    })

    if (tr.docChanged) {
      dispatch?.(tr)
    }
  }

  private static isMatchedIgnored(match: Match) {
    return (match.ignoredTextCheckWords?.length ?? 0) > 0 || match.isIgnoredOnce
  }

  private static findTextCheckMark(node: any, matchId?: number) {
    return node.marks?.find(
      (mark: any) =>
        mark.type.name === TextCheckTagName &&
        (matchId === undefined || mark.attrs.id === matchId.toString()),
    )
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
      this.updateIgnoredMark(match, state, dispatch)
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

  /**
   * @doc Structural Toggle & Data Sync for Ignore Once.
   * This function performs the structural change in the Tiptap editor and updates the store data.
   * It uses the current editor selection position to find the associated TextCheckMark ID and its true ProseMirror range.
   * @param editor The Tiptap Editor instance required to execute transactions.
   */
  public async toggleIgnoreOnce(editor: Editor): Promise<boolean> {
    const { state } = editor
    let matchId: number | null = null
    let markRange = { from: 0, to: 0 }

    const cursorPosition = state.selection.from

    // We need DOM traversal here, because the selectedMatch's offset is not reliable to calculate the correct prosemirror range
    let found = false
    state.doc.descendants((node, pos) => {
      if (node.isText) {
        node.marks.forEach((mark) => {
          if (mark.type.name === TextCheckTagName) {
            // Check if the current cursor position is within the node's bounds
            if (
              pos <= cursorPosition &&
              pos + node.nodeSize >= cursorPosition
            ) {
              markRange = { from: pos, to: pos + node.nodeSize }
              matchId = Number(mark.attrs.id)
              found = true
            }
          }
        })
      }
      // Stop descending once the relevant block is found (or if we've passed it)
      return !found
    })

    if (!found || !matchId) {
      console.warn(
        "Toggle ignore once abort: Could not find TextCheck Mark at selection.",
        matchId,
      )
      return false
    }

    const matches = this.store.matches.get(this.category)
    const matchToUpdate = matches?.find((m) => m.id === matchId)

    if (!matchToUpdate) {
      console.error(
        `Toggle abort: Match with ID ${matchId} not found in store.`,
      )
      return false
    }

    matchToUpdate.isIgnoredOnce = !matchToUpdate.isIgnoredOnce

    editor
      .chain()
      .focus()
      .setTextSelection(markRange)
      .toggleMark(IgnoreOnceTagName)
      .run()

    return true
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
        this.addIgnoredWordToMatches(response.data)
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

  addIgnoredWordToMatches = (ignoredTextCheckWord: IgnoredTextCheckWord) => {
    for (const matchList of this.store.matches.values()) {
      matchList.forEach((match) => {
        if (match.word === ignoredTextCheckWord.word) {
          match.ignoredTextCheckWords ??= []

          const alreadyIgnored = match.ignoredTextCheckWords.some(
            (ignored) =>
              ignored.id === ignoredTextCheckWord.id ||
              (ignored.type === ignoredTextCheckWord.type &&
                ignored.word === ignoredTextCheckWord.word),
          )

          if (!alreadyIgnored) {
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
              (ignored) =>
                !(ignored.type === ignoredType && ignored.word === word),
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
      this.addIgnoredWordToMatches(response.data)
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
