import { Table } from "@tiptap/extension-table"
import { TableCell } from "@tiptap/extension-table-cell"
import { TableHeader } from "@tiptap/extension-table-header"
import { TableRow } from "@tiptap/extension-table-row"
import StarterKit from "@tiptap/starter-kit"
import { Editor } from "@tiptap/vue-3"
import { TableStyle } from "@/editor/tableStyle"

describe("TableStyle extension", () => {
  test("should not transform inline styling", async () => {
    const editor = new Editor({
      element: document.createElement("div"),
      content:
        '<table style="border: 1px solid red"><tr><td>foo</td></tr></table>',
      extensions: [
        StarterKit,
        Table,
        TableCell,
        TableHeader,
        TableRow,
        TableStyle,
      ],
    })

    expect(editor.getHTML()).toContain('style="border: 1px solid red"')
  })
})
