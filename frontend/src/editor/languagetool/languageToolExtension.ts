import { Extension } from "@tiptap/core"
import { Node as PMNode } from "prosemirror-model"
import { Plugin, PluginKey } from "prosemirror-state"
import { DecorationSet } from "prosemirror-view"
import LanguageTool from "@/editor/languagetool/languageTool"

import {
  LanguageToolHelpingWords,
  LanguageToolOptions,
  LanguageToolStorage,
} from "@/types/languagetool"

declare module "@tiptap/core" {
  interface Commands<ReturnType> {
    languagetool: {
      /**
       * Proofreads whole document
       */
      proofread: () => ReturnType

      toggleProofreading: () => ReturnType

      ignoreLanguageToolSuggestion: () => ReturnType

      resetLanguageToolMatch: () => ReturnType

      toggleLanguageTool: (state: boolean) => ReturnType

      getLanguageToolState: () => ReturnType
    }
  }
}

export const LanguageToolExtension = Extension.create<
  LanguageToolOptions,
  LanguageToolStorage
>({
  name: "languagetool",

  addOptions() {
    return {
      language: "auto",
      automaticMode: true,
      documentId: undefined,
      textToolEnabled: false,
    }
  },

  addStorage() {
    return {
      languageToolService: undefined,
      loading: false,
      matchRange: {
        from: -1,
        to: -1,
      },
    }
  },

  addCommands() {
    return {
      proofread:
        () =>
        ({ tr }) => {
          void this.storage.languageToolService!.proofreadAndDecorateWholeDoc(
            tr.doc,
          )
          return true
        },

      ignoreLanguageToolSuggestion:
        () =>
        ({ editor }) => {
          if (this.options.documentId === undefined)
            throw new Error(
              "Please provide a unique Document ID(number|string)",
            )

          const { selection } = editor.state
          const { from, to } = selection

          this.storage.languageToolService?.removeDecorationSet(from, to)

          // const content = doc.textBetween(from, to)

          return false
        },
      resetLanguageToolMatch:
        () =>
        ({
          editor: {
            view: {
              dispatch,
              state: { tr },
            },
          },
        }) => {
          this.storage.languageToolService?.resetLanguageToolMatch()

          dispatch(
            tr
              .setMeta(
                LanguageToolHelpingWords.MatchRangeUpdatedTransactionName,
                true,
              )
              .setMeta(
                LanguageToolHelpingWords.MatchUpdatedTransactionName,
                true,
              ),
          )

          return false
        },

      toggleLanguageTool:
        (state) =>
        ({ commands }) => {
          this.storage.languageToolService!.languageToolActive = state

          if (this.storage.languageToolService!.languageToolActive)
            commands.proofread()
          else commands.resetLanguageToolMatch()

          return false
        },

      getLanguageToolState: () => () =>
        this.storage.languageToolService!.languageToolActive,
    }
  },

  addProseMirrorPlugins() {
    return [
      new Plugin({
        key: new PluginKey("languagetoolPlugin"),
        props: {
          decorations(state) {
            return this.getState(state)
          },
        },
        state: {
          init: (_, state) => {
            this.storage.languageToolService = new LanguageTool(
              DecorationSet.create(state.doc, []),
            )

            if (this.options.automaticMode && this.options.textToolEnabled) {
              void this.storage.languageToolService.proofreadAndDecorateWholeDoc(
                state.doc,
              )
            }

            return this.storage.languageToolService.decorationSet
          },
          apply: (tr) => {
            if (!this.storage.languageToolService!.languageToolActive)
              return DecorationSet.empty

            const loading = tr.getMeta(
              LanguageToolHelpingWords.LoadingTransactionName,
            )

            if (loading) this.storage.loading = true
            else this.storage.loading = false

            const languageToolDecorations = tr.getMeta(
              LanguageToolHelpingWords.LanguageToolTransactionName,
            )

            if (languageToolDecorations)
              return this.storage.languageToolService!.decorationSet

            if (tr.docChanged && this.options.automaticMode) {
              if (!this.storage.languageToolService!.proofReadInitially)
                void this.storage.languageToolService!.debouncedProofreadAndDecorate(
                  tr.doc,
                )
              else {
                const {
                  selection: { from, to },
                } = tr

                let changedNodeWithPos:
                  | { node: PMNode; pos: number }
                  | undefined

                tr.doc.descendants((node, pos) => {
                  if (!node.isBlock) return false

                  const [nodeFrom, nodeTo] = [pos, pos + node.nodeSize]

                  if (!(nodeFrom <= from && to <= nodeTo)) return

                  changedNodeWithPos = { node, pos }
                  return false
                })

                if (changedNodeWithPos) {
                  this.storage.languageToolService!.onNodeChanged(
                    changedNodeWithPos.node,
                    changedNodeWithPos.node.textContent,
                    changedNodeWithPos.pos + 1,
                  )
                }
              }
            }

            this.storage.languageToolService!.decorationSet =
              this.storage.languageToolService!.decorationSet.map(
                tr.mapping,
                tr.doc,
              )

            setTimeout(
              this.storage.languageToolService!.addEventListenersToDecorations,
              100,
            )

            return this.storage.languageToolService!.decorationSet
          },
        },
        view: () => ({
          update: (view) => {
            this.storage.languageToolService!.setEditorView(view)
            setTimeout(
              this.storage.languageToolService!.addEventListenersToDecorations,
              100,
            )
          },
        }),
      }),
    ]
  },
})
