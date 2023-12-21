import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import SearchResultList, {
  SearchResults,
} from "@/components/SearchResultList.vue"
import ActiveCitation from "@/domain/activeCitation"
import ProceedingDecision from "@/domain/previousDecision"

function renderSearchResults(
  searchResults?: SearchResults<ProceedingDecision | ActiveCitation>,
  isLoading?: boolean,
) {
  const props: {
    searchResults: SearchResults<ProceedingDecision | ActiveCitation>
    isLoading: boolean
  } = {
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
            referencedDocumentationUnitId: "foo",
          },
        }),
        isLinked: false,
      },
    ],
    isLoading: isLoading ?? false,
  }

  const utils = render(SearchResultList, {
    props,
    global: {
      stubs: { routerLink: { template: "<a><slot/></a>" } },
    },
  })
  // eslint-disable-next-line testing-library/await-async-events
  const user = userEvent.setup()
  return { user, ...utils }
}

describe("ProceedingDecision SearchResult List", () => {
  it("renders correctly", async () => {
    renderSearchResults()

    expect(await screen.findByText("fooLabel, fooDocumentNumber")).toBeVisible()
    expect(await screen.findByText("Übernehmen")).toBeVisible()
    expect(screen.queryByText(/Bereits hinzugefügt/)).not.toBeInTheDocument()
  })

  it("indicates not yet added proceeding decisions", async () => {
    renderSearchResults()

    expect(await screen.findByText("Übernehmen")).toBeVisible()
    expect(screen.queryByText(/Bereits hinzugefügt/)).not.toBeInTheDocument()
  })

  it("indicates already added proceeding decisions", async () => {
    renderSearchResults([
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
    const { user, emitted } = renderSearchResults()

    expect(await screen.findByText("fooLabel, fooDocumentNumber")).toBeVisible()
    expect(await screen.findByText("Übernehmen")).toBeVisible()
    expect(screen.queryByText(/Bereits hinzugefügt/)).not.toBeInTheDocument()

    const button = await screen.findByText("Übernehmen")
    await user.click(button)

    expect(emitted().linkDecision).toBeTruthy()
  })

  it("renders search results for active citations", async () => {
    renderSearchResults([
      {
        decision: new ActiveCitation({
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

  it("renders loading spinner, when loading", async () => {
    renderSearchResults([], true)
    expect(await screen.findByLabelText("Ladestatus")).toBeVisible()
  })
})
