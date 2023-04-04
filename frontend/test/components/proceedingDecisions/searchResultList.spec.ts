import { render, screen } from "@testing-library/vue"
import SearchResultList, {
  SearchResults,
} from "@/components/proceedingDecisions/SearchResultList.vue"

function renderComponent(searchResults?: SearchResults) {
  const props: { searchResults: SearchResults } = {
    searchResults: searchResults ?? [
      {
        decision: {
          court: {
            type: "fooType",
            location: "fooLocation",
            label: "fooLabel",
          },
          documentNumber: "fooDocumentNumber",
        },
        isLinked: false,
      },
    ],
  }

  return render(SearchResultList, {
    props,
    global: {
      stubs: { routerLink: { template: "<a><slot/></a>" } },
    },
  })
}

describe("ProceedingDecision SearchResult List", () => {
  it("indicates not yet added proceeding decisions", async () => {
    renderComponent()

    expect(await screen.findByText("Übernehmen")).toBeVisible()
    expect(screen.queryByText(/Bereits hinzugefügt/)).not.toBeInTheDocument()
  })

  it("indicates alreadt added proceeding decisions", async () => {
    renderComponent([
      {
        decision: {
          court: {
            type: "fooType",
            location: "fooLocation",
            label: "fooLabel",
          },
          documentNumber: "fooDocumentNumber",
        },
        isLinked: true,
      },
    ])

    expect(await screen.findByText("Übernehmen")).toBeVisible() //todo change this
    expect(await screen.findByText(/Bereits hinzugefügt/)).toBeVisible()
  })
})
