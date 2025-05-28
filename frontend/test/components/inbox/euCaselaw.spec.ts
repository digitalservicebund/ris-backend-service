import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { MockInstance } from "vitest"
import { createRouter, createWebHistory } from "vue-router"
import EUCaselaw from "@/components/inbox/EUCaselaw.vue"
import { Page } from "@/components/Pagination.vue"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import { PublicationState } from "@/domain/publicationStatus"
import documentUnitService from "@/services/documentUnitService"
import { ServiceResponse } from "@/services/httpClient"
import { onSearchShortcutDirective } from "@/utils/onSearchShortcutDirective"
import routes from "~/test-helper/routes"

const addToastMock = vi.fn()
vi.mock("primevue/usetoast", () => ({
  useToast: () => ({ add: addToastMock }),
}))

function renderComponent() {
  const user = userEvent.setup()

  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  return {
    user,
    ...render(EUCaselaw, {
      global: {
        directives: {
          "ctrl-enter": onSearchShortcutDirective,
        },
        plugins: [router],
        stubs: {
          InboxList: {
            template: `<button aria-label="Dokumentationseinheit löschen" @click="$emit('delete-documentation-unit', { uuid: '123' })">Delete</button>
              `,
          },
        },
      },
    }),
  }
}

describe("EU Rechtsprechung", () => {
  let searchSpy: MockInstance<
    (
      requestParams?:
        | {
            [key: string]: string
          }
        | undefined,
    ) => Promise<ServiceResponse<Page<DocumentUnitListEntry>>>
  >

  let deleteSpy: MockInstance<
    (uuid: string) => Promise<ServiceResponse<unknown>>
  >

  const alertSpy = vi.spyOn(window, "alert").mockImplementation(() => {})

  beforeEach(() => {
    deleteSpy = vi.spyOn(documentUnitService, "delete")
    searchSpy = vi
      .spyOn(documentUnitService, "searchByDocumentUnitSearchInput")
      .mockResolvedValue({
        status: 200,
        data: {
          content: [
            {
              uuid: "123",
              documentNumber: "documentNumber",
              isEditable: true,
              isDeletable: true,
              status: {
                publicationStatus: PublicationState.EXTERNAL_HANDOVER_PENDING,
              },
            },
          ],
          size: 1,
          number: 0,
          numberOfElements: 1,
          first: true,
          last: true,
          empty: false,
        },
      })
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  it("should call search endpoint on mounted", async () => {
    renderComponent()
    expect(searchSpy).toHaveBeenCalledExactlyOnceWith({
      inboxStatus: "EU",
      myDocOfficeOnly: "true",
      pg: "0",
      sz: "100",
    })
  })

  it("should call search endpoint on mounte and show an error on failure", async () => {
    searchSpy.mockResolvedValue({
      status: 400,
      error: {
        title: "Die Suchergebnisse konnten nicht geladen werden.",
      },
    })
    renderComponent()

    expect(searchSpy).toHaveBeenCalledOnce()
    const errorModal = await screen.findByTestId("service-error")
    expect(errorModal).toHaveTextContent(
      "Die Suchergebnisse konnten nicht geladen werden.",
    )
  })

  it("should call the service on delete with valid input and not show an error on success", async () => {
    deleteSpy.mockResolvedValue({
      status: 200,
      data: {},
    })
    const { user } = renderComponent()
    const deleteButton = screen.getByLabelText("Dokumentationseinheit löschen")

    await user.click(deleteButton)

    expect(deleteSpy).toHaveBeenCalledExactlyOnceWith("123")
    expect(alertSpy).not.toHaveBeenCalled()
  })

  it("should call the service on delete with valid input and show an error on failure", async () => {
    deleteSpy.mockResolvedValue({
      status: 400,
      error: { title: "Fehler" },
    })

    const { user } = renderComponent()

    // Find the "Delete" button from the stub
    const deleteButton = screen.getByLabelText("Dokumentationseinheit löschen")

    // Simulate user clicking it
    await user.click(deleteButton)

    // Now assert that your service.delete was called with the expected uuid
    expect(deleteSpy).toHaveBeenCalledOnce()
    expect(deleteSpy).toHaveBeenCalledWith("123")

    expect(alertSpy).toHaveBeenCalledWith(
      expect.stringContaining("Fehler beim Löschen der Dokumentationseinheit"),
    )
  })
})
