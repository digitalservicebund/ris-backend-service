import { Editor } from "@tiptap/core"
import { Document } from "@tiptap/extension-document"
import { Paragraph } from "@tiptap/extension-paragraph"
import { Text } from "@tiptap/extension-text"
import { TextCheckExtension } from "@/editor/textCheckExtension"

describe("TextCheckExtension", () => {
  /* eslint-disable @typescript-eslint/no-explicit-any */
  let mockService: any
  let editor: Editor

  beforeEach(() => {
    mockService = {
      checkCategory: vi.fn(),
      handleSelection: vi.fn().mockReturnValue(true),
      selectMatch: vi.fn(),
      replaceMatch: vi.fn(),
      updatedMatchesInText: vi.fn(),
    }

    editor = new Editor({
      extensions: [
        Document,
        Paragraph,
        Text,
        TextCheckExtension.configure({
          service: mockService,
        }),
      ],
      content: "",
    })
  })

  it("runs textCheck command", () => {
    editor.commands.textCheck()
    expect(mockService.checkCategory).toHaveBeenCalled()
  })

  it("runs handleMatchSelection command", () => {
    editor.commands.handleMatchSelection()
    expect(mockService.handleSelection).toHaveBeenCalled()
  })

  it("runs setSelectedMatch command", () => {
    editor.commands.setSelectedMatch(123)
    expect(mockService.selectMatch).toHaveBeenCalledWith(123)
  })

  it("runs acceptMatch command with valid matchId", () => {
    editor.commands.acceptMatch(123, "word")
    expect(mockService.replaceMatch).toHaveBeenCalledWith(
      123,
      "word",
      expect.anything(),
      expect.anything(),
    )
  })

  it("runs updatedMatchesInText command", () => {
    editor.commands.updatedMatchesInText()
    expect(mockService.updatedMatchesInText).toHaveBeenCalled()
  })
})
