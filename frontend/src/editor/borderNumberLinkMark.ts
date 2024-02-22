import {
  Mark,
  markInputRule,
  mergeAttributes,
  markPasteRule,
} from "@tiptap/core"

export interface BorderNumberOptions {
  HTMLAttributes: Record<string, string>
  validBorderNumbers: number[]
}

export const BorderNumberLinkMark = Mark.create<BorderNumberOptions>({
  name: "BorderNumber",

  addOptions() {
    return {
      HTMLAttributes: {},
      validBorderNumbers: [],
    }
  },

  parseHTML() {
    return [
      {
        tag: "border-number-link",
      },
    ]
  },

  renderHTML({ mark, HTMLAttributes }) {
    this.options.validBorderNumbers
    return [
      "border-number-link",
      mergeAttributes(this.options.HTMLAttributes, HTMLAttributes, {
        class: "ds-heading-03-bold, ds-text-blue-900",
        valid: false,
      }),
      `Rn. ${mark}`,
    ]
  },

  addInputRules() {
    return [
      markInputRule({
        find: this.options.validBorderNumbers
          ? new RegExp(`(##(${"1|2|3"})##)`, "g")
          : /(?:^|\s)(##(\d+)##)$/,
        type: this.type,
      }),
    ]
  },

  addPasteRules() {
    return [
      markPasteRule({
        find: this.options.validBorderNumbers
          ? new RegExp(
              `(##(${this.options.validBorderNumbers.join("|")})##)`,
              "g",
            )
          : /(?:^|\s)(##(\d+)##)/g,
        type: this.type,
      }),
    ]
  },
  addKeyboardShortcuts() {
    return {
      // work with plain transactions
      // Enter: () => {
      //   const { tr } = this.editor.state
      //
      //   tr.insertText(' ')
      //
      //   this.editor.view.dispatch(tr)
      //
      //   return true
      // },
      // or use commands as abstraction layer
      // Enter: () => {
      //   return this.editor.commands.command(({tr, state}) => {
      //     return state.tr.insertText(' ')
      //   });
      // },
      //
      // // or use the generic `command()` command
      // Enter: () => {
      //   return this.editor.commands.command(({ tr }) => {
      //     tr.insertText(' ')
      //
      //     return true
      //   })
      // },
    }
  },
})
