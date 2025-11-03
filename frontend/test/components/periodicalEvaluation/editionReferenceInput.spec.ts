import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { setActivePinia } from "pinia"
import { createRouter, createWebHistory } from "vue-router"
import PeriodicalEditionReferenceInput from "@/components/periodical-evaluation/references/PeriodicalEditionReferenceInput.vue"
import RelatedDocumentation from "@/domain/relatedDocumentation"
import documentUnitService from "@/services/documentUnitService"
import featureToggleService from "@/services/featureToggleService"
import { onSearchShortcutDirective } from "@/utils/onSearchShortcutDirective"
import routes from "~/test-helper/routes"

// Mock the stores
vi.mock("@/stores/documentUnitStore", () => ({
  useDocumentUnitStore: vi.fn(),
}))

vi.mock("@/stores/extraContentSidePanelStore", () => ({
  useExtraContentSidePanelStore: vi.fn(),
}))

vi.mock("@/stores/useEditionStore", () => ({
  useEditionStore: vi.fn(),
}))

// Mock the useScroll composable globally
const scrollIntoViewportByIdMock = vi.fn()
const openSidePanelAndScrollToSectionMock = vi.fn()

vi.mock("@/composables/useScroll", () => ({
  useScroll: () => ({
    scrollIntoViewportById: scrollIntoViewportByIdMock,
    openSidePanelAndScrollToSection: openSidePanelAndScrollToSectionMock,
  }),
}))

function renderComponent() {
  const user = userEvent.setup()

  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  return {
    user,
    ...render(PeriodicalEditionReferenceInput, {
      props: {
        modelValueList: [],
      },
      global: {
        directives: { "ctrl-enter": onSearchShortcutDirective },
        plugins: [
          router,
          [
            createTestingPinia({
              initialState: {
                editionStore: undefined,
              },
              stubActions: false,
            }),
          ],
        ],
      },
    }),
  }
}

describe("Legal periodical edition reference input", () => {
  beforeEach(() => {
    // Activate Pinia
    setActivePinia(createTestingPinia())

    // Mock the searchByRelatedDocumentation method
    vi.spyOn(
      documentUnitService,
      "searchByRelatedDocumentation",
    ).mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: {
          content: [
            new RelatedDocumentation({
              uuid: "123",
              court: {
                type: "type1",
                location: "location1",
                label: "label1",
              },
              decisionDate: "2022-02-01",
              documentType: {
                jurisShortcut: "documentTypeShortcut1",
                label: "documentType1",
              },
              fileNumber: "test fileNumber1",
            }),
          ],
          size: 0,
          number: 0,
          numberOfElements: 20,
          first: true,
          last: false,
          empty: false,
        },
      }),
    )

    vi.spyOn(featureToggleService, "isEnabled").mockResolvedValue({
      status: 200,
      data: true,
    })
  })

  it("search is triggered with shortcut", async () => {
    const { user } = renderComponent()

    expect(screen.queryByText(/test fileNumber1/)).not.toBeInTheDocument()
    await user.type(await screen.findByLabelText("Aktenzeichen"), "test")
    await user.keyboard("{Control>}{Enter}")

    expect(screen.getAllByText(/test fileNumber1/).length).toBe(1)
    vi.resetAllMocks()
  })

  test("adding a decision scrolls to reference on validation errors", async () => {
    const { user } = renderComponent()

    await user.click(screen.getByLabelText("Nach Entscheidung suchen"))
    await user.click(screen.getByLabelText("Treffer übernehmen"))

    expect(scrollIntoViewportByIdMock).toHaveBeenCalledWith(
      "periodical-references",
    )

    // onmounted, on search results and on validation error
    expect(scrollIntoViewportByIdMock).toHaveBeenCalledTimes(3)
  })
})
