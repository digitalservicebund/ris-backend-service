import { createTestingPinia } from "@pinia/testing"
import { render } from "@testing-library/vue"
import { setActivePinia } from "pinia"
import { describe } from "vitest"
import DocumentationUnitTextCheckSummary from "@/components/text-check/DocumentationUnitTextCheckSummary.vue"
import { Decision } from "@/domain/decision"
import languageToolService from "@/services/textCheckService"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

const checkAllResponse = {
  status: 200,
  data: { suggestions: [], categoryTypes: [], totalTextCheckErrors: 0 },
  error: undefined,
}
const mockLangToolCheck = vi.spyOn(languageToolService, "checkAll")

async function renderComponent() {
  return {
    ...render(DocumentationUnitTextCheckSummary, {}),
  }
}

describe("Documentation Text Check Summary", () => {
  beforeEach(async () => {
    setActivePinia(createTestingPinia())
    mockLangToolCheck.mockResolvedValue(checkAllResponse)
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  it("updates Document Unit before initiating check all", async () => {
    const mockedStore = useDocumentUnitStore()
    mockedStore.documentUnit = new Decision("test-uuid", {})
    mockedStore.updateDocumentUnit = vi.fn().mockResolvedValue(undefined)

    await renderComponent()

    expect(mockedStore.updateDocumentUnit).toHaveBeenCalledBefore(
      mockLangToolCheck,
    )
  })

  it("nothing is checked or updated if documentUnit not in store", async () => {
    const mockedStore = useDocumentUnitStore()
    mockedStore.documentUnit = undefined

    await renderComponent()

    expect(mockedStore.updateDocumentUnit).toHaveBeenCalledTimes(0)
    expect(mockLangToolCheck).toHaveBeenCalledTimes(0)
  })
})
