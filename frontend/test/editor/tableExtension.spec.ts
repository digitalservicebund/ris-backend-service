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
})
