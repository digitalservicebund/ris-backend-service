import { Image } from "@tiptap/extension-image"

export const CustomImage = Image.extend({
  addAttributes() {
    return {
      ...this.parent?.(),
      width: { default: null },
      height: { default: null },
      style: { default: null },
    }
  },
})
