import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import InboxList from "@/components/inbox/shared/InboxList.vue"
import { Page } from "@/components/Pagination.vue"
import { InboxStatus } from "@/domain/decision"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import { PublicationState } from "@/domain/publicationStatus"
import routes from "~/test-helper/routes"

// The bulk-assign-procedure component uses a toast
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
    note: "Some note",
    scheduledPublicationDateTime: "2025-05-06",
    isDeletable: true,
    isEditable: true,
    status: {
      publicationStatus: PublicationState.EXTERNAL_HANDOVER_PENDING,
      withError: false,
    },
    court: { label: "BGH", type: "BGH", location: "" },
  }),
  new DocumentUnitListEntry({
    uuid: "2",
    documentNumber: "DEF456",
    hasAttachments: false,
    hasHeadnoteOrPrinciple: true,
    note: "",
    isDeletable: false,
    isEditable: false,
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

function renderComponent(props = {}) {
  const user = userEvent.setup()

  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  return {
    user,
    ...render(InboxList, {
      props: {
        pageEntries,
        inboxStatus: InboxStatus.EU,
        ...props,
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

describe("PendingHandoverList", () => {
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
          lastHandoverDateTime: undefined,
          note: "Some note",
          scheduledPublicationDateTime: "2025-05-06",
          source: undefined,
          status: {
            publicationStatus: "EXTERNAL_HANDOVER_PENDING",
            withError: false,
          },
          uuid: "1",
        },
      ],
    ])
  })

  it("emits takeOverDocumentationUnit when takeover button clicked", async () => {
    const { user, emitted } = renderComponent()

    await user.click(screen.getByLabelText("Dokumentationseinheit übernehmen"))
    expect(emitted()["takeOverDocumentationUnit"]).toEqual([
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
          lastHandoverDateTime: undefined,
          note: "Some note",
          scheduledPublicationDateTime: "2025-05-06",
          source: undefined,
          status: {
            publicationStatus: "EXTERNAL_HANDOVER_PENDING",
            withError: false,
          },
          uuid: "1",
        },
      ],
    ])
  })
})
