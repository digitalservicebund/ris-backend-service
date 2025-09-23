import { createTestingPinia } from "@pinia/testing"
import { Editor } from "@tiptap/vue-3"
import { setActivePinia } from "pinia"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import { Decision } from "@/domain/decision"
import { NeurisTextCheckService } from "@/editor/commands/textCheckCommands"
import languageToolService from "@/services/textCheckService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import { DocumentationType, IgnoredTextCheckWord } from "@/types/textCheck"
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

  it("text check category update documentation unit before check", async () => {
    const mockEditor: Editor = {
      commands: {
        setContent: vi.fn(),
      },
    } as unknown as Editor

    const textCheckService = new NeurisTextCheckService(textCategory)

    await textCheckService.checkCategory(mockEditor)

    expect(store.updateDocumentUnit).toHaveBeenCalledTimes(1)
    expect(store.updateDocumentUnit).toHaveBeenCalledWith()
  })

  it("clear selected match sets it to undefined ", async () => {
    const textCheckService = new NeurisTextCheckService(textCategory)

    textCheckService.selectedMatch.value = generateMatch()

    textCheckService.clearSelectedMatch()
    expect(textCheckService.selectedMatch.value).toBeUndefined()
  })

  it("resets selected match if not in matches", async () => {
    const textCheckService = new NeurisTextCheckService(textCategory)

    textCheckService.selectedMatch.value = generateMatch()
    expect(textCheckService.selectedMatch.value).toBeDefined()

    store.matches = new Map([["tenor", [generateMatch(4)]]])

    textCheckService.selectMatch(3)
    expect(textCheckService.selectedMatch.value).toBeUndefined()
  })

  it("set selected match if contained in matches", async () => {
    const textCheckService = new NeurisTextCheckService(textCategory)

    const match = generateMatch()
    expect(textCheckService.selectedMatch.value).toBeUndefined()
    store.matches = new Map([[textCategory, [match]]])

    textCheckService.selectMatch(match.id)
    expect(textCheckService.selectedMatch.value).toEqual(match)
  })

  it.each(["global", "documentation_unit"] as const)(
    "removing a %s ignored word updates the all ignored words in matches accordingly",
    async (type: DocumentationType) => {
      const match = generateMatch()

      expect(match.ignoredTextCheckWords).toEqual([
        {
          type: "documentation_unit",
          word: "Rechtshcreibfehler",
        },
        {
          type: "global",
          word: "Rechtshcreibfehler",
        },
      ])

      const textCheckService = new NeurisTextCheckService(textCategory)
      store.matches = new Map([
        ["tenor", [match]],
        ["reasons", [match]],
      ])

      if (type === "global") {
        vi.spyOn(languageToolService, "removeGlobalIgnore").mockResolvedValue({
          status: 200,
          data: undefined,
        })

        await textCheckService.removeGloballyIgnoredWord(match.word)

        expect(store.matches.get("tenor")![0].ignoredTextCheckWords).toEqual([
          {
            type: "documentation_unit",
            word: "Rechtshcreibfehler",
          },
        ])

        expect(store.matches.get("reasons")![0].ignoredTextCheckWords).toEqual([
          {
            type: "documentation_unit",
            word: "Rechtshcreibfehler",
          },
        ])
      } else if (type === "documentation_unit") {
        vi.spyOn(languageToolService, "removeLocalIgnore").mockResolvedValue({
          status: 200,
          data: undefined,
        })
        await textCheckService.removeIgnoredWord(match.word)

        expect(store.matches.get("tenor")![0].ignoredTextCheckWords).toEqual([
          {
            type: "global",
            word: "Rechtshcreibfehler",
          },
        ])

        expect(store.matches.get("reasons")![0].ignoredTextCheckWords).toEqual([
          {
            type: "global",
            word: "Rechtshcreibfehler",
          },
        ])
      } else {
        throw new Error("ignored type is not supported")
      }
    },
  )

  it.each(["global", "documentation_unit"] as const)(
    "adding a %s ignored word updates the selected match ignored words list accordingly",
    async (type: DocumentationType) => {
      const textCheckService = new NeurisTextCheckService(textCategory)
      const match = generateMatch()
      match.ignoredTextCheckWords = []

      store.matches = new Map([
        ["tenor", [match]],
        ["reasons", [match]],
      ])

      const ignoredWord = generateIgnoredWord(type) as IgnoredTextCheckWord

      if (type == "global") {
        vi.spyOn(languageToolService, "addGlobalIgnore").mockResolvedValue({
          status: 200,
          data: generateIgnoredWord(type) as IgnoredTextCheckWord,
        })
        await textCheckService.ignoreWordGlobally(ignoredWord.word)

        expect(store.matches.get("tenor")![0].ignoredTextCheckWords).toEqual([
          {
            id: "0dd15ae7-bece-4133-9eb3-e01563a39102",
            type: "global",
            word: "Rechtshcreibfehler",
          },
        ])
      } else if (type == "documentation_unit") {
        vi.spyOn(languageToolService, "addLocalIgnore").mockResolvedValue({
          status: 200,
          data: ignoredWord,
        })
        await textCheckService.ignoreWord(ignoredWord.word)

        expect(store.matches.get("tenor")![0].ignoredTextCheckWords).toEqual([
          {
            id: "0dd15ae7-bece-4133-9eb3-e01563a39102",
            type: "documentation_unit",
            word: "Rechtshcreibfehler",
          },
        ])
      } else {
        throw new Error("ignored type is not supported")
      }
    },
  )
})
