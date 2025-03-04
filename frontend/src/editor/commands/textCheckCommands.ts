import { Editor } from "@tiptap/core"
import { CommandProps } from "@tiptap/vue-3"
import { ServiceResponse } from "@/services/httpClient"
import languageToolService from "@/services/languageToolService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import {
  TextCheckCategoryResponse,
  TextCheckExtensionStorage,
} from "@/types/languagetool"

const handleSelection = ({ state, editor }: CommandProps): boolean => {
  const { selection } = state

  const node = state.doc.nodeAt(selection.from)

  if (node && node.marks) {
    const textCheckMark = node.marks.find(
      (mark) => mark.type.name === "textCheck",
    )

    if (textCheckMark) {
      const matchId = textCheckMark.attrs.id
      editor.commands.setSelectedMatch(matchId)
      return true
    }
  }

  return false
}

const checkCategory = async (editor: Editor, category?: string) => {
  const store = useDocumentUnitStore()

  if (store.documentUnit?.uuid == undefined) {
    return
  }

  const languageToolCheckResponse: ServiceResponse<TextCheckCategoryResponse> =
    await languageToolService.checkCategory(store.documentUnit?.uuid, category)

  if (languageToolCheckResponse.status == 200) {
    const extensionStorage = editor.storage
      .languagetool2 as TextCheckExtensionStorage
    extensionStorage.matches = languageToolCheckResponse.data!.matches

    editor.commands.setContent(languageToolCheckResponse.data!.htmlText)
  }
}

/**
 * Received match id and the text to replace, find the mark with the id, remove the text-check html tag and set the text
 * @param editor
 * @param matchId
 * @param text
 */
const replaceMatch = (editor: Editor, matchId: number, text: string) => {
  editor.state.doc.descendants((node, pos) => {
    if (
      node.isText &&
      node.marks.some(
        (mark) =>
          mark.type.name === "textCheck" &&
          mark.attrs.id === matchId.toString(),
      )
    ) {
      const textCheckMark = node.marks.find(
        (mark) =>
          mark.type.name === "textCheck" &&
          mark.attrs.id === matchId.toString(),
      )

      if (textCheckMark) {
        const tr = editor.state.tr
          .delete(pos, pos + node.nodeSize)
          .insert(pos, editor.state.schema.text(text))

        editor.view.dispatch(tr)
      }
    }
    clearSelectedMatch(editor)
  })
}

const setMatch = async (editor: Editor, matchId?: number) => {
  const extensionStorage: TextCheckExtensionStorage =
    editor.storage.languagetool2

  if (matchId) {
    const selectedMatch = extensionStorage.matches.find(
      (match) => match.id === matchId,
    )
    if (selectedMatch) {
      extensionStorage.selectedMatch = selectedMatch
    } else {
      clearSelectedMatch(editor)
    }
  } else {
    clearSelectedMatch(editor)
  }
}

const clearSelectedMatch = (editor: Editor) => {
  const extensionStorage = editor.storage
    .languagetool2 as TextCheckExtensionStorage
  extensionStorage.selectedMatch = undefined
}

const clearAllMatches = (editor: Editor) => {
  clearSelectedMatch(editor)
  clearMatches(editor)
}

const clearMatches = (editor: Editor) => {
  const extensionStorage = editor.storage
    .languagetool2 as TextCheckExtensionStorage
  extensionStorage.matches = []
}

export { handleSelection, checkCategory, replaceMatch, setMatch, clearMatches }
