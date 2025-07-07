import { createTestingPinia } from "@pinia/testing"
import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { config } from "@vue/test-utils"
import InputText from "primevue/inputtext"
import { afterEach, beforeEach, expect } from "vitest"
import { createRouter, createWebHistory } from "vue-router"
import PendingProceedingSearch from "@/components/search/PendingProceedingSearch.vue"
import PendingProceeding from "@/domain/pendingProceeding"
import errorMessages from "@/i18n/errors.json"
import documentUnitService from "@/services/documentUnitService"
import { onSearchShortcutDirective } from "@/utils/onSearchShortcutDirective"
import routes from "~/test-helper/routes"

const addToastMock = vi.fn()
vi.mock("primevue/usetoast", () => ({
  useToast: () => ({ add: addToastMock }),
}))

function renderComponent(
  { isInternal }: { isInternal: boolean } = { isInternal: true },
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
                user: { roles: [isInternal ? "Internal" : "External"] },
              },
            },
          }),
        ],
      },
    }),
  }
}

describe("Pending Proceeding Search", () => {
  beforeEach(() => {
    // InputMask evaluates cursor position on every keystroke, however, our browser vitest setup does not
    // implement any layout-related functionality, meaning the required functions for cursor offset
    // calculation are missing. When we deal with typing in date/ year / time inputs, we can mock it with
    // TextInput, as we only need the string and do not need to test the actual mask behaviour.
    config.global.stubs = {
      InputMask: InputText,
    }
  })

  afterEach(() => {
    // Mock needs to be reset (and can not be mocked globally) because InputMask has interdependencies
    // with the PrimeVue select component. When testing the select components with InputMask
    // mocked globally, they fail due to these dependencies.
    config.global.stubs = {}
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
      status: 400,
      error: {
        title: "Fehler",
        description: "Etwas ist schiefgelaufen",
      },
    })
    const { user } = renderComponent()

    // Act
    await user.type(screen.getByLabelText("Aktenzeichen Suche"), "TEST")
    await user.click(screen.getByText("Ergebnisse anzeigen"))

    // Assert
    expect(await screen.findByTestId("service-error")).toBeInTheDocument()
    expect(screen.getByText("Etwas ist schiefgelaufen")).toBeInTheDocument()
  })

  it("opens new tab when 'Neues Anhängiges Verfahren erstellen' is clicked", async () => {
    // Arrange
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
    const openSpy = vi.spyOn(window, "open").mockImplementation(() => null)

    const { user } = renderComponent()
    expect(
      screen.getByText(
        "Starten Sie die Suche oder erstellen Sie ein neues Anhängiges Verfahren.",
      ),
    ).toBeVisible()
    expect(
      screen.getByText("Neues Anhängiges Verfahren erstellen"),
    ).toBeVisible()
    await user.type(screen.getByLabelText("Aktenzeichen Suche"), "TEST")
    await user.click(screen.getByText("Ergebnisse anzeigen"))

    // Act
    await user.click(screen.getByText("Neues Anhängiges Verfahren erstellen"))

    // Assert
    expect(openSpy).toHaveBeenCalledWith(
      "/caselaw/pendingProceeding/new",
      "_blank",
    )
  })

  it("opens new tab when 'Übernehmen und fortfahren' is clicked", async () => {
    // Arrange
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
    vi.spyOn(documentUnitService, "createNew").mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: new PendingProceeding("foo", {
          documentNumber: "1234567891234",
        }),
      }),
    )
    const openSpy = vi.spyOn(window, "open").mockImplementation(() => null)

    const { user } = renderComponent()
    await user.type(
      screen.getByLabelText("Aktenzeichen Suche"),
      "TEST Aktenzeigen",
    )
    await user.type(screen.getByLabelText("Gerichtstyp Suche"), "TEST Gericht")
    await user.type(
      screen.getByLabelText("Mitteilungsdatum Suche"),
      "05.05.2005",
    )

    await user.click(screen.getByText("Ergebnisse anzeigen"))

    expect(
      screen.getByText(
        /sie können die folgenden stammdaten übernehmen und ein neuesanhängiges verfahren erstellen:/i,
      ),
    ).toBeVisible()

    // Act
    await user.click(screen.getByText("Übernehmen und fortfahren"))

    // Assert
    expect(openSpy).toHaveBeenCalledWith(
      "/caselaw/pendingProceeding/1234567891234/categories",
      "_blank",
    )
  })

  test("external user cannot create new doc unit", async () => {
    renderComponent({ isInternal: false })

    expect(
      screen.queryByLabelText("Neues Anhängiges Verfahren erstellen"),
    ).not.toBeInTheDocument()
    expect(screen.getByText("Starten Sie die Suche.")).toBeVisible()
  })

  test("display empty state message when there are no search results", async () => {
    // Arrange
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
})
