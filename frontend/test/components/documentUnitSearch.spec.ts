import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import DocumentUnitSearch from "@/components/DocumentUnitSearch.vue"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import authService from "@/services/authService"
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
        path: "/caselaw/documentUnit/:documentNumber/preview",
        name: "caselaw-documentUnit-documentNumber-preview",
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
      global: { plugins: [router, createTestingPinia()] },
    }),
  }
}

describe("Documentunit Search", () => {
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
            documentationOffice: { abbreviation: "DS" },
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

  test("renders correctly", async () => {
    renderComponent()

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
