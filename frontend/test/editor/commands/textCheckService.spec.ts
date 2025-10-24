import { createTestingPinia } from "@pinia/testing"
import { EditorState } from "@tiptap/pm/state"
import { Editor } from "@tiptap/vue-3"
import { setActivePinia } from "pinia"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import { Decision } from "@/domain/decision"
import { NeurisTextCheckService } from "@/editor/commands/textCheckCommands"
import languageToolService from "@/services/textCheckService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import {
  DocumentationType,
  IgnoredTextCheckWord,
  Match,
} from "@/types/textCheck"
import {
  generateIgnoredWord,
  generateMatch,
} from "~/test-helper/text-check-service-mock"

describe("check category service", () => {
  const textCategory = "tenor"
  let store: ReturnType<typeof useDocumentUnitStore>
  const updateDocumentUnitMock = vi.fn()

  beforeEach(() => {
    vi.spyOn(languageToolService, "checkCategory").mockResolvedValue({
      status: 200,
      data: {
        htmlText: "",
        matches: [],
      },
    })

    setActivePinia(
      createTestingPinia({
        initialState: {
          docunitStore: {
            documentUnit: new Decision("foo", {
              documentNumber: "1234567891234",
            }),
          },
        },
        stubActions: false,
      }),
    )

    store = useDocumentUnitStore()
    vi.spyOn(store, "updateDocumentUnit").mockImplementation(
      updateDocumentUnitMock,
    )
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe("check category", () => {
    it("updates document unit before performing check", async () => {
      const mockEditor: Editor = {
        commands: { setContent: vi.fn() },
      } as unknown as Editor

      const textCheckService = new NeurisTextCheckService(textCategory)

      await textCheckService.checkCategory(mockEditor)

      expect(store.updateDocumentUnit).toHaveBeenCalledTimes(1)
      expect(store.updateDocumentUnit).toHaveBeenCalledWith()
    })
  })

  describe("match selection", () => {
    it("clears selected match", () => {
      const textCheckService = new NeurisTextCheckService(textCategory)
      textCheckService.selectedMatch.value = generateMatch()

      textCheckService.clearSelectedMatch()

      expect(textCheckService.selectedMatch.value).toBeUndefined()
    })

    it("resets selected match if not in matches", () => {
      const textCheckService = new NeurisTextCheckService(textCategory)
      textCheckService.selectedMatch.value = generateMatch()

      store.matches = new Map([["tenor", [generateMatch(4)]]])
      textCheckService.selectMatch(3)

      expect(textCheckService.selectedMatch.value).toBeUndefined()
    })

    it("sets selected match if contained in matches", () => {
      const textCheckService = new NeurisTextCheckService(textCategory)
      const match = generateMatch()

      store.matches = new Map([[textCategory, [match]]])
      textCheckService.selectMatch(match.id)

      expect(textCheckService.selectedMatch.value).toEqual(match)
    })
  })

  describe("ignored words", () => {
    describe("removing ignored words", () => {
      it.each(["global", "documentation_unit"] as const)(
        "removing a %s ignored word updates matches accordingly",
        async (type: DocumentationType) => {
          const match = generateMatch()
          const textCheckService = new NeurisTextCheckService(textCategory)

          store.matches = new Map([
            ["tenor", [match]],
            ["reasons", [match]],
          ])

          if (type === "global") {
            vi.spyOn(
              languageToolService,
              "removeGlobalIgnore",
            ).mockResolvedValue({
              status: 200,
              data: undefined,
            })

            await textCheckService.removeGloballyIgnoredWord(match.word)

            const expectedIgnoredWord = {
              type: "documentation_unit",
              word: "Rechtshcreibfehler",
            }

            expect(
              store.matches.get("tenor")![0].ignoredTextCheckWords,
            ).toEqual([expectedIgnoredWord])

            expect(
              store.matches.get("reasons")![0].ignoredTextCheckWords,
            ).toEqual([expectedIgnoredWord])
          } else {
            vi.spyOn(
              languageToolService,
              "removeLocalIgnore",
            ).mockResolvedValue({
              status: 200,
              data: undefined,
            })

            await textCheckService.removeIgnoredWord(match.word)

            expect(
              store.matches.get("tenor")![0].ignoredTextCheckWords,
            ).toEqual([{ type: "global", word: "Rechtshcreibfehler" }])
          }
        },
      )
    })

    describe("adding ignored words", () => {
      it.each(["global", "documentation_unit"] as const)(
        "adding a %s ignored word updates matches accordingly",
        async (type: DocumentationType) => {
          const textCheckService = new NeurisTextCheckService(textCategory)
          const match = generateMatch()
          match.ignoredTextCheckWords = []

          store.matches = new Map([
            ["tenor", [match]],
            ["reasons", [match]],
          ])

          const ignoredWord = generateIgnoredWord(type) as IgnoredTextCheckWord

          if (type === "global") {
            vi.spyOn(languageToolService, "addGlobalIgnore").mockResolvedValue({
              status: 200,
              data: generateIgnoredWord(type),
            })

            await textCheckService.ignoreWordGlobally(ignoredWord.word)

            const expectedResult = {
              id: "0dd15ae7-bece-4133-9eb3-e01563a39102",
              type: "global",
              word: "Rechtshcreibfehler",
            }

            expect(
              store.matches.get("tenor")![0].ignoredTextCheckWords,
            ).toEqual([expectedResult])

            expect(
              store.matches.get("reasons")![0].ignoredTextCheckWords,
            ).toEqual([expectedResult])
          } else {
            vi.spyOn(languageToolService, "addLocalIgnore").mockResolvedValue({
              status: 200,
              data: ignoredWord,
            })

            await textCheckService.ignoreWord(ignoredWord.word)

            const expectedResult = {
              id: "0dd15ae7-bece-4133-9eb3-e01563a39102",
              type: "documentation_unit",
              word: "Rechtshcreibfehler",
            }

            expect(
              store.matches.get("tenor")![0].ignoredTextCheckWords,
            ).toEqual([expectedResult])

            expect(
              store.matches.get("reasons")![0].ignoredTextCheckWords,
            ).toEqual([expectedResult])
          }
        },
      )
    })
  })

  describe("isTextCheckTagSelected", () => {
    it("returns true when node at selection has textCheck mark", () => {
      const mockEditor: Editor = {
        state: {
          selection: {
            from: 5,
          },
          doc: {
            nodeAt: vi.fn().mockReturnValue({
              marks: [
                {
                  type: {
                    name: "textCheck",
                  },
                },
              ],
            }),
          },
        },
      } as unknown as Editor

      const result = NeurisTextCheckService.isTextCheckTagSelected(mockEditor)

      expect(mockEditor.state.doc.nodeAt).toHaveBeenCalledWith(5)
      expect(result).toBe(true)
    })

    it("returns false when node has no marks", () => {
      const mockEditor: Editor = {
        state: {
          selection: {
            from: 5,
          },
          doc: {
            nodeAt: vi.fn().mockReturnValue({
              marks: [],
            }),
          },
        },
      } as unknown as Editor

      const result = NeurisTextCheckService.isTextCheckTagSelected(mockEditor)

      expect(result).toBe(false)
    })

    it("returns false when node is null", () => {
      const mockEditor: Editor = {
        state: {
          selection: {
            from: 5,
          },
          doc: {
            nodeAt: vi.fn().mockReturnValue(null),
          },
        },
      } as unknown as Editor

      const result = NeurisTextCheckService.isTextCheckTagSelected(mockEditor)

      expect(result).toBe(false)
    })
  })

  describe("replaceMatch", () => {
    it("replaces text and clears selection when matching node is found", () => {
      const textCheckService = new NeurisTextCheckService(textCategory)
      const dispatch = vi.fn()
      const mockNode = {
        isText: true,
        nodeSize: 5,
        text: "oldText",
        marks: [
          {
            type: { name: "textCheck" },
            attrs: { id: "42" },
          },
        ],
      }

      const mockState = {
        schema: {
          text: vi.fn().mockReturnValue("newTextNode"),
        },
        tr: {
          replaceWith: vi.fn(),
        },
        doc: {
          descendants: vi.fn().mockImplementation((callback) => {
            callback(mockNode, 10) // pos = 10
          }),
        },
      }

      textCheckService.selectedMatch.value = generateMatch(42)
      textCheckService.replaceMatch(
        42,
        "newText",
        mockState as unknown as EditorState,
        dispatch,
      )

      // Verify the replacement was done correctly
      expect(mockState.schema.text).toHaveBeenCalledWith("newText")
      expect(mockState.tr.replaceWith).toHaveBeenCalledWith(
        10,
        15,
        "newTextNode",
      ) // pos + nodeSize = 15
      expect(dispatch).toHaveBeenCalledWith(mockState.tr)
      expect(textCheckService.selectedMatch.value).toBeUndefined()
    })

    it("does not replace text when no matching node is found", () => {
      const textCheckService = new NeurisTextCheckService(textCategory)
      const dispatch = vi.fn()
      const mockNode = {
        isText: true,
        marks: [
          {
            type: { name: "textCheck" },
            attrs: { id: "43" }, // Different ID
          },
        ],
      }

      const mockState = {
        schema: {
          text: vi.fn(),
        },
        tr: {
          replaceWith: vi.fn(),
        },
        doc: {
          descendants: vi.fn().mockImplementation((callback) => {
            callback(mockNode, 0)
          }),
        },
      }

      textCheckService.replaceMatch(
        42,
        "newText",
        mockState as unknown as EditorState,
        dispatch,
      )

      expect(mockState.schema.text).not.toHaveBeenCalled()
      expect(mockState.tr.replaceWith).not.toHaveBeenCalled()
      expect(dispatch).toHaveBeenCalledWith(mockState.tr)
    })

    it("handles non-text nodes correctly", () => {
      const textCheckService = new NeurisTextCheckService(textCategory)
      const dispatch = vi.fn()
      const mockNode = {
        isText: false,
        marks: [
          {
            type: { name: "textCheck" },
            attrs: { id: "42" },
          },
        ],
      }

      const mockState = {
        schema: {
          text: vi.fn(),
        },
        tr: {
          replaceWith: vi.fn(),
        },
        doc: {
          descendants: vi.fn().mockImplementation((callback) => {
            callback(mockNode, 0)
          }),
        },
      }

      textCheckService.replaceMatch(
        42,
        "newText",
        mockState as unknown as EditorState,
        dispatch,
      )

      expect(mockState.schema.text).not.toHaveBeenCalled()
      expect(mockState.tr.replaceWith).not.toHaveBeenCalled()
      expect(dispatch).toHaveBeenCalledWith(mockState.tr)
    })
  })

  describe("updateIgnoredMark", () => {
    it("does nothing when match is undefined", () => {
      const textCheckService = new NeurisTextCheckService(textCategory)
      const dispatch = vi.fn()
      const mockState = {
        schema: { text: vi.fn() },
        tr: { replaceWith: vi.fn() },
        doc: { descendants: vi.fn() },
      }

      textCheckService.updateIgnoredMark(
        undefined as unknown as Match,
        mockState as unknown as EditorState,
        dispatch,
      )

      expect(mockState.doc.descendants).not.toHaveBeenCalled()
      expect(dispatch).not.toHaveBeenCalled()
    })

    it("updates mark and dispatches when ignored status changes", () => {
      const textCheckService = new NeurisTextCheckService(textCategory)
      const dispatch = vi.fn()
      const mark = {
        type: {
          name: "textCheck",
          create: vi.fn().mockReturnValue("updatedMark"),
        },
        attrs: {
          id: "42",
          ignored: false,
        },
      }
      const mockNode = {
        isText: true,
        nodeSize: 5,
        text: "oldText",
        marks: [mark],
      }

      const mockState = {
        schema: {
          text: vi.fn().mockReturnValue("newNode"),
        },
        tr: {
          replaceWith: vi.fn(),
          docChanged: true,
        },
        doc: {
          descendants: vi.fn().mockImplementation((callback) => {
            callback(mockNode, 10)
          }),
        },
      }

      const match = generateMatch(42)
      match.ignoredTextCheckWords = [{ type: "global", word: "oldText" }]

      textCheckService.updateIgnoredMark(
        match,
        mockState as unknown as EditorState,
        dispatch,
      )

      expect(mark.type.create).toHaveBeenCalledWith({
        ...mark.attrs,
        ignored: true,
      })
      expect(mockState.schema.text).toHaveBeenCalledWith("oldText", [
        "updatedMark",
      ])
      expect(mockState.tr.replaceWith).toHaveBeenCalledWith(10, 15, "newNode")
      expect(dispatch).toHaveBeenCalledWith(mockState.tr)
    })

    it("does not update mark when ignored status hasn't changed", () => {
      const textCheckService = new NeurisTextCheckService(textCategory)
      const dispatch = vi.fn()
      const mark = {
        type: { name: "textCheck" },
        attrs: {
          id: "42",
          ignored: true,
        },
      }
      const mockNode = {
        marks: [mark],
        text: "oldText",
      }

      const mockState = {
        schema: { text: vi.fn() },
        tr: {
          replaceWith: vi.fn(),
          docChanged: false,
        },
        doc: {
          descendants: vi.fn().mockImplementation((callback) => {
            callback(mockNode, 0)
          }),
        },
      }

      const match = generateMatch(42)
      match.ignoredTextCheckWords = [{ type: "global", word: "oldText" }]

      textCheckService.updateIgnoredMark(
        match,
        mockState as unknown as EditorState,
        dispatch,
      )

      expect(mockState.tr.replaceWith).not.toHaveBeenCalled()
      expect(dispatch).not.toHaveBeenCalled()
    })

    it("does not update when no matching mark is found", () => {
      const textCheckService = new NeurisTextCheckService(textCategory)
      const dispatch = vi.fn()
      const mockNode = {
        marks: [
          {
            type: { name: "textCheck" },
            attrs: { id: "43" }, // Different ID
          },
        ],
      }

      const mockState = {
        schema: { text: vi.fn() },
        tr: {
          replaceWith: vi.fn(),
          docChanged: false,
        },
        doc: {
          descendants: vi.fn().mockImplementation((callback) => {
            callback(mockNode, 0)
          }),
        },
      }

      const match = generateMatch(42)
      textCheckService.updateIgnoredMark(
        match,
        mockState as unknown as EditorState,
        dispatch,
      )

      expect(mockState.tr.replaceWith).not.toHaveBeenCalled()
      expect(dispatch).not.toHaveBeenCalled()
    })
  })

  describe("static utilities", () => {
    describe("isMatchedIgnored", () => {
      it("returns true when match has ignored words", () => {
        const match = generateMatch()
        match.ignoredTextCheckWords = [{ type: "global", word: "test" }]

        expect(NeurisTextCheckService["isMatchedIgnored"](match)).toBe(true)
      })

      it("returns false when match has no ignored words", () => {
        const match = generateMatch()
        match.ignoredTextCheckWords = []
        match.isIgnoredOnce = false

        expect(NeurisTextCheckService["isMatchedIgnored"](match)).toBe(false)
      })

      it("returns true when match has no ignored words but locally ignored word", () => {
        const match = generateMatch()
        match.ignoredTextCheckWords = []
        match.isIgnoredOnce = true

        expect(NeurisTextCheckService["isMatchedIgnored"](match)).toBe(true)
      })

      it("returns false when ignoredTextCheckWords is undefined and no locally ignored words", () => {
        const match = generateMatch()
        match.ignoredTextCheckWords = undefined
        match.isIgnoredOnce = false

        expect(NeurisTextCheckService["isMatchedIgnored"](match)).toBe(false)
      })
    })

    describe("findTextCheckMark", () => {
      it("finds mark with matching id", () => {
        const node = {
          marks: [
            {
              type: { name: "other" },
              attrs: { id: "42" },
            },
            {
              type: { name: "textCheck" },
              attrs: { id: "42" },
            },
          ],
        }

        const result = NeurisTextCheckService["findTextCheckMark"](node, 42)
        expect(result).toBeDefined()
        expect(result.type.name).toBe("textCheck")
        expect(result.attrs.id).toBe("42")
      })

      it("finds any textCheck mark when no id specified", () => {
        const node = {
          marks: [
            {
              type: { name: "textCheck" },
              attrs: { id: "42" },
            },
          ],
        }

        const result = NeurisTextCheckService["findTextCheckMark"](node)
        expect(result).toBeDefined()
        expect(result.type.name).toBe("textCheck")
      })

      it("returns undefined when no matching mark found", () => {
        const node = {
          marks: [
            {
              type: { name: "textCheck" },
              attrs: { id: "43" },
            },
          ],
        }

        const result = NeurisTextCheckService["findTextCheckMark"](node, 42)
        expect(result).toBeUndefined()
      })

      it("returns undefined when marks array is empty", () => {
        const node = { marks: [] }

        const result = NeurisTextCheckService["findTextCheckMark"](node, 42)
        expect(result).toBeUndefined()
      })

      it("returns undefined when marks is undefined", () => {
        const node = { marks: undefined }

        const result = NeurisTextCheckService["findTextCheckMark"](node, 42)
        expect(result).toBeUndefined()
      })
    })
  })

  describe("updating matches in text", () => {
    it("calls updateIgnoredMark for each match in the specific category", () => {
      // Arrange
      const textCheckService = new NeurisTextCheckService(textCategory)
      const mockState = {
        doc: {
          descendants: vi.fn(),
        },
        tr: {
          replaceWith: vi.fn(),
        },
        schema: {
          text: vi.fn(),
        },
      } as unknown as EditorState
      const mockDispatch = vi.fn()
      const updateIgnoredMarkSpy = vi.spyOn(
        textCheckService,
        "updateIgnoredMark",
      )

      const match1 = generateMatch(1)
      const match2 = generateMatch(2)
      // Add matches for the service's category and another one to ensure it's specific
      store.matches = new Map([
        [textCategory, [match1, match2]],
        ["Gründe", [generateMatch(3)]],
      ])

      // Act
      textCheckService.updatedMatchesInText(mockState, mockDispatch)

      // Assert
      expect(updateIgnoredMarkSpy).toHaveBeenCalledTimes(2)
      expect(updateIgnoredMarkSpy).toHaveBeenCalledWith(
        match1,
        mockState,
        mockDispatch,
      )
      expect(updateIgnoredMarkSpy).toHaveBeenCalledWith(
        match2,
        mockState,
        mockDispatch,
      )
    })

    it("does not call updateIgnoredMark when no matches exist for the category", () => {
      // Arrange
      const textCheckService = new NeurisTextCheckService(textCategory)
      const mockState = {} as EditorState
      const mockDispatch = vi.fn()
      const updateIgnoredMarkSpy = vi.spyOn(
        textCheckService,
        "updateIgnoredMark",
      )

      // Store has no matches for the 'tenor' category
      store.matches = new Map([["anotherCategory", [generateMatch(1)]]])

      // Act
      textCheckService.updatedMatchesInText(mockState, mockDispatch)

      // Assert
      expect(updateIgnoredMarkSpy).not.toHaveBeenCalled()
    })
  })
})
