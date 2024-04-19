import {
  createInvisiblesPlugin,
  space,
  paragraph,
  nbSpace,
  heading,
  createInvisibleDecosForCharacter,
  createInvisibleDecosForNode,
} from "@guardian/prosemirror-invisibles"
import { Extension } from "@tiptap/core"
import { Node } from "prosemirror-model"
import "@guardian/prosemirror-invisibles/dist/style.css"
import "./softHyphen.css"

// Can be removed (along with the css file and the createInvisibleDecosForCharacter("soft-hyphen", isSoftHyphen) below) as soon as prosemirror-invisibles is updated (latest version includes soft-hyphen)
const isSoftHyphen = (char: string) => char === "\u00ad"
// Necessary as TipTap overrides the hardbreak node type name from hard_break to hardBreak
const isHardbreak = (node: Node): boolean => node.type.name === "hardBreak"

export const InvisibleCharacters = Extension.create({
  name: "invisible-characters",
  addProseMirrorPlugins() {
    return [
      createInvisiblesPlugin([
        space,
        paragraph,
        nbSpace,
        heading,
        createInvisibleDecosForCharacter("soft-hyphen", isSoftHyphen),
        createInvisibleDecosForNode("break", (_, pos) => pos, isHardbreak),
      ]),
    ]
  },
})
