import { Extension } from "@tiptap/core"
import { Plugin, PluginKey, Transaction, EditorState } from "prosemirror-state"
import {
  TextCheckExtensionOptions,
  TextCheckService,
  TextCheckTagName,
} from "@/types/textCheck"

declare module "@tiptap/core" {
  interface Commands<ReturnType> {
    textCheckExtension: {
      textCheck: () => ReturnType
      setSelectedMatch: (matchId?: number) => ReturnType
      handleMatchSelection: () => ReturnType
      acceptMatch: (matchId: number, text: string) => ReturnType
      updatedMatchesInText: () => ReturnType
    }
  }
}

export const TextCheckExtension = Extension.create<TextCheckExtensionOptions>({
  name: "textCheckExtension",

  addStorage() {},
  addOptions() {
    return {
      service: undefined,
    }
  },

  addCommands() {
    return {
      textCheck:
        () =>
        ({ editor }) => {
          const service = this.options.service as TextCheckService

          void service.checkCategory(editor)
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

      updatedMatchesInText:
        () =>
        ({ state, dispatch }) => {
          const service = this.options.service as TextCheckService

          service.updatedMatchesInText(state, dispatch)
          return true
        },
    }
  },

  addProseMirrorPlugins() {
    return [
      new Plugin({
        key: new PluginKey("textCheckExtension"),
        appendTransaction: (
          transactions: readonly Transaction[],
          oldState: EditorState,
          newState: EditorState,
        ) => {
          const { schema } = newState
          const markType = schema.marks[TextCheckTagName]

          if (!markType) return null

          const isDocumentChanged = transactions.some(
            (transaction) => transaction.docChanged,
          )
          if (!isDocumentChanged) {
            return null
          }

          let modified = false
          const newStateTransaction = newState.tr

          transactions.forEach((transaction) => {
            const { from, to } = transaction.selection
            let oldNodeText: string | undefined = ""

            if (!oldState?.doc) return

            // If the difference between newState and oldState is greater than 1 char, then it is
            // not user typing text and this plugin should not care about it.
            const testSizeDifference = Math.abs(
              oldState.doc.nodeSize - newState.doc.nodeSize,
            )
            if (testSizeDifference > 1) return
            oldState.doc.nodesBetween(from - 1, to + 1, (node) => {
              oldNodeText = node.text
            })

            newState.doc.nodesBetween(from - 1, to + 1, (node, pos) => {
              if (!node.isText) return
              if (node.text == oldNodeText) return

              const hasMark = markType.isInSet(node.marks)

              if (hasMark) {
                newStateTransaction.removeMark(
                  pos,
                  pos + node.nodeSize,
                  markType,
                )
                modified = true
              }
            })
          })

          return modified ? newStateTransaction : null
        },
      }),
    ]
  },
})
