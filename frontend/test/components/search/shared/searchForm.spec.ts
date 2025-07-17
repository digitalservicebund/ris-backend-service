import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { config } from "@vue/test-utils"
import { http, HttpResponse } from "msw"
import { setupServer } from "msw/node"
import InputText from "primevue/inputtext"
import { afterEach, beforeEach, expect, it, vi } from "vitest"
import { createRouter, createWebHistory } from "vue-router"
import SearchForm from "@/components/search/shared/SearchForm.vue"
import { Court } from "@/domain/court"
import { Kind } from "@/domain/documentationUnitKind"
import PendingProceeding from "@/domain/pendingProceeding"
import authService from "@/services/authService"
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
  http.get("/api/v1/caselaw/documentunits/search", () => {
    return HttpResponse.json([new PendingProceeding("uuid")])
  }),
)

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
    ...render(SearchForm, {
      props: {
        kind: Kind.PENDING_PROCEEDING,
      },
      global: {
        directives: { "ctrl-enter": onSearchShortcutDirective },
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
    router: router,
  }
}

describe("Documentunit Search", () => {
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
  })

  afterEach(() => {
    // Mock needs to be reset (and can not be mocked globally) because InputMask has interdependencies
    // with the PrimeVue select component. When testing the select components with InputMask
    // mocked globally, they fail due to these dependencies.
    config.global.stubs = {}
  })

  vi.spyOn(authService, "getName").mockImplementation(() =>
    Promise.resolve({
      status: 200,
      data: {
        name: "username",
        documentationOffice: {
          abbreviation: "DS",
        },
      },
    }),
  )

  test("renders all input fields", async () => {
    renderComponent()

    expect(screen.getByLabelText("Aktenzeichen Suche")).toBeInTheDocument()
    expect(screen.getByLabelText("Gerichtstyp Suche")).toBeInTheDocument()
    expect(screen.getByLabelText("Gerichtsort Suche")).toBeInTheDocument()
    expect(screen.getByLabelText("Dokumentnummer Suche")).toBeInTheDocument()
    expect(screen.getByLabelText("Status Suche")).toBeInTheDocument()
    expect(
      screen.getByLabelText("Erledigungsmitteilung Suche"),
    ).toBeInTheDocument()
    expect(
      screen.getByLabelText("Erledigungsmitteilung Suche Ende"),
    ).toBeInTheDocument()
    expect(screen.getByLabelText("Mitteilungsdatum Suche")).toBeInTheDocument()
    expect(
      screen.getByLabelText("Mitteilungsdatum Suche Ende"),
    ).toBeInTheDocument()
    expect(screen.getByLabelText("Erledigt Filter")).toBeInTheDocument()
  })

  test("emits 'search' event when valid search is submitted", async () => {
    const { user, emitted } = renderComponent()

    await user.type(screen.getByLabelText("Aktenzeichen Suche"), "12345")
    await user.keyboard("{Control>}{Enter}")

    expect(emitted().search).toBeTruthy()
  })

  test("emits 'search' event again when same search is repeated", async () => {
    const { user, emitted } = renderComponent()

    await user.type(screen.getByLabelText("Aktenzeichen Suche"), "12345")
    await user.keyboard("{Control>}{Enter}")
    const initialCount = emitted().search.length

    await user.click(screen.getByText("Ergebnisse anzeigen"))
    expect(emitted().search.length).toBe(initialCount + 1)
  })

  test("resets form and emits 'resetSearchResults' on reset", async () => {
    const { user, emitted } = renderComponent()

    await user.type(screen.getByLabelText("Aktenzeichen Suche"), "12345")
    await user.type(screen.getByLabelText("Gerichtstyp Suche"), "BGH")
    await user.type(screen.getByLabelText("Gerichtsort Suche"), "DEU")
    await user.type(screen.getByLabelText("Dokumentnummer Suche"), "DOKNUMMER")
    await user
      .click(screen.getByLabelText("Status Suche"))
      .then(
        async () => await user.click(screen.getByLabelText("Veröffentlicht")),
      )
    await user.type(
      screen.getByLabelText("Erledigungsmitteilung Suche"),
      "11.11.2011",
    )
    await user.type(
      screen.getByLabelText("Erledigungsmitteilung Suche Ende"),
      "12.11.2020",
    )
    await user.type(
      screen.getByLabelText("Mitteilungsdatum Suche"),
      "05.05.2005",
    )
    await user.type(
      screen.getByLabelText("Mitteilungsdatum Suche Ende"),
      "01.01.2010",
    )
    await user.click(screen.getByLabelText("Erledigt Filter"))

    await user.click(screen.getByLabelText("Suche zurücksetzen"))

    expect(emitted().resetSearchResults).toBeTruthy()
    expect(screen.getByLabelText("Aktenzeichen Suche")).toHaveValue("")
    expect(screen.getByLabelText("Gerichtstyp Suche")).toHaveValue("")
    expect(screen.getByLabelText("Gerichtsort Suche")).toHaveValue("")
    expect(screen.getByLabelText("Dokumentnummer Suche")).toHaveValue("")
    expect(screen.getByLabelText("Status Suche").textContent).equals(
      "Bitte auswählen",
    )
    expect(screen.getByLabelText("Mitteilungsdatum Suche")).toHaveValue("")
    expect(screen.getByLabelText("Mitteilungsdatum Suche Ende")).toHaveValue("")
    expect(screen.getByLabelText("Erledigungsmitteilung Suche")).toHaveValue("")
    expect(
      screen.getByLabelText("Erledigungsmitteilung Suche Ende"),
    ).toHaveValue("")
    expect(screen.getByLabelText("Erledigt Filter")).not.toBeChecked()
  })

  test("validates date range and shows error if end date is before start date", async () => {
    const { user } = renderComponent()

    const startDateInput = screen.getByLabelText("Mitteilungsdatum Suche")
    const endDateInput = screen.getByLabelText("Mitteilungsdatum Suche Ende")

    await user.type(startDateInput, "01.01.2025")
    await user.type(endDateInput, "01.01.2023")

    await user.keyboard("{Control>}{Enter}")

    expect(
      screen.getByText("Enddatum darf nicht vor Startdatum liegen"),
    ).toBeInTheDocument()
  })

  test("validates resolution date range and shows error if end date is before start date", async () => {
    const { user } = renderComponent()

    const startDateInput = screen.getByLabelText("Erledigungsmitteilung Suche")
    const endDateInput = screen.getByLabelText(
      "Erledigungsmitteilung Suche Ende",
    )

    await user.type(startDateInput, "01.01.2025")
    await user.type(endDateInput, "01.01.2023")

    await user.keyboard("{Control>}{Enter}")

    expect(
      screen.getByText("Enddatum darf nicht vor Startdatum liegen"),
    ).toBeInTheDocument()
  })

  it("displays error message when no search item is entered", async () => {
    // given
    const { user } = renderComponent()
    const startDateInput = screen.getByLabelText("Mitteilungsdatum Suche")
    await user.click(startDateInput)

    // when
    await user.keyboard("{Control>}{Enter}")

    // then
    expect(
      screen.getByText("Geben Sie mindestens ein Suchkriterium ein"),
    ).toBeInTheDocument()
  })

  it("displays error message when invalid date is entered", async () => {
    // given
    const { user } = renderComponent()
    const startDateInput = screen.getByLabelText("Mitteilungsdatum Suche")
    await user.type(startDateInput, "99.99.9999")

    // when
    await user.keyboard("{Control>}{Enter}")

    // then
    expect(screen.getByText("Kein valides Datum")).toBeInTheDocument()
  })

  it("displays error message when invalid resolution date is entered", async () => {
    // given
    const { user } = renderComponent()
    const startDateInput = screen.getByLabelText("Mitteilungsdatum Suche")
    await user.type(startDateInput, "99.99.9999")

    // when
    await user.keyboard("{Control>}{Enter}")

    // then
    expect(screen.getByText("Kein valides Datum")).toBeInTheDocument()
  })

  it("displays error message when start date is missing", async () => {
    // given
    const { user } = renderComponent()
    const endDateInput = screen.getByLabelText("Mitteilungsdatum Suche Ende")
    await user.type(endDateInput, "01.01.2000")

    // when
    await user.keyboard("{Control>}{Enter}")

    // then
    expect(screen.getByText("Startdatum fehlt")).toBeInTheDocument()
  })

  it("displays error message when start resolution date is missing", async () => {
    // given
    const { user } = renderComponent()
    const endDateInput = screen.getByLabelText(
      "Erledigungsmitteilung Suche Ende",
    )
    await user.type(endDateInput, "01.01.2000")

    // when
    await user.keyboard("{Control>}{Enter}")

    // then
    expect(screen.getByText("Startdatum fehlt")).toBeInTheDocument()
  })
})
