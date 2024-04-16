import {
  createInvisiblesPlugin,
  space,
  hardBreak,
  paragraph,
} from "@guardian/prosemirror-invisibles"
import { Extension } from "@tiptap/core"
import "@guardian/prosemirror-invisibles/dist/style.css"

export const InvisibleCharacters = Extension.create({
  name: "invisible-characters",
  addProseMirrorPlugins() {
    return [
      createInvisiblesPlugin([space, hardBreak, paragraph], {
        shouldShowInvisibles: true,
      }),
    ]
  },
})
