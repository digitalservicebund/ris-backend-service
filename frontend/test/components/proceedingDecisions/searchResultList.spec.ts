import { render, screen } from "@testing-library/vue"
import SearchResultList, {
  SearchResult,
} from "@/components/proceedingDecisions/SearchResultList.vue"

function renderComponent() {
  const searchResults: SearchResult = [
    {
      decision: {
        court: { type: "fooType", location: "fooLocation" },
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
