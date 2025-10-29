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

describe("removeTagsOnTyping extension", () => {
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

    editor.destroy()
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

    editor.destroy()
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

    editor.destroy()
  })

  it("when text check inserts custom tags/marks into the document then those tags/marks should be preserved", () => {
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
              },
            ],
          },
        ],
      },
    })

    // Verify initial state has no marks
    const paraBeforeMarking = editor.state.doc.firstChild
    const textNodeBeforeMarking = paraBeforeMarking?.firstChild
    expect(textNodeBeforeMarking?.marks.length).toBe(0)

    // Simulate text check adding marks without changing text content
    const textCheckMark = editor.schema.marks[TextCheckMark.name]
    const from = 1
    const to = 5

    editor.commands.command(({ tr }) => {
      tr.addMark(from, to, textCheckMark.create(textCheckMarkAttrs))
      return true
    })

    // Verify marks were added and preserved (plugin should not remove them)
    const paraAfterMarking = editor.state.doc.firstChild
    const textNodeAfterMarking = paraAfterMarking?.firstChild
    expect(textNodeAfterMarking?.marks.length).toBe(1)
    expect(textNodeAfterMarking?.marks[0].type.name).toBe(TextCheckMark.name)
    expect(editor.getText()).toBe("word")

    editor.destroy()
  })

  it("when the last character of the last word is deleted text check mark is also removed", () => {
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
                text: "wordabc",
              },
            ],
          },
        ],
      },
    })

    const textCheckMark = editor.schema.marks[TextCheckMark.name]
    const from = 1
    const to = 8

    editor.commands.command(({ tr }) => {
      tr.addMark(from, to, textCheckMark.create(textCheckMarkAttrs))
      return true
    })

    // Verify marks were added
    const paraBeforeDelete = editor.state.doc.firstChild
    const textNodeBeforeDelete = paraBeforeDelete?.firstChild

    expect(textNodeBeforeDelete?.marks.length).toBe(1)
    expect(textNodeBeforeDelete?.marks[0].type.name).toBe(TextCheckMark.name)
    expect(editor.getText()).toBe("wordabc")

    // Delete the last character by positioning cursor at end and deleting back one char
    const endPos = editor.state.doc.nodeSize - 2
    editor.commands.setTextSelection(endPos)
    editor.commands.deleteRange({ from: endPos - 2, to: endPos - 1 })

    expect(editor.getText()).toBe("wordab")

    // Verify the mark was removed after deletion
    const paraAfterDelete = editor.state.doc.firstChild
    const textNodeAfterDelete = paraAfterDelete?.firstChild

    expect(textNodeAfterDelete?.marks.length).toBe(0)

    editor.destroy()
  })

  it("when the last character of word in text is deleted then validate mark removal", () => {
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
                text: "firstword",
              },
              {
                type: "text",
                text: " ",
              },
              {
                type: "text",
                text: "secondword",
              },
              {
                type: "text",
                text: " ",
              },
              {
                type: "text",
                text: "thirdword",
              },
            ],
          },
        ],
      },
    })

    const textCheckMark = editor.schema.marks[TextCheckMark.name]
    const fromFirst = 1
    const toFirst = 10
    const fromSecond = 11
    const toSecond = 21
    const fromThird = 22
    const toThird = 31

    editor.commands.command(({ tr }) => {
      tr.addMark(
        fromFirst,
        toFirst,
        textCheckMark.create({ ...textCheckMarkAttrs, id: "1" }),
      )
      tr.addMark(
        fromSecond,
        toSecond,
        textCheckMark.create({ ...textCheckMarkAttrs, id: "2" }),
      )
      tr.addMark(
        fromThird,
        toThird,
        textCheckMark.create({ ...textCheckMarkAttrs, id: "3" }),
      )
      return true
    })

    expect(editor.getText()).toBe("firstword secondword thirdword")

    const paraBeforeDelete = editor.state.doc.firstChild
    let markedWordsCount = 0
    paraBeforeDelete?.forEach((node) => {
      if (node.isText && node.marks.length > 0) {
        markedWordsCount++
      }
    })
    expect(markedWordsCount).toBe(3)

    // Delete the last character 'd' from the second word "secondword"
    // "secondword" ends at position 21, so delete from position 20 to 21
    editor.commands.deleteRange({ from: 20, to: 21 })

    expect(editor.getText()).toBe("firstword secondwor thirdword")

    // Verify the mark was removed only from the second word
    const paragraphAfterDelete = editor.state.doc.firstChild
    paragraphAfterDelete?.forEach((node) => {
      if (node.isText) {
        if (node.text === "firstword") {
          expect(node.marks.length).toBe(1)
        } else if (node.text === "secondwor") {
          expect(node.marks.length).toBe(0)
        } else if (node.text === "thirdword") {
          expect(node.marks.length).toBe(1)
        }
      }
    })

    editor.destroy()
  })

  it("when a selection has multiple words when deleting or replacing it with a character then remove marks in all nodes around this change", () => {
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
                text: "wordss",
              },
            ],
          },
          {
            type: "paragraph",
            content: [
              {
                type: "text",
                text: "firstword",
              },
              {
                type: "text",
                text: " ",
              },
              {
                type: "text",
                text: "secondword",
              },
            ],
          },
        ],
      },
    })

    const textCheckMark = editor.schema.marks[TextCheckMark.name]
    const firstFrom = 8
    const firstTo = 18
    const secondFrom = 19
    const secondTo = 29

    editor.commands.command(({ tr }) => {
      tr.addMark(
        firstFrom,
        firstTo,
        textCheckMark.create({ ...textCheckMarkAttrs, id: "1" }),
      )
      tr.addMark(
        secondFrom,
        secondTo,
        textCheckMark.create({ ...textCheckMarkAttrs, id: "2" }),
      )
      return true
    })

    // Verify both words are marked
    const secondParagraph = editor.state.doc.child(1)
    let markedWordsCount = 0
    secondParagraph.forEach((node) => {
      if (node.marks.length > 0) {
        markedWordsCount++
      }
    })
    expect(markedWordsCount).toBe(2)
    expect(editor.getText()).toBe("wordss\n\nfirstword secondword")

    // Select "rd se" which spans across both marked words and replace with "x"
    // "firstwo[rd se]condword" -> "firstwo[x]condword"
    editor.commands.insertContentAt({ from: 16, to: 21 }, "x")

    expect(editor.getText()).toBe("wordss\n\nfirstwoxcondword")

    // Verify marks were removed from all affected text nodes
    const secondParagraphAfter = editor.state.doc.child(1)
    let markedWordsAfter = 0
    secondParagraphAfter.forEach((node) => {
      if (node.isText && node.marks.length > 0) {
        markedWordsAfter++
      }
    })
    expect(markedWordsAfter).toBe(0)

    editor.destroy()
  })

  it("multiple selectio and chnage does not remove marks in other nodes in the same row", () => {
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
                text: "wordss",
              },
            ],
          },
          {
            type: "paragraph",
            content: [
              {
                type: "text",
                text: "firstword",
              },
              {
                type: "text",
                text: " ",
              },
              {
                type: "text",
                text: "secondword",
              },
              {
                type: "text",
                text: " ",
              },
              {
                type: "text",
                text: "thirdword",
              },
              {
                type: "text",
                text: " ",
              },
              {
                type: "text",
                text: "fourthword",
              },
            ],
          },
        ],
      },
    })

    const textCheckMark = editor.schema.marks[TextCheckMark.name]
    const firstFrom = 8
    const firstTo = 18
    const secondFrom = 19
    const secondTo = 29
    const thirdFrom = 30
    const thirdTo = 40
    const fourthFrom = 41
    const fourthTo = 51

    editor.commands.command(({ tr }) => {
      tr.addMark(
        firstFrom,
        firstTo,
        textCheckMark.create({ ...textCheckMarkAttrs, id: "1" }),
      )
      tr.addMark(
        secondFrom,
        secondTo,
        textCheckMark.create({ ...textCheckMarkAttrs, id: "2" }),
      )
      tr.addMark(
        thirdFrom,
        thirdTo,
        textCheckMark.create({ ...textCheckMarkAttrs, id: "3" }),
      )
      tr.addMark(
        fourthFrom,
        fourthTo,
        textCheckMark.create({ ...textCheckMarkAttrs, id: "4" }),
      )
      return true
    })

    // Verify all words are marked
    const secondParagraph = editor.state.doc.child(1)
    let markedWordsCount = 0
    secondParagraph.forEach((node) => {
      if (node.marks.length > 0) {
        markedWordsCount++
      }
    })
    expect(markedWordsCount).toBe(4)
    expect(editor.getText()).toBe(
      "wordss\n\nfirstword secondword thirdword fourthword",
    )

    editor.commands.insertContentAt({ from: 27, to: 32 }, "x")

    expect(editor.getText()).toBe(
      "wordss\n\nfirstword secondwoxirdword fourthword",
    )

    // Verify marks were removed from all affected text nodes
    const secondParagraphAfter = editor.state.doc.child(1)
    let markedWordsAfter = 0
    secondParagraphAfter.forEach((node) => {
      if (node.isText && node.marks.length > 0) {
        markedWordsAfter++
      }
    })
    expect(markedWordsAfter).toBe(2)

    editor.destroy()
  })
})
