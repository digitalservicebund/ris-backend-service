import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { config } from "@vue/test-utils"
import { http, HttpResponse } from "msw"
import { setupServer } from "msw/node"
import InputText from "primevue/inputtext"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import { createRouter, createWebHistory } from "vue-router"
import SearchForm from "@/components/search/shared/SearchForm.vue"
import { Court } from "@/domain/court"
import { Kind } from "@/domain/documentationUnitKind"
import PendingProceeding from "@/domain/pendingProceeding"
import ProcessStep from "@/domain/processStep"
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
  http.get("/api/v1/caselaw/processsteps", () => {
    return HttpResponse.json([
      { uuid: "a", abbreviation: "A", name: "Step A" },
      { uuid: "b", abbreviation: "B", name: "Step B" },
    ] as ProcessStep[])
  }),
)

function renderComponent(
  kind: Kind,
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
        kind,
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
        name: "Test User",
        initials: "TU",
        documentationOffice: {
          abbreviation: "DS",
        },
      },
    }),
  )

  const kinds = [
    { kind: Kind.PENDING_PROCEEDING, dateLabel: "Mitteilungsdatum" },
    { kind: Kind.DECISION, dateLabel: "Entscheidungsdatum" },
  ]

  kinds.forEach(({ kind }) =>
    test(`renders all commin input fields for ${kind}`, async () => {
      renderComponent(kind)
      expect(screen.getByLabelText("Aktenzeichen Suche")).toBeInTheDocument()
      expect(screen.getByLabelText("Gerichtstyp Suche")).toBeInTheDocument()
      expect(screen.getByLabelText("Gerichtsort Suche")).toBeInTheDocument()
      expect(screen.getByLabelText("Dokumentnummer Suche")).toBeInTheDocument()
      expect(screen.getByLabelText("Status Suche")).toBeInTheDocument()
    }),
  )

  test("renders all specific input fields for decisions", async () => {
    const { user } = renderComponent(Kind.DECISION)
    expect(
      screen.getByLabelText("Nur meine Dokstelle Filter"),
    ).toBeInTheDocument()

    // don't show own doc office only inputs
    expect(
      screen.queryByLabelText("jDV Übergabedatum Suche"),
    ).not.toBeInTheDocument()
    expect(screen.queryByLabelText("Terminiert Filter")).not.toBeInTheDocument()
    expect(
      screen.queryByLabelText("Nur fehlerhafte Dokumentationseinheiten"),
    ).not.toBeInTheDocument()
    expect(
      screen.queryByLabelText("Dokumentationseinheiten mit Dublettenverdacht"),
    ).not.toBeInTheDocument()
    expect(screen.queryByLabelText("Prozessschritt")).not.toBeInTheDocument()
    expect(
      screen.queryByLabelText("Nur mir zugewiesen"),
    ).not.toBeInTheDocument()

    // show own doc office only inputs as soon as checkbox is clicked
    await user.click(screen.getByLabelText("Nur meine Dokstelle Filter"))
    expect(screen.getByLabelText("jDV Übergabedatum Suche")).toBeInTheDocument()
    expect(screen.getByLabelText("Terminiert Filter")).toBeInTheDocument()
    expect(
      screen.getByLabelText("Nur fehlerhafte Dokumentationseinheiten"),
    ).toBeInTheDocument()
    expect(
      screen.getByLabelText("Dokumentationseinheiten mit Dublettenverdacht"),
    ).toBeInTheDocument()
    expect(screen.getByLabelText("Prozessschritt")).toBeInTheDocument()
    expect(screen.getByLabelText("Nur mir zugewiesen")).toBeInTheDocument()
  })

  test("renders all specific input fields for pending proceedings", async () => {
    renderComponent(Kind.PENDING_PROCEEDING)
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
    expect(
      screen.queryByLabelText("Nur meine Dokstelle Filter"),
    ).not.toBeInTheDocument()
  })

  kinds.forEach(({ kind }) =>
    test(`emits 'search' event when valid ${kind} search is submitted`, async () => {
      const { user, emitted } = renderComponent(kind)

      await user.type(screen.getByLabelText("Aktenzeichen Suche"), "12345")
      await user.keyboard("{Control>}{Enter}")

      expect(emitted().search).toBeTruthy()
    }),
  )

  kinds.forEach(({ kind }) =>
    test(`emits 'search' event again when same ${kind} search is repeated`, async () => {
      const { user, emitted } = renderComponent(kind)

      await user.type(screen.getByLabelText("Aktenzeichen Suche"), "12345")
      await user.keyboard("{Control>}{Enter}")
      const initialCount = emitted().search.length

      await user.click(screen.getByText("Ergebnisse zeigen"))
      expect(emitted().search.length).toBe(initialCount + 1)
    }),
  )

  kinds.forEach(({ kind, dateLabel }) =>
    test(`resets common fields in ${kind} form and emits 'resetSearchResults' on reset`, async () => {
      const { user, emitted } = renderComponent(kind)

      await user.type(screen.getByLabelText("Aktenzeichen Suche"), "12345")
      await user.type(screen.getByLabelText("Gerichtstyp Suche"), "BGH")
      await user.type(screen.getByLabelText("Gerichtsort Suche"), "DEU")
      await user.type(
        screen.getByLabelText("Dokumentnummer Suche"),
        "DOKNUMMER",
      )
      await user
        .click(screen.getByLabelText("Status Suche"))
        .then(
          async () => await user.click(screen.getByLabelText("Veröffentlicht")),
        )

      await user.type(screen.getByLabelText(dateLabel + " Suche"), "05.05.2005")
      await user.type(
        screen.getByLabelText(dateLabel + " Suche Ende"),
        "01.01.2010",
      )
      await user.click(screen.getByLabelText("Suche zurücksetzen"))

      expect(emitted().resetSearchResults).toBeTruthy()
      expect(screen.getByLabelText("Aktenzeichen Suche")).toHaveValue("")
      expect(screen.getByLabelText("Gerichtstyp Suche")).toHaveValue("")
      expect(screen.getByLabelText("Gerichtsort Suche")).toHaveValue("")
      expect(screen.getByLabelText("Dokumentnummer Suche")).toHaveValue("")
      expect(screen.getByLabelText("Status Suche").textContent).equals(
        "Nicht ausgewählt",
      )
      expect(screen.getByLabelText(dateLabel + " Suche")).toHaveValue("")
      expect(screen.getByLabelText(dateLabel + " Suche Ende")).toHaveValue("")
    }),
  )

  test(`resets pending proceeding form fields and emits 'resetSearchResults' on reset`, async () => {
    const { user, emitted } = renderComponent(Kind.PENDING_PROCEEDING)

    await user.type(
      screen.getByLabelText("Erledigungsmitteilung Suche"),
      "11.11.2011",
    )
    await user.type(
      screen.getByLabelText("Erledigungsmitteilung Suche Ende"),
      "12.11.2020",
    )
    await user.click(screen.getByLabelText("Erledigt Filter"))

    await user.click(screen.getByLabelText("Suche zurücksetzen"))

    expect(emitted().resetSearchResults).toBeTruthy()

    expect(screen.getByLabelText("Erledigungsmitteilung Suche")).toHaveValue("")
    expect(
      screen.getByLabelText("Erledigungsmitteilung Suche Ende"),
    ).toHaveValue("")
    expect(screen.getByLabelText("Erledigt Filter")).not.toBeChecked()
  })

  test(`resets decision form fields and emits 'resetSearchResults' on reset`, async () => {
    const { user, emitted } = renderComponent(Kind.DECISION)

    await user.click(screen.getByLabelText("Nur meine Dokstelle Filter"))
    await user
      .click(screen.getByLabelText("Prozessschritt"))
      .then(async () => await user.click(screen.getByLabelText("Step A")))
    await user.type(
      screen.getByLabelText("jDV Übergabedatum Suche"),
      "11.11.2011",
    )
    await user.click(screen.getByLabelText("Terminiert Filter"))
    await user.click(
      screen.getByLabelText("Nur fehlerhafte Dokumentationseinheiten"),
    )
    await user.click(
      screen.getByLabelText("Dokumentationseinheiten mit Dublettenverdacht"),
    )
    await user.click(screen.getByLabelText("Nur mir zugewiesen"))
    expect(screen.getByLabelText("Nur mir zugewiesen")).toBeChecked()

    await user.click(screen.getByLabelText("Suche zurücksetzen"))

    expect(emitted().resetSearchResults).toBeTruthy()
    await user.click(screen.getByLabelText("Nur meine Dokstelle Filter"))

    expect(screen.getByLabelText("jDV Übergabedatum Suche")).toHaveValue("")
    expect(screen.getByLabelText("Prozessschritt")).toHaveTextContent(
      "Nicht ausgewählt",
    )
    expect(screen.getByLabelText("Terminiert Filter")).not.toBeChecked()
    expect(
      screen.getByLabelText("Nur fehlerhafte Dokumentationseinheiten"),
    ).not.toBeChecked()
    expect(
      screen.getByLabelText("Dokumentationseinheiten mit Dublettenverdacht"),
    ).not.toBeChecked()
    expect(screen.getByLabelText("Nur mir zugewiesen")).not.toBeChecked()
  })

  test(`resets all docoffice specific filter, when 'Nur meine Dokstelle Filter' is unchecked`, async () => {
    const { user } = renderComponent(Kind.DECISION)

    await user.click(screen.getByLabelText("Nur meine Dokstelle Filter"))
    await user
      .click(screen.getByLabelText("Prozessschritt"))
      .then(async () => await user.click(screen.getByLabelText("Step A")))
    await user.type(
      screen.getByLabelText("jDV Übergabedatum Suche"),
      "11.11.2011",
    )
    await user.click(screen.getByLabelText("Terminiert Filter"))
    await user.click(
      screen.getByLabelText("Nur fehlerhafte Dokumentationseinheiten"),
    )
    await user.click(
      screen.getByLabelText("Dokumentationseinheiten mit Dublettenverdacht"),
    )
    await user.click(screen.getByLabelText("Nur mir zugewiesen"))
    expect(screen.getByLabelText("Nur mir zugewiesen")).toBeChecked()

    await user.click(screen.getByLabelText("Nur meine Dokstelle Filter"))
    expect(
      screen.getByLabelText("Nur meine Dokstelle Filter"),
    ).not.toBeChecked()
    await user.click(screen.getByLabelText("Nur meine Dokstelle Filter"))
    expect(screen.getByLabelText("Nur meine Dokstelle Filter")).toBeChecked()

    expect(screen.getByLabelText("jDV Übergabedatum Suche")).toHaveValue("")
    expect(screen.getByLabelText("Prozessschritt").textContent).equals(
      "Bitte auswählen",
    )
    expect(screen.getByLabelText("Terminiert Filter")).not.toBeChecked()
    expect(
      screen.getByLabelText("Nur fehlerhafte Dokumentationseinheiten"),
    ).not.toBeChecked()
    expect(
      screen.getByLabelText("Dokumentationseinheiten mit Dublettenverdacht"),
    ).not.toBeChecked()
    expect(screen.getByLabelText("Nur mir zugewiesen")).not.toBeChecked()
  })

  test(`resets own doc office fields when check box is unchecked`, async () => {
    const { user } = renderComponent(Kind.DECISION)

    await user.click(screen.getByLabelText("Nur meine Dokstelle Filter"))
    await user.type(
      screen.getByLabelText("jDV Übergabedatum Suche"),
      "11.11.2011",
    )
    await user.click(screen.getByLabelText("Terminiert Filter"))
    await user.click(
      screen.getByLabelText("Nur fehlerhafte Dokumentationseinheiten"),
    )
    await user.click(
      screen.getByLabelText("Dokumentationseinheiten mit Dublettenverdacht"),
    )

    // hide fields
    await user.click(screen.getByLabelText("Nur meine Dokstelle Filter"))
    // show fields
    await user.click(screen.getByLabelText("Nur meine Dokstelle Filter"))

    expect(screen.getByLabelText("jDV Übergabedatum Suche")).toHaveValue("")
    expect(screen.getByLabelText("Terminiert Filter")).not.toBeChecked()
    expect(
      screen.getByLabelText("Nur fehlerhafte Dokumentationseinheiten"),
    ).not.toBeChecked()
    expect(
      screen.getByLabelText("Dokumentationseinheiten mit Dublettenverdacht"),
    ).not.toBeChecked()
  })

  kinds
    .concat({
      kind: Kind.PENDING_PROCEEDING,
      dateLabel: "Erledigungsmitteilung",
    })
    .forEach(({ kind, dateLabel }) =>
      test(`validates ${kind} ${dateLabel} date range and shows error if end date is before start date`, async () => {
        const { user } = renderComponent(kind)

        const startDateInput = screen.getByLabelText(dateLabel + " Suche")
        const endDateInput = screen.getByLabelText(dateLabel + " Suche Ende")

        await user.type(startDateInput, "01.01.2025")
        await user.type(endDateInput, "01.01.2023")

        await user.keyboard("{Control>}{Enter}")

        expect(
          screen.getByText("Enddatum darf nicht vor Startdatum liegen"),
        ).toBeInTheDocument()
      }),
    )

  kinds.forEach(({ kind }) =>
    it(`displays error message when no ${kind} search item is entered`, async () => {
      // given
      const { user } = renderComponent(kind)
      const startDateInput = screen.getByLabelText("Aktenzeichen Suche")
      await user.click(startDateInput)

      // when
      await user.keyboard("{Control>}{Enter}")

      // then
      expect(
        screen.getByText("Geben Sie mindestens ein Suchkriterium ein"),
      ).toBeInTheDocument()
    }),
  )

  kinds.forEach(({ kind, dateLabel }) =>
    it(`displays error message when invalid date is entered for ${kind}`, async () => {
      // given
      const { user } = renderComponent(kind)
      const startDateInput = screen.getByLabelText(dateLabel + " Suche")
      await user.type(startDateInput, "99.99.9999")

      // when
      await user.keyboard("{Control>}{Enter}")

      // then
      expect(screen.getByText("Kein valides Datum")).toBeInTheDocument()
    }),
  )

  kinds
    .concat({
      kind: Kind.PENDING_PROCEEDING,
      dateLabel: "Erledigungsmitteilung",
    })
    .forEach(({ kind, dateLabel }) =>
      it("displays error message when start date is missing", async () => {
        // given
        const { user } = renderComponent(kind)
        const endDateInput = screen.getByLabelText(dateLabel + " Suche Ende")
        await user.type(endDateInput, "01.01.2000")

        // when
        await user.keyboard("{Control>}{Enter}")

        // then
        expect(screen.getByText("Startdatum fehlt")).toBeInTheDocument()
      }),
    )
})
