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
    resolutionDate: "2000-04-06",
  }),
]
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

  it("opens delete modal on delete button click and emits delete event", async () => {
    const { user, emitted } = renderComponent()
    const deleteButtons = screen.getAllByRole("button", {
      name: "Dokumentationseinheit löschen",
    })

    await user.click(deleteButtons[0])
    expect(
      screen.getByText(
        "Möchten Sie die Dokumentationseinheit ABC123 wirklich dauerhaft löschen?",
      ),
    ).toBeVisible()

    await user.click(screen.getByLabelText("Löschen"))
    expect(emitted()["deleteDocumentationUnit"]).toEqual([
      [
        {
          appraisalBody: undefined,
          court: {
            label: "BGH",
            location: "",
            type: "BGH",
          },
          createdAt: undefined,
          creatingDocumentationOffice: undefined,
          decisionDate: undefined,
          documentNumber: "ABC123",
          documentType: undefined,
          fileNumber: undefined,
          hasAttachments: true,
          hasHeadnoteOrPrinciple: false,
          id: undefined,
          isDeletable: true,
          isEditable: true,
          lastPublicationDateTime: undefined,
          note: undefined,
          resolutionDate: "2025-05-06",
          scheduledPublicationDateTime: undefined,
          source: undefined,
          status: {
            publicationStatus: "PUBLISHED",
            withError: false,
          },
          uuid: "1",
        },
      ],
    ])
  })
})
