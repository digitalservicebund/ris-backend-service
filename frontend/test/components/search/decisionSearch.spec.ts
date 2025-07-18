import { createTestingPinia } from "@pinia/testing"
import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { config } from "@vue/test-utils"
import { http, HttpResponse } from "msw"
import { setupServer } from "msw/node"
import InputText from "primevue/inputtext"
import { afterEach, beforeEach, expect, MockInstance, vi } from "vitest"
import { Component, Directive } from "vue"
import { createRouter, createWebHistory } from "vue-router"
import { Page } from "@/components/Pagination.vue"
import DecisionSearch from "@/components/search/DecisionSearch.vue"
import { Court } from "@/domain/court"
import { Decision } from "@/domain/decision"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import errorMessages from "@/i18n/errors.json"
import documentUnitService from "@/services/documentUnitService"
import { ServiceResponse } from "@/services/httpClient"
import { onSearchShortcutDirective } from "@/utils/onSearchShortcutDirective"
import routes from "~/test-helper/routes"

const server = setupServer(
  http.get("/api/v1/caselaw/courts", () => {
    const court: Court = {
      type: "BGH",
      location: "",
      label: "BGH",
    }
    return HttpResponse.json([court])
  }),
)

function renderComponent(
  { isInternal }: { isInternal: boolean } = { isInternal: true },
  stubs?: Record<string, boolean | Component | Directive> | string[],
) {
  const user = userEvent.setup()

  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  return {
    user,
    ...render(DecisionSearch, {
      global: {
        directives: {
          "ctrl-enter": onSearchShortcutDirective,
          tooltip: {},
        },
        plugins: [
          router,
          createTestingPinia({
            initialState: {
              session: {
                user: { roles: [isInternal ? "Internal" : "External"] },
              },
            },
          }),
        ],
        stubs: stubs ?? undefined,
      },
    }),
    router: router,
  }
}

