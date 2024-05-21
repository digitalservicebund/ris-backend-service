import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import DocumentUnitList from "@/components/DocumentUnitList.vue"
import { PublicationState } from "@/domain/documentUnit"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import authService from "@/services/authService"
import { ResponseError } from "@/services/httpClient"

function renderComponent(options?: {
  documentUnitListEntries?: DocumentUnitListEntry[]
  searchResponseError?: ResponseError
  isLoading?: boolean
  isDeletable?: boolean
  emptyState?: string
}) {
  // eslint-disable-next-line testing-library/await-async-events
  const user = userEvent.setup()
  return {
    user,
    ...render(DocumentUnitList, {
      props: {
        documentUnitListEntries: options?.documentUnitListEntries,
        searchResponseError: options?.searchResponseError ?? undefined,
        isLoading: options?.isLoading ?? false,
        isDeletable: options?.isDeletable ?? true,
        emptyState:
          options?.emptyState ??
          (!options?.documentUnitListEntries
            ? "Starten Sie die Suche oder erstellen Sie eine neue Dokumentationseinheit."
            : "Keine Ergebnisse gefunden."),
      },
      global: {
        plugins: [
          createRouter({
            history: createWebHistory(),
            routes: [
              {
                path: "/caselaw/documentUnit/:documentNumber/files",
                name: "caselaw-documentUnit-documentNumber-files",
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
                path: "/",
                name: "caselaw",
                component: {},
              },
            ],
          }),
        ],
      },
    }),
  }
}

describe("documentUnit list", () => {
  const fetchSpy = vi.spyOn(authService, "getName").mockImplementation(() =>
    Promise.resolve({
      status: 200,
      data: [
        {
          name: "username",
          documentationOffice: {
            abbreviation: "DS",
          },
        },
      ],
    }),
  )

  test("initial state feedback", async () => {
    renderComponent({})

    expect(
      screen.getByText(/Starten Sie die Suche oder erstellen Sie eine/),
    ).toBeVisible()
  })

  test("no results feedback", async () => {
    renderComponent({
      documentUnitListEntries: [],
    })

    expect(screen.getByText(/Keine Ergebnisse./)).toBeVisible()
  })

  test("shows error", () => {
    renderComponent({
      searchResponseError: {
        title: "error title",
        description: "error description",
      },
    })
    expect(screen.getByText(/error title/)).toBeVisible()
    expect(screen.getByText(/error description/)).toBeVisible()
  })

  test("shows loading state", () => {
    renderComponent({ isLoading: true })
    expect(screen.getByLabelText("Ladestatus")).toBeInTheDocument()
  })

  test("renders documentUnit list", async () => {
    renderComponent({
      documentUnitListEntries: [
        {
          id: "id",
          uuid: "1",
          documentNumber: "123",
          decisionDate: "2022-02-10",
          fileNumber: "",
          appraisalBody: "",
          documentType: { label: "Test", jurisShortcut: "T" },
          court: { type: "typeA", location: "locB", label: "typeA locB" },
          status: {
            publicationStatus: PublicationState.PUBLISHED,
            withError: false,
          },
          documentationOffice: { abbreviation: "OTHER" },
        },
        {
          id: "id",
          uuid: "2",
          documentNumber: "234",
          decisionDate: "2022-02-10",
          fileNumber: "",
          appraisalBody: "cba",
          documentType: { label: "Test", jurisShortcut: "T" },
          court: { type: "typeA", location: "locB", label: "typeA locB" },
          status: {
            publicationStatus: PublicationState.PUBLISHED,
            withError: false,
          },
          documentationOffice: { abbreviation: "DS" },
        },
      ],
    })

    expect(fetchSpy).toBeCalledTimes(1)

    expect(screen.getAllByTestId("listEntry").length).toBe(2)

    //Spruchkörper visible
    expect(screen.getByText("cba")).toBeVisible()

    // expect only one edit link to 234 documentation unit
    expect(
      screen.getByRole("link", { name: "Dokumentationseinheit bearbeiten" }),
    ).toHaveAttribute("href", "/caselaw/documentUnit/234/categories")

    // expect only one delete button for 234 documentation unit
    expect(
      screen.getByRole("button", { name: "Dokumentationseinheit löschen" }),
    ).toBeVisible()

    // expect two view links
    expect(
      screen.getAllByRole("link", { name: "Dokumentationseinheit ansehen" }),
    ).toHaveLength(2)
  })

  test("delete emits event", async () => {
    const { user, emitted } = renderComponent({
      documentUnitListEntries: [
        {
          id: "id",
          uuid: "1",
          documentNumber: "123",
          decisionDate: "2022-02-10",
          fileNumber: "",
          documentType: { label: "Test", jurisShortcut: "T" },
          court: { type: "typeA", location: "locB", label: "typeA locB" },
          status: {
            publicationStatus: PublicationState.PUBLISHED,
            withError: false,
          },
          documentationOffice: { abbreviation: "DS" },
        },
      ],
    })

    // expect(authServiceMock).toHaveBeenCalledTimes(1)

    await screen.findByText("123")
    await user.click(screen.getByLabelText("Dokumentationseinheit löschen"))
    const confirmButton = screen.getByRole("button", { name: "Löschen" })
    expect(confirmButton).toBeInTheDocument()
    await user.click(confirmButton)
    expect(emitted().deleteDocumentUnit).toBeTruthy()
  })
})
