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

    const textCheckService = new NeurisTextCheckService()

    await textCheckService.checkCategory(mockEditor, "tenor")

    expect(store.updateDocumentUnit).toHaveBeenCalledTimes(1)
    expect(store.updateDocumentUnit).toHaveBeenCalledWith()
  })

  it("clear selected match sets it to undefined ", async () => {
    const textCheckService = new NeurisTextCheckService()

    textCheckService.selectedMatch.value = generateMatch()

    textCheckService.clearSelectedMatch()
    expect(textCheckService.selectedMatch.value).toBeUndefined()
  })

  it("resets selected match if not in matches, otherwise sets it", async () => {
    const textCheckService = new NeurisTextCheckService()

    const match = generateMatch()
    textCheckService.selectedMatch.value = match
    textCheckService.matches = [match]

    textCheckService.selectMatch(match.id)
    expect(textCheckService.selectedMatch.value).toEqual(match)
    textCheckService.selectedMatch.value = generateMatch()

    const matchWithOtherId = generateMatch(4)
    textCheckService.matches = [matchWithOtherId]

    textCheckService.selectMatch(match.id)
    expect(textCheckService.selectedMatch.value).toBeUndefined()
  })

  it.each(["global", "documentation_unit"] as const)(
    "removing a %s ignored word updates the selected match ignored words list accordingly",
    async (type: DocumentationType) => {
      const textCheckService = new NeurisTextCheckService()
      if (type === "global") {
        vi.spyOn(languageToolService, "removeGlobalIgnore").mockResolvedValue({
          status: 200,
          data: undefined,
        })
      } else if (type === "documentation_unit") {
        vi.spyOn(languageToolService, "removeLocalIgnore").mockResolvedValue({
          status: 200,
          data: undefined,
        })
      }

      const match = generateMatch()
      const ignoredWord: IgnoredTextCheckWord = generateIgnoredWord(type)
      match.ignoredTextCheckWords = [ignoredWord]
      textCheckService.selectedMatch.value = match

      expect(
        textCheckService.selectedMatch.value.ignoredTextCheckWords,
      ).toEqual([ignoredWord])

      if (type === "global") {
        await textCheckService.removeGloballyIgnoredWord(ignoredWord.word)
      } else if (type === "documentation_unit") {
        await textCheckService.removeIgnoredWord(ignoredWord.word)
      }

      expect(
        textCheckService.selectedMatch.value.ignoredTextCheckWords,
      ).toEqual([])
    },
  )

  it.each(["global", "documentation_unit"] as const)(
    "adding a %s ignored word updates the selected match ignored words list accordingly",
    async (type: DocumentationType) => {
      const textCheckService = new NeurisTextCheckService()

      if (type == "documentation_unit") {
        vi.spyOn(languageToolService, "removeLocalIgnore").mockResolvedValue({
          status: 200,
          data: undefined,
        })
      } else {
        vi.spyOn(languageToolService, "removeGlobalIgnore").mockResolvedValue({
          status: 200,
          data: undefined,
        })
      }

      const match = generateMatch()
      const ignoredWord = generateIgnoredWord(type) as IgnoredTextCheckWord
      match.ignoredTextCheckWords = []
      textCheckService.selectedMatch.value = match

      if (type == "global") {
        vi.spyOn(languageToolService, "addGlobalIgnore").mockResolvedValue({
          status: 200,
          data: ignoredWord,
        })
      } else {
        vi.spyOn(languageToolService, "addLocalIgnore").mockResolvedValue({
          status: 200,
          data: ignoredWord,
        })
      }

      expect(
        textCheckService.selectedMatch.value.ignoredTextCheckWords,
      ).toEqual([])

      if (type === "global") {
        await textCheckService.ignoreWordGlobally(ignoredWord.word)
      } else if (type === "documentation_unit") {
        await textCheckService.ignoreWord(ignoredWord.word)
      }

      expect(
        textCheckService.selectedMatch.value.ignoredTextCheckWords,
      ).toEqual([ignoredWord])
    },
  )
})
