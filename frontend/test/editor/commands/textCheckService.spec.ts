import { createTestingPinia } from "@pinia/testing"
import { Editor } from "@tiptap/vue-3"
import { setActivePinia } from "pinia"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import DocumentUnit from "@/domain/documentUnit"
import { NeurisTextCheckService } from "@/editor/commands/textCheckCommands"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

describe("check category service", () => {
  let store: ReturnType<typeof useDocumentUnitStore>
  const updateDocumentUnitMock = vi.fn()

  beforeEach(() => {
    setActivePinia(
      createTestingPinia({
        initialState: {
          docunitStore: {
            documentUnit: new DocumentUnit("foo", {
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
})