describe("Decision Search", () => {
  let deleteSpy: MockInstance<
    (uuid: string) => Promise<ServiceResponse<unknown>>
  >
  beforeAll(() => server.listen())
  afterAll(() => server.close())
  beforeEach(() => {
    // InputMask evaluates cursor position on every keystroke, however, our browser vitest setup does not
    // implement any layout-related functionality, meaning the required functions for cursor offset
    // calculation are missing. When we deal with typing in date/ year / time inputs, we can mock it with
    // TextInput, as we only need the string and do not need to test the actual mask behaviour.
    config.global.stubs = {
      InputMask: InputText,
    }
    deleteSpy = vi.spyOn(documentUnitService, "delete")
    vi.resetAllMocks()
  })

  afterEach(() => {
    // Mock needs to be reset (and can not be mocked globally) because InputMask has interdependencies
    // with the PrimeVue select component. When testing the select components with InputMask
    // mocked globally, they fail due to these dependencies.
    config.global.stubs = {}
    vi.clearAllMocks()
  })

  it("renders search form and result list", () => {
    renderComponent()

    expect(screen.getByTestId("decision-search")).toBeInTheDocument()
    expect(screen.getByTestId("search-result-list")).toBeInTheDocument()
  })

  it("triggers another search when last item was deleted", async () => {
    // Arrange
    vi.clearAllMocks()
    const searchSpy = mockOneSearchResult()
    deleteSpy.mockResolvedValue({
      status: 200,
      data: {},
    })
    const { user } = renderComponent(
      { isInternal: true },
      {
        ResultList: {
          template: `<button aria-label="Dokumentationseinheit löschen" @click="$emit('delete-documentation-unit', { uuid: '123' })">Delete</button>
              `,
        },
      },
    )
    await user.type(screen.getByLabelText("Aktenzeichen Suche"), "TEST")
    await user.click(screen.getByText("Ergebnisse anzeigen"))

    // Act
    await user.click(screen.getByLabelText("Dokumentationseinheit löschen"))

    // Assert
    expect(searchSpy).toBeCalledTimes(2)
  })

  it("calls service on search and shows error modal on error", async () => {
    // Arrange
    vi.spyOn(
      documentUnitService,
      "searchByDocumentUnitSearchInput",
    ).mockResolvedValue({
      status: 500,
      data: {
        timestamp: "2025-07-11T09:01:25.758+00:00",
        status: 500,
        error: "Internal Server Error",
        path: "/api/v1/caselaw/documentunits/search",
      },
      error: {
        title: "Die Suchergebnisse konnten nicht geladen werden.",
        description: "Bitte versuchen Sie es später erneut.",
      },
    } as unknown as ServiceResponse<Page<DocumentUnitListEntry>>)
    const { user } = renderComponent()

    // Act
    await user.type(screen.getByLabelText("Aktenzeichen Suche"), "TEST")
    await user.click(screen.getByText("Ergebnisse anzeigen"))

    // Assert
    expect(screen.getByTestId("service-error")).toBeVisible()
    expect(
      screen.getByText("Die Suchergebnisse konnten nicht geladen werden."),
    ).toBeVisible()
    expect(
      screen.getByText("Bitte versuchen Sie es später erneut."),
    ).toBeVisible()
  })

  it("opens new document when 'Neue Dokumentationseinheit erstellen' is clicked", async () => {
    // Arrange
    mockNoSearchResult()
    const { user, router } = renderComponent()
    await router.replace({ path: "/" })
    expect(
      screen.getByText(
        "Starten Sie die Suche oder erstellen Sie eine neue Dokumentationseinheit.",
      ),
    ).toBeVisible()
    expect(
      screen.getByText("Neue Dokumentationseinheit erstellen"),
    ).toBeVisible()
    await user.type(screen.getByLabelText("Dokumentnummer Suche"), "TEST")
    await user.click(screen.getByText("Ergebnisse anzeigen"))

    // Act
    await user.click(screen.getByText("Neue Dokumentationseinheit erstellen"))

    // Assert
    expect(router.currentRoute.value.fullPath).toBe("/caselaw/documentUnit/new")
  })

  it("opens new document when 'Übernehmen und fortfahren' is clicked", async () => {
    // Arrange
    mockNoSearchResult()
    vi.spyOn(documentUnitService, "createNew").mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: new Decision("foo", {
          documentNumber: "1234567891234",
        }),
      }),
    )

    const { user, router } = renderComponent()
    await user.type(
      screen.getByLabelText("Aktenzeichen Suche"),
      "TEST Aktenzeigen",
    )
    await user.type(screen.getByLabelText("Gerichtstyp Suche"), "TEST Gericht")
    await user.type(
      screen.getByLabelText("Entscheidungsdatum Suche"),
      "05.05.2005",
    )

    await user.click(screen.getByText("Ergebnisse anzeigen"))

    expect(
      screen.getByText(
        /Sie können die folgenden Stammdaten übernehmen und eine neue/i,
      ),
    ).toBeVisible()

    // Act
    await user.click(screen.getByText("Übernehmen und fortfahren"))

    // Assert
    expect(router.currentRoute.value.fullPath).toBe(
      "/caselaw/documentUnit/1234567891234/categories",
    )
  })

  it("hides create new button for external users", async () => {
    renderComponent({ isInternal: false })

    expect(
      screen.queryByLabelText("Neue Dokumentationseinheit erstellen"),
    ).not.toBeInTheDocument()
    expect(screen.getByText("Starten Sie die Suche.")).toBeVisible()
  })

  it("displays empty state message when there are no search results", async () => {
    // Arrange
    mockNoSearchResult()
    const { user } = renderComponent()
    expect(
      screen.queryByText(errorMessages.SEARCH_RESULTS_NOT_FOUND.title),
    ).not.toBeInTheDocument()
    await user.type(screen.getByLabelText("Aktenzeichen Suche"), "TEST")

    // Act
    await user.click(screen.getByText("Ergebnisse anzeigen"))

    // Assert
    expect(
      screen.getByText(errorMessages.SEARCH_RESULTS_NOT_FOUND.title),
    ).toBeVisible()
  })

  it("should call the service on delete with valid input and not show an error on success", async () => {
    // Arrange
    mockOneSearchResult()
    deleteSpy.mockResolvedValue({
      status: 200,
      data: {},
    })
    const { user } = renderComponent(
      { isInternal: true },
      {
        ResultList: {
          template: `<button aria-label="Dokumentationseinheit löschen" @click="$emit('delete-documentation-unit', { uuid: '123' })">Delete</button>
              `,
        },
      },
    )
    await user.type(screen.getByLabelText("Aktenzeichen Suche"), "TEST")
    await user.click(screen.getByText("Ergebnisse anzeigen"))
    const deleteButton = screen.getByLabelText("Dokumentationseinheit löschen")

    // Act
    await user.click(deleteButton)

    // Assert
    expect(deleteSpy).toHaveBeenCalledExactlyOnceWith("123")
  })

  it("displays error on deletion", async () => {
    // Arrange
    mockOneSearchResult()
    deleteSpy.mockResolvedValue({
      status: 400,
      error: {
        title: errorMessages.DOCUMENT_UNIT_DELETE_FAILED.title,
      },
    })
    const { user } = renderComponent(
      { isInternal: true },
      {
        ResultList: {
          template: `<button aria-label="Dokumentationseinheit löschen" @click="$emit('delete-documentation-unit', { uuid: '123' })">Delete</button>
              `,
        },
      },
    )
    await user.type(screen.getByLabelText("Aktenzeichen Suche"), "TEST")
    await user.click(screen.getByText("Ergebnisse anzeigen"))
    const deleteButton = screen.getByLabelText("Dokumentationseinheit löschen")

    // Act
    await user.click(deleteButton)

    // Assert
    expect(
      screen.getByText(errorMessages.DOCUMENT_UNIT_DELETE_FAILED.title),
    ).toBeVisible()
  })

  it("displays court search result", async () => {
    // Arrange
    vi.spyOn(
      documentUnitService,
      "searchByDocumentUnitSearchInput",
    ).mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: {
          content: [
            new DocumentUnitListEntry({
              uuid: "123",
              court: {
                type: "type",
                location: "location",
                label: "type location",
              },
              decisionDate: "01.02.2022",
              documentType: {
                jurisShortcut: "documentTypeShortcut",
                label: "docTypeLabel",
              },
              documentNumber: "documentNumber",
              fileNumber: "fileNumber",
            }),
          ],
          size: 0,
          number: 0,
          numberOfElements: 20,
          first: true,
          last: false,
          empty: false,
        },
      }),
    )

    const { user } = renderComponent()
    await user.type(screen.getByLabelText("Gerichtstyp Suche"), "AG")
    expect(screen.getByLabelText("Gerichtstyp Suche")).toHaveValue("AG")

    // Act
    await user.click(screen.getByText("Ergebnisse anzeigen"))

    // Assert
    expect(screen.getAllByRole("row").length).toBe(2) // including header
    expect(screen.getAllByText("documentNumber").length).toBe(1)
  })

  function mockOneSearchResult() {
    return vi
      .spyOn(documentUnitService, "searchByDocumentUnitSearchInput")
      .mockImplementation(() =>
        Promise.resolve({
          status: 200,
          data: {
            content: [
              new DocumentUnitListEntry({
                uuid: "123",
                decisionDate: "01.02.2022",
                documentNumber: "documentNumber",
                scheduledPublicationDateTime: "2000-11-23T10:04:22.603",
                resolutionDate: "01.06.2024",
              }),
            ],
            size: 10,
            number: 0,
            numberOfElements: 1,
            first: true,
            last: false,
            empty: false,
          },
        }),
      )
  }

  function mockNoSearchResult() {
    vi.spyOn(
      documentUnitService,
      "searchByDocumentUnitSearchInput",
    ).mockResolvedValue({
      status: 200,
      data: {
        content: [],
        size: 0,
        number: 0,
        numberOfElements: 0,
        first: false,
        last: false,
        empty: false,
      },
    })
  }

  it("displays 'Übernehmen und fortfahren' option when only filenumber is given", async () => {
    // Arrange
    mockNoSearchResult()
    const { user } = renderComponent()
    await user.type(
      screen.getByLabelText("Aktenzeichen Suche"),
      "TEST Aktenzeigen",
    )

    // Act
    await user.click(screen.getByText("Ergebnisse anzeigen"))

    // Assert
    expect(
      screen.getByText(
        /Sie können die folgenden Stammdaten übernehmen und eine neue/i,
      ),
    ).toBeVisible()
    expect(screen.getByText("Übernehmen und fortfahren")).toBeVisible()
  })

  it("displays 'Übernehmen und fortfahren' option when only court type is given", async () => {
    // Arrange
    mockNoSearchResult()
    const { user } = renderComponent()
    await user.type(
      screen.getByLabelText("Gerichtstyp Suche"),
      "TEST Gerichtstyp",
    )

    // Act
    await user.click(screen.getByText("Ergebnisse anzeigen"))

    // Assert
    expect(
      screen.getByText(
        /Sie können die folgenden Stammdaten übernehmen und eine neue/i,
      ),
    ).toBeVisible()
    expect(screen.getByText("Übernehmen und fortfahren")).toBeVisible()
  })

  it("displays 'Übernehmen und fortfahren' option when only decision date is given", async () => {
    // Arrange
    mockNoSearchResult()
    const { user } = renderComponent()
    await user.type(
      screen.getByLabelText("Entscheidungsdatum Suche"),
      "05.05.2005",
    )

    // Act
    await user.click(screen.getByText("Ergebnisse anzeigen"))

    // Assert
    expect(
      screen.getByText(
        /Sie können die folgenden Stammdaten übernehmen und eine neue/i,
      ),
    ).toBeVisible()
    expect(screen.getByText("Übernehmen und fortfahren")).toBeVisible()
  })
})
