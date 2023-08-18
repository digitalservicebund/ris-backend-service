import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createPinia, setActivePinia } from "pinia"
import { createRouter, createWebHistory } from "vue-router"
import DocumentUnitSearch from "@/components/DocumentUnitSearch.vue"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import documentUnitService from "@/services/documentUnitService"

function renderComponent() {
  const user = userEvent.setup()

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
    user,
    ...render(DocumentUnitSearch, {
      global: { plugins: [router] },
    }),
  }
}

describe("Documentunit Search", () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
  })

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
              label: "label",
            },
            decisionDate: "01.02.2022",
            documentType: {
              jurisShortcut: "documentTypeShortcut",
              label: "documentType",
            },
            documentNumber: "documentNumber",
            fileNumber: "fileNumber",
          }),
        ],
        size: 0,
        totalElements: 20,
        totalPages: 2,
        number: 0,
        numberOfElements: 20,
        first: true,
        last: false,
      },
    }),
  )

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

  test("click on 'Nur meine Dokstelle' renders 'Nur fehlerhafte Dokumentationseinheiten' checkbox", async () => {
    const { user } = renderComponent()
    expect(
      screen.queryByLabelText("Nur fehlerhafte Dokumentationseinheiten"),
    ).not.toBeInTheDocument()
    expect(screen.getByLabelText("Nur meine Dokstelle Filter")).toBeVisible()
    await user.click(screen.getByLabelText("Nur meine Dokstelle Filter"))
    expect(
      screen.getByLabelText("Nur fehlerhafte Dokumentationseinheiten"),
    ).toBeVisible()
  })

  test("click on 'Ergebnisse anzeigen' renders search results", async () => {
    const { user } = renderComponent()

    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )
    expect(screen.getAllByText(/documentNumber/).length).toBe(1)
  })

  test("click on 'Suche zurücksetzen' resets all input values and search results", async () => {
    const { user } = renderComponent()

    await user.click(screen.getByLabelText("Nur meine Dokstelle Filter"))
    await user.type(screen.getByLabelText("Gerichtstyp Suche"), "AG")
    await user.type(screen.getByLabelText("Gerichtsort Suche"), "Aachen")
    await user.type(
      screen.getByLabelText("Entscheidungsdatum Suche"),
      "01.02.2022",
    )
    await user.selectOptions(screen.getByLabelText("Status Suche"), "PUBLISHED")

    expect(screen.getByLabelText("Gerichtstyp Suche")).toHaveValue("AG")
    expect(screen.getByLabelText("Gerichtsort Suche")).toHaveValue("Aachen")
    expect(screen.getByLabelText("Entscheidungsdatum Suche")).toHaveValue(
      "01.02.2022",
    )
    expect(screen.getByLabelText("Status Suche")).toHaveValue("PUBLISHED")
    expect(screen.getByLabelText("Nur meine Dokstelle Filter")).toBeChecked()
    expect(
      screen.getByLabelText("Nur fehlerhafte Dokumentationseinheiten"),
    ).toBeVisible()

    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )
    expect(screen.getAllByText(/documentNumber/).length).toBe(1)

    await user.click(screen.getByLabelText("Suche zurücksetzen"))
    expect(screen.queryByText(/documentNumber/)).not.toBeInTheDocument()

    expect(screen.getByLabelText("Gerichtstyp Suche")).toHaveValue("")
    expect(screen.getByLabelText("Gerichtsort Suche")).toHaveValue("")
    expect(screen.getByLabelText("Entscheidungsdatum Suche")).toHaveValue("")
    expect(screen.getByLabelText("Status Suche")).toHaveValue("")
    expect(
      screen.getByLabelText("Nur meine Dokstelle Filter"),
    ).not.toBeChecked()
    expect(
      screen.queryByLabelText("Nur fehlerhafte Dokumentationseinheiten"),
    ).not.toBeInTheDocument()
  })

  test("empty search result string is different on initial load and after search", async () => {
    vi.spyOn(
      documentUnitService,
      "searchByDocumentUnitSearchInput",
    ).mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: {
          content: [],
          size: 0,
          totalElements: 0,
          totalPages: 0,
          number: 0,
          numberOfElements: 0,
          first: true,
          last: false,
        },
      }),
    )

    const { user } = renderComponent()

    expect(
      screen.getByText(/Starten Sie die Suche oder erstellen Sie eine/),
    ).toBeVisible()

    await user.click(screen.getByLabelText("Nur meine Dokstelle Filter"))
    await user.type(screen.getByLabelText("Gerichtstyp Suche"), "ABCDEFG")

    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )

    expect(screen.getByText(/Keine Ergebnisse gefunden./)).toBeVisible()
  })

  test("an error is displayed if something went wrong", async () => {
    vi.spyOn(
      documentUnitService,
      "searchByDocumentUnitSearchInput",
    ).mockResolvedValue({
      status: 400,
      error: { title: "error title", description: "error description" },
    })

    const { user } = renderComponent()

    expect(
      screen.getByText(/Starten Sie die Suche oder erstellen Sie eine/),
    ).toBeVisible()

    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )

    expect(screen.getByText(/error title/)).toBeVisible()
    expect(screen.getByText(/error description/)).toBeVisible()
  })
})
