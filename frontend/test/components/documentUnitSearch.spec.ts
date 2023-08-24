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
            courtType: "type",
            courtLocation: "location",
            decisionDate: "01.02.2022",
            documentType: {
              label: "documentTypeShortcut",
              jurisShortcut: "documentTypeShortcut",
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
    expect(
      screen.getByLabelText("Neue Dokumentationseinheit erstellen"),
    ).toBeVisible()
    expect(screen.queryAllByTestId("listEntry").length).toBe(0)
  })

  test("click on 'Ergebnisse anzeigen' renders search results", async () => {
    const { user } = renderComponent()

    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )
    expect(screen.getAllByTestId("listEntry").length).toBe(1)
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
})
