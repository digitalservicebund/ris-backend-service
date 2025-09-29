import { createTestingPinia } from "@pinia/testing"
import userEvent from "@testing-library/user-event"
import { render, screen, within } from "@testing-library/vue"
import { nextTick } from "vue"
import { createRouter, createWebHistory } from "vue-router"
import { Page } from "@/components/Pagination.vue"
import ResultList from "@/components/search/shared/ResultList.vue"
import { Kind } from "@/domain/documentationUnitKind"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import { PublicationState } from "@/domain/publicationStatus"
import featureToggleService from "@/services/featureToggleService"
import routes from "~/test-helper/routes"

const addToastMock = vi.fn()
vi.mock("primevue/usetoast", () => ({
  useToast: () => ({ add: addToastMock }),
}))

const mockEntries = [
  new DocumentUnitListEntry({
    uuid: "1",
    documentNumber: "ABC123",
    hasAttachments: true,
    hasHeadnoteOrPrinciple: false,
    isDeletable: true,
    isEditable: true,
    status: {
      publicationStatus: PublicationState.PUBLISHED,
      withError: false,
    },
    court: { label: "BGH", type: "BGH", location: "" },
    resolutionDate: "2025-05-06",
  }),
  new DocumentUnitListEntry({
    uuid: "2",
    documentNumber: "DEF456",
    hasAttachments: false,
    hasHeadnoteOrPrinciple: true,
    note: "",
    isDeletable: false,
    isEditable: false,
    status: {
      publicationStatus: PublicationState.UNPUBLISHED,
      withError: true,
    },
    currentDocumentationUnitProcessStep: getCurrentProcessStep(),
    previousProcessStep: {
      uuid: "1",
      name: "Ersterfassung",
      abbreviation: "EF",
    },
    resolutionDate: "2000-04-06",
  }),
]

function getCurrentProcessStep() {
  return {
    user: {
      id: "2",
      initials: "TN",
      name: "Test Name",
    },
    processStep: {
      uuid: "2",
      name: "QS Formal",
      abbreviation: "QS",
    },
  }
}

const pageEntries: Page<DocumentUnitListEntry> = {
  content: mockEntries,
  size: 2,
  number: 0,
  numberOfElements: 200,
  first: true,
  last: false,
  empty: false,
}

function renderComponent(props?: {
  kind: Kind
  pageEntries?: Page<DocumentUnitListEntry>
  loading?: boolean
  showPublicationDate?: boolean
}) {
  const user = userEvent.setup()

  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })

  return {
    user,
    ...render(ResultList, {
      props: {
        kind: props?.kind || Kind.DECISION,
        pageEntries: props?.pageEntries || pageEntries,
        loading: props?.loading || false,
        showPublicationDate: props?.showPublicationDate || false,
      },
      global: {
        directives: {
          tooltip: {},
        },
        plugins: [[router, createTestingPinia()]],
      },
    }),
  }
}

describe("Search Result List", () => {
  beforeEach(() => {
    vi.useFakeTimers()
    vi.resetAllMocks()
    vi.spyOn(featureToggleService, "isEnabled").mockResolvedValue({
      status: 200,
      data: true,
    })
  })

  afterEach(() => {
    vi.useRealTimers()
  })
  it("renders document numbers", () => {
    renderComponent()
    const rows = screen.getAllByRole("row")
    expect(rows[1]).toHaveTextContent("ABC123")
    expect(rows[2]).toHaveTextContent("DEF456")
  })

  it("renders a selectable checkbox in the first row", async () => {
    renderComponent()
    // Wait for the asynchronous onMounted hook to complete
    // might be deleted after feature toggle removed
    await vi.runAllTimersAsync()
    await nextTick()

    const rows = screen.getAllByRole("row")
    const firstDataRow = rows[1]

    const checkbox = within(firstDataRow).getByRole("checkbox")
    expect(checkbox).toBeInTheDocument()
  })

  it("displays current user of process step", () => {
    renderComponent({ kind: Kind.DECISION })
    const rowWithProcessStep = screen.getAllByRole("row")[2]
    expect(rowWithProcessStep).toHaveTextContent("QS Formal")
    expect(rowWithProcessStep).toHaveTextContent("TN")
  })

  it("displays scheduledPublicationDateTime/lastHandoverDateTime", async () => {
    const entryWithScheduledPublication = new DocumentUnitListEntry({
      uuid: "3",
      scheduledPublicationDateTime: "2025-12-31T17:45:00Z",
      lastHandoverDateTime: undefined,
    })

    const entryWithLastHandover = new DocumentUnitListEntry({
      uuid: "4",
      scheduledPublicationDateTime: undefined,
      lastHandoverDateTime: "2022-02-01T06:00:00Z",
    })

    const pageEntries: Page<DocumentUnitListEntry> = {
      content: [entryWithScheduledPublication, entryWithLastHandover],
      size: 2,
      number: 0,
      numberOfElements: 2,
      first: true,
      last: false,
      empty: false,
    }

    renderComponent({
      kind: Kind.DECISION,
      pageEntries,
      showPublicationDate: true,
    })

    expect(screen.getByText(/31\.12\.2025 18:45/)).toBeInTheDocument()
    expect(screen.getByText(/01\.02\.2022 07:00/)).toBeInTheDocument()
  })
})
