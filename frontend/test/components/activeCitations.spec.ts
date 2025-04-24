import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { fireEvent, render, screen, waitFor } from "@testing-library/vue"
import { config } from "@vue/test-utils"
import { http, HttpResponse } from "msw"
import { setupServer } from "msw/node"
import InputText from "primevue/inputtext"
import { createRouter, createWebHistory } from "vue-router"
import ActiveCitations from "@/components/ActiveCitations.vue"
import ActiveCitation from "@/domain/activeCitation"
import { CitationType } from "@/domain/citationType"
import { DocumentType } from "@/domain/documentType"
import DocumentUnit, { Court } from "@/domain/documentUnit"

import documentUnitService from "@/services/documentUnitService"
import featureToggleService from "@/services/featureToggleService"
import { onSearchShortcutDirective } from "@/utils/onSearchShortcutDirective"
import routes from "~/test-helper/routes"

const server = setupServer(
  http.get("/api/v1/caselaw/courts", () => {
    const court: Court = {
      type: "AG",
      location: "Test",
      label: "AG Test",
    }
    return HttpResponse.json([court])
  }),
  http.get("/api/v1/caselaw/documenttypes", () => {
    const documentType: DocumentType = {
      jurisShortcut: "Ant",
      label: "EuGH-Vorlage",
    }
    return HttpResponse.json([documentType])
  }),
  http.get("/api/v1/caselaw/citationtypes", () => {
    const citationStyle: CitationType = {
      uuid: "123",
      jurisShortcut: "Änderungen",
      label: "Änderungen",
    }
    return HttpResponse.json([citationStyle])
  }),
)

function renderComponent(activeCitations?: ActiveCitation[]) {
  const user = userEvent.setup()

  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  return {
    user,
    ...render(ActiveCitations, {
      global: {
        directives: { "ctrl-enter": onSearchShortcutDirective },
        plugins: [
          [
            createTestingPinia({
              initialState: {
                docunitStore: {
                  documentUnit: new DocumentUnit("foo", {
                    documentNumber: "1234567891234",
                    contentRelatedIndexing: {
                      activeCitations: activeCitations ?? [],
                    },
                  }),
                },
              },
              stubActions: false,
            }),
          ],
          [router],
        ],
        stubs: {
          routerLink: {
            template: "<a><slot/></a>",
          },
        },
      },
    }),
  }
}

function generateActiveCitation(options?: {
  uuid?: string
  documentNumber?: string
  court?: Court
  decisionDate?: string
  fileNumber?: string
  documentType?: DocumentType
  citationStyle?: CitationType
}) {
  const activeCitation = new ActiveCitation({
    uuid: options?.uuid ?? crypto.randomUUID(),
    documentNumber: options?.documentNumber ?? undefined,
    court: options?.court ?? {
      type: "type1",
      location: "location1",
      label: "label1",
    },
    decisionDate: options?.decisionDate ?? "2022-02-01",
    fileNumber: options?.fileNumber ?? "test fileNumber",
    documentType: options?.documentType ?? {
      jurisShortcut: "documentTypeShortcut1",
      label: "documentType1",
    },
    citationType: options?.citationStyle ?? {
      uuid: "123",
      jurisShortcut: "Änderungen",
      label: "Änderungen",
    },
  })
  return activeCitation
}

