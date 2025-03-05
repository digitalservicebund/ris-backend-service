import { Editor } from "@tiptap/core"
import { CommandProps } from "@tiptap/vue-3"
import { ServiceResponse } from "@/services/httpClient"
import languageToolService from "@/services/languageToolService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import {
  NoIndexTagName,
  TextCheckCategoryResponse,
  TextCheckExtensionStorage,
  TextCheckTagName,
} from "@/types/languagetool"

const isTextCheckTagSelected = (editor: Editor): boolean => {
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
 * @param editor
 */
const handleSelection = ({ state, editor }: CommandProps): boolean => {
  const node = state.doc.nodeAt(state.selection.from)

  if (!node?.marks) {
    clearSelectedMatch(editor)
    return false
  }

  const textCheckMark = node.marks.find(
    (mark) => mark.type.name === TextCheckTagName,
  )

  if (!textCheckMark) {
    clearSelectedMatch(editor)
    return false
  }

  setMatch(editor, Number(textCheckMark.attrs.id))
  return true
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
      .textCheckExtension as TextCheckExtensionStorage
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
        const tr = editor.state.tr
          .delete(pos, pos + node.nodeSize)
          .insert(pos, editor.state.schema.text(text))

        editor.view.dispatch(tr)
      }
    }
  })
  clearSelectedMatch(editor)
}

const setMatch = (editor: Editor, matchId?: number) => {
  const extensionStorage = getExtensionStorage(editor)

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
    .textCheckExtension as TextCheckExtensionStorage
  extensionStorage.selectedMatch = undefined
}

const getExtensionStorage = (editor: Editor): TextCheckExtensionStorage => {
  return editor.storage.textCheckExtension as TextCheckExtensionStorage
}

const ignoreMatch = (editor: Editor, matchId: number) => {
  console.log("i am here")
  editor.state.doc.descendants((node, pos) => {
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
        const noIndexMark = editor.state.schema.marks[NoIndexTagName]?.create()
        const tr = editor.state.tr
          .delete(pos, pos + node.nodeSize)
          .insert(pos, editor.state.schema.text(node.text, [noIndexMark]))

        editor.view.dispatch(tr)
      }
    }
  })
  clearSelectedMatch(editor)
}

const clearMatches = (editor: Editor) => {
  getExtensionStorage(editor).matches = []
}

export {
  handleSelection,
  checkCategory,
  replaceMatch,
  setMatch,
  clearMatches,
  isTextCheckTagSelected,
  ignoreMatch,
}
