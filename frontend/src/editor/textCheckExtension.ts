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
    textCheckExtension: {
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
  name: "textCheckExtension",

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
          setMatch(editor, matchId)
          return true
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
