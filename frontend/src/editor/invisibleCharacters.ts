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
import "../styles/invisible-characters.css"

// Can be removed (along with the css file and the createInvisibleDecosForCharacter("soft-hyphen", isSoftHyphen) below) as soon as prosemirror-invisibles is updated (latest version includes soft-hyphen)
const isSoftHyphen = (char: string) => char === "\u00ad"
// Necessary as TipTap overrides the hardbreak node type name from hard_break to hardBreak
const isHardbreak = (node: Node): boolean => node.type.name === "hardBreak"
const isTab = (char: string) => char === "\t"
const isBlockquote = (node: Node) => node.type.name === "blockquote"

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
        createInvisibleDecosForCharacter("tab", isTab),
        createInvisibleDecosForNode("break", (_, pos) => pos, isHardbreak),
        createInvisibleDecosForNode(
          "blockquote",
          (_, pos) => pos,
          isBlockquote,
        ),
      ]),
    ]
  },
  addKeyboardShortcuts() {
    return {
      // ↓ Windows/Linux (English):   Ctrl + ⇧ Shift + Space
      // ↓ Windows/Linux (Deutsch):   Strg + ⇧ Shift + Leertaste
      // ↓ MacOS:                     ⌘ Cmd + ⇧ Shift + Space
      "Mod-Shift-Space": () => this.editor.commands.insertContent(" "),
    }
  },
})
