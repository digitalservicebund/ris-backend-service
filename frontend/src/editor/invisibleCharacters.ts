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
const isTab = (node: Node): boolean => node.attrs.indent >= 40
const isBlockquote = (node: Node) => node.type.name === "blockquote"

const countNestedBlockquotes = (startNode: Node): number => {
  let count = 0
  let currentNode: Node | null = startNode

  while (currentNode && currentNode.type.name === "blockquote") {
    count++
    currentNode = currentNode.firstChild
  }

  return count
}


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
        createInvisibleDecosForNode("tab", (_, pos) => pos, isTab),
        createInvisibleDecosForNode("break", (_, pos) => pos, isHardbreak),
        createInvisibleDecosForNode(
          "blockquote",
          (node, pos) => pos + countNestedBlockquotes(node) - 1,
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
