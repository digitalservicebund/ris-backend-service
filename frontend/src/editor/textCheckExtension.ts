import { Extension } from "@tiptap/core"
import { TextCheckService } from "@/editor/commands/textCheckCommands"
import { TextCheckExtensionOptions } from "@/types/textCheck"

declare module "@tiptap/core" {
  interface Commands<ReturnType> {
    textCheckExtension: {
      textCheck: () => ReturnType
      setSelectedMatch: (matchId?: number) => ReturnType
      handleMatchSelection: () => ReturnType
      acceptMatch: (matchId: number, text: string) => ReturnType
    }
  }
}

export const TextCheckExtension = Extension.create<TextCheckExtensionOptions>({
  name: "textCheckExtension",

  addStorage() {},
  addOptions() {
    return {
      category: undefined,
      service: undefined,
    }
  },

  addCommands() {
    return {
      textCheck:
        () =>
        ({ editor }) => {
          const service = this.options.service as TextCheckService

          void service.checkCategory(editor, this.options.category)
          return true
        },
      handleMatchSelection:
        () =>
        ({ state }) => {
          const service = this.options.service as TextCheckService

          return service.handleSelection(state)
        },

      setSelectedMatch: (matchId?: number) => () => {
        const service = this.options.service as TextCheckService

        service.selectMatch(matchId)

        return true
      },

      acceptMatch:
        (matchId: number, text: string) =>
        ({ state, dispatch }) => {
          if (matchId) {
            const service = this.options.service as TextCheckService

            service.replaceMatch(matchId, text, state, dispatch)
            return true
          }
          return false
        },
    }
  },
})
