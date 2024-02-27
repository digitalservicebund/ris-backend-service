import { Mark, markInputRule } from "@tiptap/core"

export const BorderNumberLink = Mark.create({
  name: "BorderNumberLink",
  inclusive: false,

  parseHTML() {
    return [
      {
        tag: "border-number-link",
      },
    ]
  },

  renderHTML({ mark }) {
    const color =
      mark.attrs.valid === "true"
        ? "text-white bg-blue-700"
        : "text-red-900 bg-red-200"
    return [
      "border-number-link",
      {
        class: `font-bold ${color} before:content-["Rd_"]`,
        valid: mark.attrs.valid,
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
        parseHTML: (element) => element.getAttribute("nr"),
        renderHTML: (attributes) => ({
          nr: attributes.nr,
        }),
      },
      valid: {
        parseHTML: (element) => element.getAttribute("valid"),
        renderHTML: (attributes) => ({
          valid: attributes.valid,
        }),
      },
    }
  },
})
