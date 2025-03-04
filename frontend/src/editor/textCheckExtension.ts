import { Extension } from "@tiptap/core"
import {
  checkCategory,
  handleSelection,
  replaceMatch,
  setMatch,
} from "@/editor/commands/textCheckCommands"
import {
  TextCheckExtensionOptions,
  TextCheckExtensionStorage,
} from "@/types/languagetool"

declare module "@tiptap/core" {
  interface Commands<ReturnType> {
    languagetool2: {
      textCheck: () => ReturnType
      setSelectedMatch: (matchId?: number) => ReturnType
      resetSelectedMatch: () => ReturnType
      handleMatchSelection: () => ReturnType
      acceptMatch: (matchId: number, text: string) => ReturnType
    }
  }
}

export const TextCheckExtension = Extension.create<
  TextCheckExtensionOptions,
  TextCheckExtensionStorage
>({
  name: "languagetool2",

  addStorage() {
    return {
      matches: [],
      selectedMatch: undefined,
    }
  },
  addOptions() {
    return {
      category: undefined,
    }
  },

  addCommands() {
    return {
      textCheck:
        () =>
        ({ editor }) => {
          void checkCategory(editor, this.options.category)
          return true
        },
      handleMatchSelection: () => handleSelection,

      setSelectedMatch:
        (matchId?: number) =>
        ({ editor }) => {
          void setMatch(editor, matchId)
          return false
        },
      acceptMatch:
        (matchId: number, text: string) =>
        ({ editor }) => {
          if (matchId) {
            replaceMatch(editor, matchId, text)
            return true
          }
          return false
        },
    }
  },
})
