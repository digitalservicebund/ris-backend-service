import "@tiptap/extension-text-style"

import { Extension } from "@tiptap/core"

declare module "@tiptap/core" {
  interface Commands<ReturnType> {
    color: {
      /**
       * Set the font size
       */
      setFontSize: (fontSize: string) => ReturnType
      /**
       * Unset the text color
       */
      unsetFontSize: () => ReturnType
    }
  }
}

export type FontSizeOptions = {
  types: string[]
}

export const FontSize = Extension.create<FontSizeOptions>({
  name: "fontSize",

  addOptions() {
    return {
      types: ["textStyle", "paragraph"],
    }
  },

  addGlobalAttributes() {
    return [
      {
        types: this.options.types,
        attributes: {
          fontSize: {
            default: null,
            parseHTML: (element) =>
              element.style.fontSize?.replace(/['"]+/g, ""),
            renderHTML: (attributes) => {
              if (!attributes.fontSize) {
                return {}
              }

              return {
                style: `font-size: ${attributes.fontSize}`,
              }
            },
          },
        },
      },
    ]
  },

  addCommands() {
    return {
      setFontSize:
        (fontSize) =>
        ({ chain }) => {
          return chain().setMark("textStyle", { fontSize }).run()
        },
      unsetFontSize:
        () =>
        ({ chain }) => {
          return chain()
            .setMark("textStyle", { fontSize: null })
            .removeEmptyTextStyle()
            .run()
        },
    }
  },
})
