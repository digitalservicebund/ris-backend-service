import { Paragraph } from "@tiptap/extension-paragraph"

export const CustomParagraph = Paragraph.extend({
  addAttributes() {
    return {
      ...this.parent?.(),
      class: { default: null },
      style: {
        default: null,
        parseHTML: (element) => element.getAttribute("style"),
        renderHTML: (attributes) => {
          return {
            style: attributes.style,
          }
        },
      },
    }
  },
})
