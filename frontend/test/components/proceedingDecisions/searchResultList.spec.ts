import { render, screen } from "@testing-library/vue"
import SearchResultList, {
  SearchResults,
} from "@/components/proceedingDecisions/SearchResultList.vue"

function renderComponent() {
  const searchResults: SearchResults = [
    {
      decision: {
        court: { type: "fooType", location: "fooLocation", label: "fooLabel" },
        documentNumber: "fooDocumentNumber",
      },
      isLinked: false,
    },
  ]

  return render(SearchResultList, {
    props: { searchResults },
    global: {
      stubs: { routerLink: { template: "<a><slot/></a>" } },
    },
  })
}

describe("ProceedingDecision SearchResult List", () => {
  it("indicates not yet added proceeding decisions from search results", async () => {
    renderComponent()

    expect(await screen.findByText("Übernehmen")).toBeVisible()
    expect(screen.queryByText("Bereits hinzugefügt")).not.toBeInTheDocument()
  })
})
