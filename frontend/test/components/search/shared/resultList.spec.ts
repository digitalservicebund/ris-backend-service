import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import { Page } from "@/components/Pagination.vue"
import ResultList from "@/components/search/shared/ResultList.vue"
import { Kind } from "@/domain/documentationUnitKind"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import { PublicationState } from "@/domain/publicationStatus"
import routes from "~/test-helper/routes"

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
    currentDocumentationUnitProcessStep: generateProcessStep(),
    previousProcessStep: {
      uuid: "1",
      name: "Ersterfassung",
      abbreviation: "EF",
    },
    resolutionDate: "2000-04-06",
  }),
]

function generateProcessStep() {
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
        plugins: [[router]],
      },
    }),
  }
}

describe("Search Result List", () => {
  it("renders document numbers", () => {
    renderComponent()
    const rows = screen.getAllByRole("row")
    expect(rows[1]).toHaveTextContent("ABC123")
    expect(rows[2]).toHaveTextContent("DEF456")
  })

  it("displays current user of process step", () => {
    renderComponent({ kind: Kind.DECISION })
    const rowWithProcessStep = screen.getAllByRole("row")[2]
    expect(rowWithProcessStep).toHaveTextContent("Ersterfassung")
    expect(rowWithProcessStep).toHaveTextContent("TN")
  })
})
