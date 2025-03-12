import { Extension } from "@tiptap/core"
import {
  NeurisTextCheckService,
  TextCheckService,
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
      service: new NeurisTextCheckService(),
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
          const service = editor.storage.textCheckExtension
            .service as TextCheckService

          void service.checkCategory(editor, this.options.category)
          return true
        },
      handleMatchSelection:
        () =>
        ({ state, editor }) => {
          const service = editor.storage.textCheckExtension
            .service as TextCheckService

          service.handleSelection(editor, state)
        },

      setSelectedMatch:
        (matchId?: number) =>
        ({ editor }) => {
          const service = editor.storage.textCheckExtension
            .service as TextCheckService

          service.setMatch(editor, matchId)

          return true
        },
      acceptMatch:
        (matchId: number, text: string) =>
        ({ editor }) => {
          if (matchId) {
            const service = editor.storage.textCheckExtension
              .service as TextCheckService

            service.replaceMatch(editor, matchId, text)
            return true
          }
          return false
        },
    }
  },
})
