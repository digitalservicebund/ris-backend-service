import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createPinia, setActivePinia } from "pinia"
import { nextTick } from "vue"
import { createRouter, createWebHistory } from "vue-router"
import DocumentUnitSearchEntryForm from "@/components/DocumentUnitSearchEntryForm.vue"

function renderComponent(options?: { isLoading: boolean }) {
  // eslint-disable-next-line testing-library/await-async-events
  const props = {
    isLoading: options?.isLoading ? options?.isLoading : false,
  }

  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: "/caselaw/documentUnit/new",
        name: "new",
        component: {},
      },
      {
        path: "/",
        name: "home",
        component: {},
      },
      {
        path: "/caselaw/documentUnit/:documentNumber/categories",
        name: "caselaw-documentUnit-documentNumber-categories",
        component: {},
      },
      {
        path: "/caselaw/documentUnit/:documentNumber/files",
        name: "caselaw-documentUnit-documentNumber-files",
        component: {},
      },
    ],
  })

  return {
    ...render(DocumentUnitSearchEntryForm, {
      props,
      global: { plugins: [router] },
    }),
  }
}

describe("Documentunit Search", () => {
  const user = userEvent.setup()

  beforeEach(async () => {
    setActivePinia(createPinia())
  })

  test("renders correctly", async () => {
    renderComponent()
    expect(
      screen.getByLabelText("Dokumentnummer oder Aktenzeichen Suche"),
    ).toBeVisible()
    expect(screen.getByLabelText("Gerichtstyp Suche")).toBeVisible()
    expect(screen.getByLabelText("Gerichtsort Suche")).toBeVisible()
    expect(screen.getByLabelText("Entscheidungsdatum Suche")).toBeVisible()
    expect(screen.getByLabelText("Status Suche")).toBeVisible()
    expect(screen.getByLabelText("Nur meine Dokstelle Filter")).toBeVisible()
  })

  test("click on 'Suche zurücksetzen' emits 'resetSearchResults'", async () => {
    const { emitted } = renderComponent()
    await user.type(
      screen.getByLabelText("Entscheidungsdatum Suche"),
      "22.02.2001",
    )
    await user.click(screen.getByLabelText("Suche zurücksetzen"))
    expect(emitted().resetSearchResults).toBeTruthy()
  })

  test("click on 'Ergebnisse anzeigen' emits search event", async () => {
    const { emitted } = renderComponent()

    const input = screen.getByLabelText("Entscheidungsdatum Suche")

    await user.type(input, "22.02.2001")
    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )

    expect(emitted().search).toBeTruthy()
  })

  test("click on 'Nur meine Dokstelle' renders 'Nur fehlerhafte Dokumentationseinheiten' checkbox", async () => {
    renderComponent()
    expect(
      screen.queryByLabelText("Nur fehlerhafte Dokumentationseinheiten"),
    ).not.toBeInTheDocument()
    expect(screen.getByLabelText("Nur meine Dokstelle Filter")).toBeVisible()
    await user.click(screen.getByLabelText("Nur meine Dokstelle Filter"))
    expect(
      screen.getByLabelText("Nur fehlerhafte Dokumentationseinheiten"),
    ).toBeVisible()
  })

  test("click on 'Nur meine Dokstelle' renders 'Nur fehlerhafte Dokumentationseinheiten' checkbox", async () => {
    renderComponent()
    expect(
      screen.queryByLabelText("Nur fehlerhafte Dokumentationseinheiten"),
    ).not.toBeInTheDocument()
    expect(screen.getByLabelText("Nur meine Dokstelle Filter")).toBeVisible()
    await user.click(screen.getByLabelText("Nur meine Dokstelle Filter"))
    expect(
      screen.getByLabelText("Nur fehlerhafte Dokumentationseinheiten"),
    ).toBeVisible()
  })

  test("shows error if 2nd date input given but 1st empty", async () => {
    renderComponent()

    const input = screen.getByLabelText("Entscheidungsdatum Suche Ende")

    await user.type(input, "22.02.2001")

    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )

    await nextTick()
    expect(screen.getByText("Startdatum fehlt")).toBeVisible()
  })

  test("removes startdate missing error if 2nd date is removed", async () => {
    renderComponent()

    const input = screen.getByLabelText("Entscheidungsdatum Suche Ende")

    await user.type(input, "22.02.2001")

    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )

    await nextTick()
    expect(screen.getByText("Startdatum fehlt")).toBeVisible()

    await user.clear(screen.getByLabelText("Entscheidungsdatum Suche Ende"))
    await nextTick()
    expect(screen.queryByText("Startdatum fehlt")).not.toBeInTheDocument()
  })

  test("shows error when 2nd date before 1st date", async () => {
    renderComponent()

    const date = screen.getByLabelText("Entscheidungsdatum Suche")
    const dateEnd = screen.getByLabelText("Entscheidungsdatum Suche Ende")

    await user.type(date, "22.02.2001")
    await user.type(dateEnd, "22.02.2000")

    expect(
      screen.getByText("Enddatum darf nich vor Startdatum liegen"),
    ).toBeVisible()
  })

  test("shows error when clicking on search with errors in search params", async () => {
    renderComponent()

    const date = screen.getByLabelText("Entscheidungsdatum Suche")
    const dateEnd = screen.getByLabelText("Entscheidungsdatum Suche Ende")

    await user.type(date, "22.02.2001")
    await user.type(dateEnd, "22.02.2000")

    expect(
      screen.getByText("Enddatum darf nich vor Startdatum liegen"),
    ).toBeVisible()

    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )

    await nextTick()
    expect(screen.getByText("Fehler in Suchkriterien")).toBeVisible()
  })
})
