import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import PeriodicalEditionReferenceInput from "@/components/periodical-evaluation/references/PeriodicalEditionReferenceInput.vue"
import RelatedDocumentation from "@/domain/relatedDocumentation"
import documentUnitService from "@/services/documentUnitService"
import { onSearchShortcutDirective } from "@/utils/onSearchShortcutDirective"
import routes from "~/test-helper/routes"

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
        isSaved: false,
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
  })

  it("search is triggered with shortcut", async () => {
    vi.spyOn(console, "error").mockImplementation(() => null)
    const { user } = renderComponent()

    expect(screen.queryByText(/test fileNumber1/)).not.toBeInTheDocument()
    await user.type(await screen.findByLabelText("Aktenzeichen"), "test")
    await user.keyboard("{Control>}{Enter}")

    expect(screen.getAllByText(/test fileNumber1/).length).toBe(1)
    vi.restoreAllMocks()
  })

  test("adding a decision scrolls to reference on validation errors", async () => {
    const { user } = renderComponent()
    const searchButton = screen.getByLabelText("Nach Entscheidung suchen")
    const scrollToMock = vi.fn()
    window.scrollTo = scrollToMock
    await user.click(searchButton)

    const addDecision = screen.getByLabelText("Treffer übernehmen")
    await user.click(addDecision)

    // scrollTo called on mounted, on searchresults mounted and on validation failed
    expect(
      scrollToMock,
      "Adding a reference with missing required fields should scroll to entry",
    ).toHaveBeenCalledTimes(3)
  })
})
