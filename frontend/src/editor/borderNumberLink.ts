import { Mark, markInputRule } from "@tiptap/core"

export interface BorderNumberOptions {
  validBorderNumbers: string[]
}

export const BorderNumberLink = Mark.create<BorderNumberOptions>({
  name: "BorderNumberLink",
  inclusive: false,

  addOptions() {
    return {
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

  renderHTML({ mark }) {
    const valid = this.options.validBorderNumbers.includes(mark.attrs.nr)
    const color = valid ? "text-blue-800" : "text-red-800"
    return [
      "border-number-link",
      {
        class: `font-bold ${color} underline italic before:content-["Rn._"]`,
        valid: valid,
        nr: mark.attrs.nr,
      },
      0,
    ]
  },

  addInputRules() {
    return [
      markInputRule({
        find: /(?:^|\s)(#([^#]+)#)$/,
        type: this.type,
        getAttributes(match) {
          return { nr: match[2] }
        },
      }),
    ]
  },

  addAttributes() {
    return {
      nr: {
        default: null,
        parseHTML: (element) => element.getAttribute("nr"),
        renderHTML: (attributes) => ({
          nr: attributes.nr,
        }),
      },
    }
  },
})
