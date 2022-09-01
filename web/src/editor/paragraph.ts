import { Paragraph } from "@tiptap/extension-paragraph"

export const CustomParagraph = Paragraph.extend({
  addAttributes() {
    return {
      ...this.parent?.(),
      class: { default: null },
    }
  },
})
