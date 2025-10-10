import { Extension } from "@tiptap/core"
import { Node } from "prosemirror-model"
import { Plugin, Transaction, EditorState } from "prosemirror-state"
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
        appendTransaction: (
          transactions: readonly Transaction[],
          oldState: EditorState,
          newState: EditorState,
        ) => {
          if (!transactions.some((tr: Transaction) => tr.docChanged))
            return null

          const { schema } = newState
          const markType = schema.marks[TextCheckTagName]
          if (!markType) return null

          let modified = false
          const tr = newState.tr
          newState.doc.descendants((node: Node | undefined, pos: number) => {
            if (!node?.isText) return true
            if (node?.text === undefined) return true
            const marks = node.marks.filter((mark) => mark.type === markType)
            if (marks.length === 0) return true

            const oldNode = oldState.doc.nodeAt(pos)
            if (!oldNode?.isText) return true
            if (oldNode.text !== node.text) {
              tr.removeMark(pos, pos + node.text.length, markType)
              modified = true
            }
            return true
          })
          return modified ? tr : null
        },
      }),
    ]
  },
})
