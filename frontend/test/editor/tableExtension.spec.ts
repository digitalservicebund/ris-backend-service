import { Document } from "@tiptap/extension-document"
import { TableCell } from "@tiptap/extension-table-cell"
import { TableHeader } from "@tiptap/extension-table-header"
import { TableRow } from "@tiptap/extension-table-row"
import { Text } from "@tiptap/extension-text"
import { Editor } from "@tiptap/vue-3"
import { CustomTable } from "@/editor/table"

describe("TableStyle extension", () => {
  test("should not transform inline styling for table tag", async () => {
    const editor = new Editor({
      element: document.createElement("div"),
      content:
        '<table style="border: 1px solid red"><tr><td>foo</td></tr></table>',
      extensions: [
        CustomTable,
        Document,
        Text,
        TableCell,
        TableHeader,
        TableRow,
      ],
    })

    expect(editor.getHTML()).toContain('style="border: 1px solid red')
  })

  test("should not transform inline styling for tr tag", async () => {
    const editor = new Editor({
      element: document.createElement("div"),
      content:
        '<table><tr style="border: 1px solid red"><td>foo</td></tr></table>',
      extensions: [
        CustomTable,
        Document,
        Text,
        TableCell,
        TableHeader,
        TableRow,
      ],
    })

    expect(editor.getHTML()).toContain('style="border: 1px solid red;"')
  })

  test("should not transform inline styling for td tag", async () => {
    const editor = new Editor({
      element: document.createElement("div"),
      content:
        '<table><tr><td style="border: 1px solid red">foo</td></tr></table>',
      extensions: [
        CustomTable,
        Document,
        Text,
        TableCell,
        TableHeader,
        TableRow,
      ],
    })

    expect(editor.getHTML()).toContain('style="border: 1px solid red;"')
  })

  test("should show table border", async () => {
    const editor = new Editor({
      element: document.createElement("div"),
      content:
        '<table style="border: 1px solid blue"><tr><td style="border: 1px solid red">foo</td></tr></table>',
      extensions: [
        CustomTable,
        Document,
        Text,
        TableCell,
        TableHeader,
        TableRow,
      ],
    })

    expect(editor.getHTML()).toContain("border: 1px solid blue")
  })

  test("should replace old border style", async () => {
    const editor = new Editor({
      element: document.createElement("div"),
      content: '<table border="1"><tr><td>foo</td><td>bar</td></tr></table>',
      extensions: [
        CustomTable,
        Document,
        Text,
        TableCell,
        TableHeader,
        TableRow,
      ],
    })

    expect(editor.getHTML()).not.toContain('border="1"')
    const count = (
      editor.getHTML().match(/border: 1px solid rgb\(0, 0, 0\)/g) || []
    ).length
    expect(count).toBe(2)
  })

  test("should replace old valign style", async () => {
    const editor = new Editor({
      element: document.createElement("div"),
      content:
        '<table><tr><td valign="top">foo</td><td valign="bottom">bar</td></tr></table>',
      extensions: [
        CustomTable,
        Document,
        Text,
        TableCell,
        TableHeader,
        TableRow,
      ],
    })

    expect(editor.getHTML()).not.toContain("valign")
    let count = (editor.getHTML().match(/vertical-align: top/g) || []).length
    expect(count).toBe(1)
    count = (editor.getHTML().match(/vertical-align: bottom/g) || []).length
    expect(count).toBe(1)
  })
})
