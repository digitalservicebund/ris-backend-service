import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { http, HttpResponse } from "msw"
import { setupServer } from "msw/node"
import { createRouter, createWebHistory } from "vue-router"
import DocumentUnitSearch from "@/components/DocumentUnitSearch.vue"
import { Court } from "@/domain/documentUnit"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import authService from "@/services/authService"
import documentUnitService from "@/services/documentUnitService"
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
) {
  const user = userEvent.setup()

  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  return {
    user,
    ...render(DocumentUnitSearch, {
      global: {
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

describe("Documentunit Search", () => {
  beforeAll(() => server.listen())
  afterAll(() => server.close())

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

  test("Internal user can create new doc unit", async () => {
    renderComponent()

    expect(
      screen.getByLabelText("Neue Dokumentationseinheit erstellen"),
    ).toBeVisible()
    expect(
      screen.getByText(
        "Starten Sie die Suche oder erstellen Sie eine neue Dokumentationseinheit.",
      ),
    ).toBeVisible()
    expect(screen.queryAllByRole("row").length).toBe(0)
  })

  test("External user cannot create new doc unit", async () => {
    renderComponent({ isInternal: false })

    expect(
      screen.queryByLabelText("Neue Dokumentationseinheit erstellen"),
    ).not.toBeInTheDocument()
    expect(screen.getByText("Starten Sie die Suche.")).toBeVisible()
    expect(screen.queryAllByRole("row").length).toBe(0)
  })

  test("search for scheduled publicationDate should display one result", async () => {
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
              decisionDate: "01.02.2022",
              documentNumber: "documentNumber",
              scheduledPublicationDateTime: "2000-11-23T10:04:22.603",
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

    await user.click(screen.getByLabelText("Nur meine Dokstelle"))
    await user.type(
      screen.getByLabelText("jDV Übergabedatum Suche"),
      "23.11.2000",
    )
    await user.click(screen.getByLabelText("Terminiert Filter"))
    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )
    expect(screen.getByRole("cell", { name: "jDV Übergabe" })).toBeVisible()
    expect(screen.queryAllByRole("row").length).toBe(1)
  })

  test("search with scheduled only should display one result", async () => {
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
              decisionDate: "01.02.2022",
              documentNumber: "documentNumber",
              scheduledPublicationDateTime: "2000-11-23T10:04:22.603",
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

    await user.click(screen.getByLabelText("Nur meine Dokstelle"))
    await user.click(screen.getByLabelText("Terminiert Filter"))
    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )
    expect(screen.getByRole("cell", { name: "jDV Übergabe" })).toBeVisible()
    expect(screen.queryAllByRole("row").length).toBe(1)
  })

  test("search for publicationDate should display one result", async () => {
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
              decisionDate: "01.02.2022",
              documentNumber: "documentNumber",
              lastPublicationDateTime: "2000-11-23T10:04:22.603",
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

    await user.click(screen.getByLabelText("Nur meine Dokstelle"))
    await user.type(
      screen.getByLabelText("jDV Übergabedatum Suche"),
      "23.11.2000",
    )
    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )
    expect(screen.getByRole("cell", { name: "jDV Übergabe" })).toBeVisible()
    expect(screen.queryAllByRole("row").length).toBe(1)
  })

  test("click on 'Ergebnisse anzeigen' with search input renders results", async () => {
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

    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )

    expect(screen.getAllByRole("row").length).toBe(1)
    expect(screen.getAllByText(/documentNumber/).length).toBe(1)
  })

  test("click on 'Ergebnisse anzeigen' without results let's you create a new doc unit", async () => {
    vi.spyOn(
      documentUnitService,
      "searchByDocumentUnitSearchInput",
    ).mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: {
          content: [],
          size: 0,
          number: 0,
          numberOfElements: 20,
          first: false,
          last: false,
          empty: true,
        },
      }),
    )

    const { user } = renderComponent()

    await user.type(screen.getByLabelText("Aktenzeichen Suche"), "ABCD")
    expect(screen.getByLabelText("Aktenzeichen Suche")).toHaveValue("ABCD")

    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )

    await new Promise((resolve) => setTimeout(resolve, 100))

    expect(screen.queryAllByRole("row").length).toBe(0)

    expect(screen.getByText("Übernehmen und fortfahren")).toBeVisible()
  })

  test("click on 'Ergebnisse anzeigen' without results does not let external users create a new doc unit", async () => {
    vi.spyOn(
      documentUnitService,
      "searchByDocumentUnitSearchInput",
    ).mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: {
          content: [],
          size: 0,
          number: 0,
          numberOfElements: 20,
          first: false,
          last: false,
          empty: true,
        },
      }),
    )

    const { user } = renderComponent({ isInternal: false })

    await user.type(screen.getByLabelText("Aktenzeichen Suche"), "ABCD")
    expect(screen.getByLabelText("Aktenzeichen Suche")).toHaveValue("ABCD")

    await user.click(
      screen.getByLabelText("Nach Dokumentationseinheiten suchen"),
    )

    await new Promise((resolve) => setTimeout(resolve, 100))

    expect(screen.queryAllByRole("row").length).toBe(0)

    expect(
      screen.queryByText("Übernehmen und fortfahren"),
    ).not.toBeInTheDocument()
  })
})
