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
  name: "BorderNumberLink",

  addOptions() {
    return {
      HTMLAttributes: {},
      validBorderNumbers: ["1", "2", "3"],
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
    const valid = this.options.validBorderNumbers.includes(mark.attrs.nr)
    const color = valid ? "text-blue-800" : "text-red-800"
    return [
      "border-number-link",
      mergeAttributes(this.options.HTMLAttributes, HTMLAttributes, {
        // class: "ds-heading-03-bold, ds-text-blue-900",
        class: `font-bold ${color} italic underline`,
        // style:
        //     "color: #003350; font-weight: bold; text-decoration: underline; padding: 2px; font-style: italic",
        valid: valid,
      }),
      `Rn. ${mark.attrs.nr}`,
    ]
  },

  addInputRules() {
    return [
      markInputRule({
        find: /(?:^|\s)((?:#)((?:[^#]+))(?:#))$/,
        // find: this.options.validBorderNumbers
        //   ? new RegExp(`(##(${"1|2|3"})##)$`, "g")
        //   : /(?:^|\s)(##(\d+)##)$/,
        type: this.type,
        getAttributes(match) {
          console.log(match)
          return { nr: match[2] }
        },
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
  addAttributes() {
    return {
      nr: {
        default: null,
        parseHTML: (element) => element.getAttribute("nr"),
        renderHTML: (attributes) => {
          if (!attributes.nr) {
            return {}
          }

          return {
            nr: attributes.nr,
          }
        },
      },
    }
  },
})