describe("active citations", () => {
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
    window.HTMLElement.prototype.scrollIntoView = vi.fn()
    vi.spyOn(featureToggleService, "isEnabled").mockResolvedValue({
      status: 200,
      data: true,
    })
    vi.spyOn(
      documentUnitService,
      "searchByRelatedDocumentation",
    ).mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: {
          content: [
            new ActiveCitation({
              uuid: "123",
              court: {
                type: "type1",
                location: "location1",
                label: "label1",
              },
              decisionDate: "2022-02-01",
              documentType: {
                jurisShortcut: "documentTypeShortcut1",
                label: "documentType1",
              },
              fileNumber: "test fileNumber1",
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

    vi.spyOn(window, "scrollTo").mockImplementation(() => vi.fn())
  })
  afterEach(() => {
    // Mock needs to be reset (and can not be mocked globally) because InputMask has interdependencies
    // with the PrimeVue select component. When testing the select components with InputMask
    // mocked globally, they fail due to these dependencies.
    config.global.stubs = {}
    vi.resetAllMocks()
  })

  it("renders empty active citation in edit mode, when no activeCitations in list", async () => {
    renderComponent()
    expect((await screen.findAllByLabelText("Listen Eintrag")).length).toBe(1)
    expect(screen.getByLabelText("Art der Zitierung")).toBeVisible()
    expect(screen.getByLabelText("Gericht Aktivzitierung")).toBeVisible()
    expect(
      screen.getByLabelText("Entscheidungsdatum Aktivzitierung"),
    ).toBeVisible()
    expect(
      screen.getByLabelText("Aktenzeichen Aktivzitierung"),
    ).toBeInTheDocument()
    expect(
      screen.getByLabelText("Dokumenttyp Aktivzitierung"),
    ).toBeInTheDocument()
    expect(screen.getByLabelText("Aktivzitierung speichern")).toBeDisabled()
  })

  it("renders activeCitations as list entries", () => {
    renderComponent([
      generateActiveCitation({ fileNumber: "123" }),
      generateActiveCitation({ fileNumber: "345" }),
    ])

    expect(screen.queryByLabelText("Art der Zitierung")).not.toBeInTheDocument()
    expect(screen.getByText(/123/)).toBeInTheDocument()
    expect(screen.getByText(/345/)).toBeInTheDocument()
  })

  it("creates new active citation manually", async () => {
    const { user } = renderComponent()
    const input = await screen.findByLabelText("Aktenzeichen Aktivzitierung")
    await user.type(input, "123")
    const button = screen.getByLabelText("Aktivzitierung speichern")
    await user.click(button)

    expect(screen.getAllByLabelText("Listen Eintrag").length).toBe(2)
  })

  it("click on edit icon, opens the list entry in edit mode", async () => {
    const { user } = renderComponent([
      generateActiveCitation({
        fileNumber: "123",
      }),
    ])
    const itemHeader = screen.getByTestId("list-entry-0")
    await user.click(itemHeader)

    expect(screen.getByLabelText("Art der Zitierung")).toBeVisible()
    expect(
      screen.getByLabelText("Entscheidungsdatum Aktivzitierung"),
    ).toBeVisible()
    expect(
      screen.getByLabelText("Entscheidungsdatum Aktivzitierung"),
    ).toBeInTheDocument()
    expect(
      screen.getByLabelText("Aktenzeichen Aktivzitierung"),
    ).toBeInTheDocument()
    expect(
      screen.getByLabelText("Dokumenttyp Aktivzitierung"),
    ).toBeInTheDocument()
  })

  it("renders manually added active citations as editable list item", async () => {
    renderComponent([generateActiveCitation()])
    expect(screen.getByTestId("list-entry-0")).toBeInTheDocument()
  })

  it("correctly updates value citation style input", async () => {
    const { user } = renderComponent([
      generateActiveCitation({
        citationStyle: {
          uuid: "123",
          jurisShortcut: "ABC",
          label: "ABC",
        },
      }),
    ])

    expect(screen.queryByText(/Änderungen/)).not.toBeInTheDocument()

    const itemHeader = screen.getByTestId("list-entry-0")
    await user.click(itemHeader)

    await user.type(
      await screen.findByLabelText("Art der Zitierung"),
      "Änderungen",
    )
    const dropdownItems = screen.getAllByLabelText("dropdown-option")
    expect(dropdownItems[0]).toHaveTextContent("Änderungen")
    await user.click(dropdownItems[0])
    const button = screen.getByLabelText("Aktivzitierung speichern")
    await user.click(button)

    expect(screen.getByText(/Änderungen/)).toBeVisible()
  })

  it("correctly updates value document type input", async () => {
    const { user } = renderComponent([generateActiveCitation()])

    expect(screen.queryByText(/EuGH-Vorlage/)).not.toBeInTheDocument()

    const itemHeader = screen.getByTestId("list-entry-0")
    await user.click(itemHeader)

    await user.type(
      await screen.findByLabelText("Dokumenttyp Aktivzitierung"),
      "Ant",
    )
    const dropdownItems = screen.getAllByLabelText("dropdown-option")
    expect(dropdownItems[0]).toHaveTextContent("EuGH-Vorlage")
    await user.click(dropdownItems[0])
    const button = screen.getByLabelText("Aktivzitierung speichern")
    await user.click(button)

    expect(screen.getByText(/EuGH-Vorlage/)).toBeVisible()
  })

  it("correctly updates value court input", async () => {
    const { user } = renderComponent([generateActiveCitation()])

    expect(screen.queryByText(/AG Test/)).not.toBeInTheDocument()

    const itemHeader = screen.getByTestId("list-entry-0")
    await user.click(itemHeader)

    await user.type(
      await screen.findByLabelText("Gericht Aktivzitierung"),
      "AG",
    )
    const dropdownItems = screen.getAllByLabelText("dropdown-option")
    expect(dropdownItems[0]).toHaveTextContent("AG Test")
    await user.click(dropdownItems[0])
    const button = screen.getByLabelText("Aktivzitierung speichern")
    await user.click(button)

    expect(screen.getByText(/AG Test/)).toBeVisible()
  })

  it("correctly updates value of fileNumber input", async () => {
    const { user } = renderComponent([generateActiveCitation()])

    expect(screen.queryByText(/new fileNumber/)).not.toBeInTheDocument()
    const itemHeader = screen.getByTestId("list-entry-0")
    await user.click(itemHeader)

    const fileNumberInput = await screen.findByLabelText(
      "Aktenzeichen Aktivzitierung",
    )

    await user.clear(fileNumberInput)
    await user.type(fileNumberInput, "new fileNumber")
    const button = screen.getByLabelText("Aktivzitierung speichern")
    await user.click(button)

    expect(screen.getByText(/new fileNumber/)).toBeVisible()
  })

  it("correctly updates value of decision date input", async () => {
    const { user } = renderComponent([generateActiveCitation()])

    expect(screen.queryByText(/02.02.2022/)).not.toBeInTheDocument()
    const itemHeader = screen.getByTestId("list-entry-0")
    await user.click(itemHeader)

    const fileNumberInput = await screen.findByLabelText(
      "Entscheidungsdatum Aktivzitierung",
    )

    await user.clear(fileNumberInput)
    await user.type(fileNumberInput, "02.02.2022")
    const button = screen.getByLabelText("Aktivzitierung speichern")
    await user.click(button)

    expect(screen.getByText(/02.02.2022/)).toBeVisible()
  })

  it("correctly deletes manually added active citations", async () => {
    const { user } = renderComponent([
      generateActiveCitation(),
      generateActiveCitation(),
    ])
    const activeCitations = screen.getAllByLabelText("Listen Eintrag")
    expect(activeCitations.length).toBe(2)
    await user.click(screen.getByTestId("list-entry-0"))
    await user.click(screen.getByLabelText("Eintrag löschen"))
    expect(screen.getAllByLabelText("Listen Eintrag").length).toBe(1)
  })

  it("correctly deletes active citations added by search", async () => {
    const { user } = renderComponent([
      generateActiveCitation(),
      generateActiveCitation(),
    ])
    const activeCitations = screen.getAllByLabelText("Listen Eintrag")
    expect(activeCitations.length).toBe(2)
    await user.click(screen.getByTestId("list-entry-0"))
    await user.click(screen.getByLabelText("Eintrag löschen"))
    expect(screen.getAllByLabelText("Listen Eintrag").length).toBe(1)
  })

  it("correctly updates deleted values in active citations", async () => {
    const { user } = renderComponent([generateActiveCitation()])

    expect(
      screen.getByText(
        "Änderungen, label1, 01.02.2022, test fileNumber, documentType1",
      ),
    ).toBeInTheDocument()
    const itemHeader = screen.getByTestId("list-entry-0")
    await user.click(itemHeader)

    const fileNumberInput = await screen.findByLabelText(
      "Aktenzeichen Aktivzitierung",
    )
    const courtInput = await screen.findByLabelText("Gericht Aktivzitierung")

    await user.clear(fileNumberInput)
    await user.clear(courtInput)

    await user.click(screen.getByLabelText("Aktivzitierung speichern"))

    expect(
      screen.getByText(/Änderungen, 01.02.2022, documentType1/),
    ).toBeInTheDocument()
  })

  it("lists search results", async () => {
    const { user } = renderComponent()

    expect(screen.queryByText(/test fileNumber/)).not.toBeInTheDocument()
    await user.click(await screen.findByLabelText("Nach Entscheidung suchen"))

    expect(screen.getAllByText(/test fileNumber/).length).toBe(1)
  })

  it("search is triggered with shortcut", async () => {
    const { user } = renderComponent()

    expect(screen.queryByText(/test fileNumber/)).not.toBeInTheDocument()
    await user.type(
      await screen.findByLabelText("Aktenzeichen Aktivzitierung"),
      "test",
    )
    await user.keyboard("{Control>}{Enter}")

    expect(screen.getAllByText(/test fileNumber/).length).toBe(1)
  })

  it("adds active citation from search results", async () => {
    const { user } = renderComponent()

    await user.click(await screen.findByLabelText("Nach Entscheidung suchen"))
    await user.click(await screen.findByLabelText("Treffer übernehmen"))
    expect(screen.getAllByText(/test fileNumber/).length).toBe(1)
  })

  it("indicates that search result already added to active citations", async () => {
    const { user } = renderComponent([generateActiveCitation({ uuid: "123" })])
    await user.click(screen.getByText(/Weitere Angabe/))
    await user.click(screen.getByLabelText("Nach Entscheidung suchen"))
    expect(screen.getByText(/Bereits hinzugefügt/)).toBeInTheDocument()
  })

  it("displays error in list and edit component when fields missing", async () => {
    const { user } = renderComponent([generateActiveCitation()])
    const itemHeader = screen.getByTestId("list-entry-0")
    await user.click(itemHeader)

    const fileInput = await screen.findByLabelText(
      "Aktenzeichen Aktivzitierung",
    )
    await user.clear(fileInput)
    await user.click(screen.getByLabelText("Aktivzitierung speichern"))
    expect(screen.getByText(/Fehlende Daten/)).toBeInTheDocument()
    await user.click(itemHeader)
    expect(screen.getAllByText(/Pflichtfeld nicht befüllt/).length).toBe(1)
  })

  it("shows missing citationStyle validation on entry in other field", async () => {
    const { user } = renderComponent()

    const getStyleValidation = () =>
      screen.queryByTestId("activeCitationPredicate-validationError")

    expect(getStyleValidation()).not.toBeInTheDocument()

    await user.type(
      await screen.findByLabelText("Aktenzeichen Aktivzitierung"),
      "test",
    )

    expect(getStyleValidation()).toBeVisible()
  })

  it("shows missing citationStyle validation for linked decision", async () => {
    const { user } = renderComponent([
      generateActiveCitation({
        documentNumber: "123",
        citationStyle: {
          label: "invalid",
        },
      }),
    ])
    const itemHeader = screen.getByTestId("list-entry-0")
    await user.click(itemHeader)

    expect(screen.getByText("Art der Zitierung *")).toBeVisible()
    expect(screen.getAllByText(/Pflichtfeld nicht befüllt/).length).toBe(1)
  })

  it("does not add active citation with invalid date input", async () => {
    const { user } = renderComponent()

    const dateInput = await screen.findByLabelText(
      "Entscheidungsdatum Aktivzitierung",
    )
    expect(dateInput).toHaveValue("")

    await user.type(dateInput, "00.00.0231")

    await screen.findByText(/Kein valides Datum/)
    screen.getByLabelText("Aktivzitierung speichern").click()
    expect(dateInput).toBeVisible()
  })

  it("does not add active citation with incomplete date input", async () => {
    const { user } = renderComponent()

    const dateInput = await screen.findByLabelText(
      "Entscheidungsdatum Aktivzitierung",
    )
    expect(dateInput).toHaveValue("")

    await user.type(dateInput, "01.02.")
    await user.tab()

    await screen.findByText(/Unvollständiges Datum/)
    screen.getByLabelText("Aktivzitierung speichern").click()
    expect(dateInput).toBeVisible()
  })

  it("does not add active citation with date in the future", async () => {
    const { user } = renderComponent()

    const dateInput = await screen.findByLabelText(
      "Entscheidungsdatum Aktivzitierung",
    )
    expect(dateInput).toHaveValue("")

    await user.type(dateInput, "01.02.2090")
    await user.tab()

    await screen.findByText(/Das Datum darf nicht in der Zukunft liegen/)
    screen.getByLabelText("Aktivzitierung speichern").click()
    expect(dateInput).toBeVisible()
  })

  it("should copy text of active citation summary", async () => {
    const { user } = renderComponent([generateActiveCitation()])
    const copyButton = screen.getByTestId("copy-summary")
    await user.click(copyButton)

    // Read from the stub clipboard
    const clipboardText = await navigator.clipboard.readText()

    expect(clipboardText).toBe(
      "Änderungen, label1, 01.02.2022, test fileNumber, documentType1",
    )
  })

  it("should render parallel decision icons for 'Teilweise Parallelentscheidung'", async () => {
    renderComponent([
      generateActiveCitation({
        citationStyle: {
          label: "Teilweise Parallelentscheidung",
        },
      }),
    ])
    await waitFor(() => {
      expect(screen.getByTestId("import-categories")).toBeVisible()
    })
    await waitFor(() => {
      expect(screen.getByTestId("generate-headnote")).toBeVisible()
    })
  })

  it("should render parallel decision icons for 'Parallelentscheidung'", async () => {
    renderComponent([
      generateActiveCitation({
        citationStyle: {
          label: "Parallelentscheidung",
        },
      }),
    ])
    await waitFor(() => {
      expect(screen.getByTestId("import-categories")).toBeVisible()
    })
    await waitFor(() => {
      expect(screen.getByTestId("generate-headnote")).toBeVisible()
    })
  })

  describe("keyboard navigation", () => {
    it("should copy text of active citation summary", async () => {
      const { user } = renderComponent([generateActiveCitation()])
      const copyButton = screen.getByTestId("copy-summary")
      await fireEvent.focus(copyButton)
      await user.type(copyButton, "{enter}")

      // Read from the stub clipboard
      const clipboardText = await navigator.clipboard.readText()

      expect(clipboardText).toBe(
        "Änderungen, label1, 01.02.2022, test fileNumber, documentType1",
      )
    })
  })
})
