import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createPinia, setActivePinia } from "pinia"
import { createRouter, createWebHistory } from "vue-router"
import DocumentUnitSearch from "@/components/DocumentUnitSearch.vue"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import documentUnitService from "@/services/documentUnitService"

function renderComponent() {
  // eslint-disable-next-line testing-library/await-async-events
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

  test("click on 'Ergebnisse anzeigen' with search input renders results", async () => {
    const { user } = renderComponent()

    await user.type(screen.getByLabelText("Gerichtstyp Suche"), "AG")
    expect(screen.getByLabelText("Gerichtstyp Suche")).toHaveValue("AG")

    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )

    expect(screen.getAllByTestId("listEntry").length).toBe(1)
    expect(screen.getAllByText(/documentNumber/).length).toBe(1)
  })
})
