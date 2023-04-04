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
      isLinked: true,
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
  it("indicates already added proceeding decisions from search results", async () => {
    renderComponent()

    expect(await screen.findByText("Bereits hinzugefügt")).toBeVisible()
  })
})
