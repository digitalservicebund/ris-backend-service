import { Editor } from "@tiptap/core"
import { Document } from "@tiptap/extension-document"
import { Paragraph } from "@tiptap/extension-paragraph"
import { Text } from "@tiptap/extension-text"
import { describe, it, expect } from "vitest"
import { TextCheckExtension } from "@/editor/textCheckExtension"
import { TextCheckMark } from "@/editor/textCheckMark"

const textCheckMarkAttrs = {
  id: "1",
  type: "test",
  ignored: "false",
}

describe("TextCheckExtension", () => {
  it("removes textCheckMark when text is edited inside the mark", () => {
    const editor = new Editor({
      extensions: [
        Document,
        Paragraph,
        Text,
        TextCheckMark,
        TextCheckExtension,
      ],
      content: {
        type: "doc",
        content: [
          {
            type: "paragraph",
            content: [
              {
                type: "text",
                text: "word",
                marks: [
                  { type: TextCheckMark.name, attrs: textCheckMarkAttrs },
                ],
              },
            ],
          },
        ],
      },
    })

    const paraBeforeEdit = editor.state.doc.firstChild
    const textNodeBeforeEdit = paraBeforeEdit?.firstChild
    expect(textNodeBeforeEdit?.marks.length).toBe(1)
    expect(textNodeBeforeEdit?.marks[0].type.name).toBe(TextCheckMark.name)

    // Simulate typing inside the marked word (insert 'X' after 'w')
    editor.commands.insertContentAt({ from: 2, to: 2 }, "X")

    // Get the updated node
    const node = editor.state.doc.nodeAt(1)
    expect(node).toBeTruthy()
    expect(editor.getText()).toBe("wXord")

    // The mark should be removed after typing
    // Find the text node and check its marks
    const para = editor.state.doc.firstChild
    const textNode = para && para.firstChild

    expect(textNode).toBeTruthy()
    if (!textNode) throw new Error("textNode is null")
    expect(textNode.marks.length).toBe(0)
  })

  it("removes textCheckMark when character is deleted from marked word", () => {
    const editor = new Editor({
      extensions: [
        Document,
        Paragraph,
        Text,
        TextCheckMark,
        TextCheckExtension,
      ],
      content: {
        type: "doc",
        content: [
          {
            type: "paragraph",
            content: [
              {
                type: "text",
                text: "word",
                marks: [
                  { type: TextCheckMark.name, attrs: textCheckMarkAttrs },
                ],
              },
            ],
          },
        ],
      },
    })

    editor.commands.deleteRange({ from: 2, to: 3 })

    expect(editor.getText()).toBe("wrd")

    const para = editor.state.doc.firstChild
    const textNode = para?.firstChild
    expect(textNode?.marks.length).toBe(0)
  })

  it("does not affect other marked words when editing one", () => {
    const editor = new Editor({
      extensions: [
        Document,
        Paragraph,
        Text,
        TextCheckMark,
        TextCheckExtension,
      ],
      content: {
        type: "doc",
        content: [
          {
            type: "paragraph",
            content: [
              {
                type: "text",
                text: "first",
                marks: [
                  {
                    type: TextCheckMark.name,
                    attrs: { ...textCheckMarkAttrs, id: "1" },
                  },
                ],
              },
              {
                type: "text",
                text: " ",
              },
              {
                type: "text",
                text: "second",
                marks: [
                  {
                    type: TextCheckMark.name,
                    attrs: { ...textCheckMarkAttrs, id: "2" },
                  },
                ],
              },
            ],
          },
        ],
      },
    })

    editor.commands.insertContentAt({ from: 2, to: 2 }, "X")

    const para = editor.state.doc.firstChild

    para?.forEach((node) => {
      if (node.text === "fXirst") {
        expect(node.marks.length).toBe(0)
      }
      // Second word should still have marks
      if (node.text === "second") {
        expect(node.marks.length).toBe(1)
      }
    })
  })
})
