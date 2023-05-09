import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import SearchResultList, {
  SearchResults,
} from "@/components/proceedingDecisions/SearchResultList.vue"
import { ProceedingDecision } from "@/domain/documentUnit"

function renderComponent(searchResults?: SearchResults) {
  const props: { searchResults: SearchResults } = {
    searchResults: searchResults ?? [
      {
        decision: new ProceedingDecision({
          ...{
            court: {
              type: "fooType",
              location: "fooLocation",
              label: "fooLabel",
            },
            documentNumber: "fooDocumentNumber",
          },
        }),
        isLinked: false,
      },
    ],
  }

  const utils = render(SearchResultList, {
    props,
    global: {
      stubs: { routerLink: { template: "<a><slot/></a>" } },
    },
  })
  const user = userEvent.setup()
  return { user, ...utils }
}

describe("ProceedingDecision SearchResult List", () => {
  it("renders correctly", async () => {
    renderComponent()

    expect(await screen.findByText("fooLabel, fooDocumentNumber")).toBeVisible()
    expect(await screen.findByText("Übernehmen")).toBeVisible()
    expect(screen.queryByText(/Bereits hinzugefügt/)).not.toBeInTheDocument()
  })

  it("indicates not yet added proceeding decisions", async () => {
    renderComponent()

    expect(await screen.findByText("Übernehmen")).toBeVisible()
    expect(screen.queryByText(/Bereits hinzugefügt/)).not.toBeInTheDocument()
  })

  it("indicates already added proceeding decisions", async () => {
    renderComponent([
      {
        decision: new ProceedingDecision({
          ...{
            court: {
              type: "fooType",
              location: "fooLocation",
              label: "fooLabel",
            },
            documentNumber: "fooDocumentNumber",
          },
        }),
        isLinked: true,
      },
    ])

    expect(await screen.findByText("Übernehmen")).toBeVisible()
    expect(await screen.findByText(/Bereits hinzugefügt/)).toBeVisible()
  })

  it("clicking on 'Übernehmen' emits link decision event", async () => {
    const { user, emitted } = renderComponent()

    expect(await screen.findByText("fooLabel, fooDocumentNumber")).toBeVisible()
    expect(await screen.findByText("Übernehmen")).toBeVisible()
    expect(screen.queryByText(/Bereits hinzugefügt/)).not.toBeInTheDocument()

    const button = await screen.findByText("Übernehmen")
    await user.click(button)

    expect(emitted().linkDecision).toBeTruthy()
  })
})
