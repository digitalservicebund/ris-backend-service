import { createTestingPinia } from "@pinia/testing"
import userEvent from "@testing-library/user-event"
import { render, screen, within } from "@testing-library/vue"
import { config } from "@vue/test-utils"
import { http, HttpResponse } from "msw"
import { setupServer } from "msw/node"
import InputText from "primevue/inputtext"
import { afterEach, beforeEach, expect, vi } from "vitest"
import { Component, Directive } from "vue"
import { createRouter, createWebHistory } from "vue-router"
import { Page } from "@/components/Pagination.vue"
import PendingProceedingSearch from "@/components/search/PendingProceedingSearch.vue"
import { Court } from "@/domain/court"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import PendingProceeding from "@/domain/pendingProceeding"
import ProcessStep from "@/domain/processStep"
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
  http.get("/api/v1/caselaw/processsteps", () => {
    return HttpResponse.json([
      { uuid: "a", abbreviation: "A", name: "Step A" },
      { uuid: "b", abbreviation: "B", name: "Step B" },
    ] as ProcessStep[])
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
    ...render(PendingProceedingSearch, {
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
                user: { internal: isInternal },
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

describe("Pending Proceeding Search", () => {
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

    expect(screen.getByTestId("pending-proceeding-search")).toBeInTheDocument()
    expect(screen.getByTestId("search-result-list")).toBeInTheDocument()
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
    await user.click(screen.getByText("Ergebnisse zeigen"))

    // Assert
    expect(screen.getByTestId("service-error")).toBeVisible()
    expect(
      screen.getByText("Die Suchergebnisse konnten nicht geladen werden."),
    ).toBeVisible()
    expect(
      screen.getByText("Bitte versuchen Sie es später erneut."),
    ).toBeVisible()
  })

  it("opens new document when 'Neues Anhängiges Verfahren erstellen' is clicked", async () => {
    // Arrange
    mockNoSearchResult()
    const { user, router } = renderComponent()
    await router.replace({ path: "/" })
    expect(
      screen.getByText(
        "Starten Sie die Suche oder erstellen Sie ein neues Anhängiges Verfahren.",
      ),
    ).toBeVisible()
    expect(
      screen.getByText("Neues Anhängiges Verfahren erstellen"),
    ).toBeVisible()
    await user.type(screen.getByLabelText("Dokumentnummer Suche"), "TEST")
    await user.click(screen.getByText("Ergebnisse zeigen"))

    // Act
    await user.click(screen.getByText("Neues Anhängiges Verfahren erstellen"))

    // Assert
    expect(router.currentRoute.value.fullPath).toBe(
      "/caselaw/pendingProceeding/new",
    )
  })

  it("opens new document when 'Übernehmen und fortfahren' is clicked", async () => {
    // Arrange
    mockNoSearchResult()
    vi.spyOn(documentUnitService, "createNew").mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: new PendingProceeding("foo", {
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
      screen.getByLabelText("Mitteilungsdatum Suche"),
      "05.05.2005",
    )

    await user.click(screen.getByText("Ergebnisse zeigen"))

    expect(
      screen.getByText(
        /sie können die folgenden formaldaten übernehmen und ein neuesanhängiges verfahren erstellen:/i,
      ),
    ).toBeVisible()

    // Act
    await user.click(screen.getByText("Übernehmen und fortfahren"))

    // Assert
    expect(router.currentRoute.value.fullPath).toBe(
      "/caselaw/pendingProceeding/1234567891234/categories",
    )
  })

  it("hides create new button for external users", async () => {
    renderComponent({ isInternal: false })

    expect(
      screen.queryByLabelText("Neues Anhängiges Verfahren erstellen"),
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
    await user.click(screen.getByText("Ergebnisse zeigen"))

    // Assert
    expect(
      screen.getByText(errorMessages.SEARCH_RESULTS_NOT_FOUND.title),
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
    await user.click(screen.getByText("Ergebnisse zeigen"))

    // Assert
    expect(screen.getAllByRole("row").length).toBe(2) // including header
    expect(screen.getAllByText("documentNumber").length).toBe(1)
  })

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
    const { user, router } = renderComponent()
    await router.replace({ path: "/" })
    await user.type(
      screen.getByLabelText("Aktenzeichen Suche"),
      "TEST Aktenzeichen",
    )

    // Act
    await user.click(screen.getByText("Ergebnisse zeigen"))

    // Assert
    const resultList = screen.getByTestId("search-result-list")
    expect(
      screen.getByText(
        /Sie können die folgenden Formaldaten übernehmen und ein neues/i,
      ),
    ).toBeVisible()
    expect(within(resultList).getByText("TEST Aktenzeichen,")).toBeVisible()
    expect(within(resultList).getByText("Gericht unbekannt,")).toBeVisible()
    expect(within(resultList).getByText("Datum unbekannt")).toBeVisible()
    expect(
      within(resultList).getByText("Übernehmen und fortfahren"),
    ).toBeVisible()
  })

  it("displays 'Übernehmen und fortfahren' option when only court type is given", async () => {
    // Arrange
    mockNoSearchResult()
    const { user, router } = renderComponent()
    await router.replace({ path: "/" })
    await user.type(screen.getByLabelText("Gerichtstyp Suche"), "BGH")

    // Act
    await user.click(screen.getByText("Ergebnisse zeigen"))

    // Assert
    const resultList = screen.getByTestId("search-result-list")
    expect(
      screen.getByText(
        /Sie können die folgenden Formaldaten übernehmen und ein neues/i,
      ),
    ).toBeVisible()
    expect(
      within(resultList).getByText("Aktenzeichen unbekannt,"),
    ).toBeVisible()
    expect(within(resultList).getByText("BGH,")).toBeVisible()
    expect(within(resultList).getByText("Datum unbekannt")).toBeVisible()
    expect(
      within(resultList).getByText("Übernehmen und fortfahren"),
    ).toBeVisible()
  })

  it("displays 'Übernehmen und fortfahren' option when only decision date is given", async () => {
    // Arrange
    mockNoSearchResult()
    const { user, router } = renderComponent()
    await router.replace({ path: "/" })
    await user.type(
      screen.getByLabelText("Mitteilungsdatum Suche"),
      "05.05.2005",
    )

    // Act
    await user.click(screen.getByText("Ergebnisse zeigen"))

    // Assert
    const resultList = screen.getByTestId("search-result-list")
    expect(
      within(resultList).getByText(
        /Sie können die folgenden Formaldaten übernehmen und ein neues/i,
      ),
    ).toBeVisible()
    expect(
      within(resultList).getByText("Aktenzeichen unbekannt,"),
    ).toBeVisible()
    expect(within(resultList).getByText("Gericht unbekannt,")).toBeVisible()
    expect(within(resultList).getByText("05.05.2005")).toBeVisible()
    expect(
      within(resultList).getByText("Übernehmen und fortfahren"),
    ).toBeVisible()
    expect(screen.getByText("Übernehmen und fortfahren")).toBeVisible()
  })
})
