import { createTestingPinia } from "@pinia/testing"
import userEvent from "@testing-library/user-event"
import {
  fireEvent,
  render,
  screen,
  waitFor,
  within,
} from "@testing-library/vue"
import { defineComponent, nextTick } from "vue"
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

vi.mock("@/components/BulkAssignProcessStep.vue", () => {
  const BulkAssignProcessStepMock = defineComponent({
    props: {
      documentationUnits: {
        type: Array,
        default: () => [],
      },
    },
    emits: ["updateSelectionErrors"],

    setup(_props, { emit }) {
      // Expose functions to trigger the two error scenarios
      const emitSelectionError = () => {
        emit(
          "updateSelectionErrors",
          "Wählen Sie mindestens eine Dokumentationseinheit aus.",
          [],
        )
      }

      return { emitSelectionError }
    },

    // Render button to manually trigger selection errors
    template: `
    <div data-testid="bulk-assign-mock">
        <span class="sr-only">Aktionen</span>
        <button 
            data-testid="trigger-no-selection-error" 
            @click="emitSelectionError"
        >
            Trigger Selection Error
        </button>
    </div>
  `,
  })

  return {
    default: BulkAssignProcessStepMock,
  }
})

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

  it("renders selection error in table header", async () => {
    renderComponent()

    await vi.runAllTimersAsync()
    await nextTick()

    const triggerButton = screen.getByTestId("trigger-no-selection-error")

    await fireEvent.click(triggerButton)
    await nextTick()

    await waitFor(() => {
      const errorMessageText = screen.getByText(
        "Wählen Sie mindestens eine Dokumentationseinheit aus.",
      )
      expect(errorMessageText).toBeInTheDocument()
    })
  })

  it("adjusts sticky header position when selection error is visible and scrolled", async () => {
    renderComponent()

    await vi.runAllTimersAsync()
    await nextTick()

    const tableWrapperElement = screen.getByTestId("search-result-list")

    vi.spyOn(tableWrapperElement, "getBoundingClientRect").mockReturnValue({
      top: -1,
      width: 0,
      height: 0,
      x: 0,
      y: 0,
      bottom: 0,
      left: 0,
      right: 0,
      toJSON: () => {},
    })

    const rowgroups = screen.getAllByRole("rowgroup")
    const thead = rowgroups[0]

    globalThis.dispatchEvent(new Event("scroll"))
    await nextTick()

    const triggerButton = screen.getByTestId("trigger-no-selection-error")
    await fireEvent.click(triggerButton)
    await nextTick()

    await waitFor(() => {
      expect(thead).toHaveStyle({ top: "60px" })
    })

    vi.spyOn(tableWrapperElement, "getBoundingClientRect").mockRestore()
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
