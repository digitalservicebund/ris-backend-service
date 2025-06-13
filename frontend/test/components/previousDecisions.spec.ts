import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { config } from "@vue/test-utils"
import { http, HttpResponse } from "msw"
import { setupServer } from "msw/node"
import InputText from "primevue/inputtext"
import { createRouter, createWebHistory } from "vue-router"
import PreviousDecisions from "@/components/PreviousDecisions.vue"
import { DocumentType } from "@/domain/documentType"
import DocumentUnit, { Court } from "@/domain/documentUnit"

import PreviousDecision from "@/domain/previousDecision"
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
)

function renderComponent(previousDecisions?: PreviousDecision[]) {
  const user = userEvent.setup()

  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  return {
    user,
    ...render(PreviousDecisions, {
      global: {
        directives: { "ctrl-enter": onSearchShortcutDirective },
        stubs: {
          routerLink: {
            template: "<a><slot/></a>",
          },
        },
        plugins: [
          [
            createTestingPinia({
              initialState: {
                docunitStore: {
                  documentUnit: new DocumentUnit("foo", {
                    documentNumber: "1234567891234",
                    coreData: {},
                    shortTexts: {},
                    longTexts: {},
                    previousDecisions: previousDecisions ?? [],
                  }),
                },
              },
            }),
          ],
          [router],
        ],
      },
    }),
  }
}

function generatePreviousDecision(options?: {
  uuid?: string
  documentNumber?: string
  court?: Court
  decisionDate?: string
  fileNumber?: string
  documentType?: DocumentType
  dateKnown?: boolean
}) {
  const previousDecision = new PreviousDecision({
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
    dateKnown: options?.dateKnown ?? true,
  })
  return previousDecision
}

describe("PreviousDecisions", () => {
  beforeAll(() => {
    // InputMask evaluates cursor position on every keystroke, however, our browser vitest setup does not
    // implement any layout-related functionality, meaning the required functions for cursor offset
    // calculation are missing. When we deal with typing in date/ year / time inputs, we can mock it with
    // TextInput, as we only need the string and do not need to test the actual mask behaviour.
    config.global.stubs = {
      InputMask: InputText,
    }
    server.listen()
  })
  afterAll(() => {
    // Mock needs to be reset (and can not be mocked globally) because InputMask has interdependencies
    // with the PrimeVue select component. When testing the select components with InputMask
    // mocked globally, they fail due to these dependencies.
    config.global.stubs = {}
    server.close()
  })

  beforeEach(() => {
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
            new PreviousDecision({
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

  it("renders empty previous decision in edit mode, when no previousDecisions in list", async () => {
    renderComponent()
    expect(
      await screen.findByLabelText("Gericht Vorgehende Entscheidung"),
    ).toBeVisible()
    expect(
      await screen.findByLabelText(
        "Entscheidungsdatum Vorgehende Entscheidung",
      ),
    ).toBeVisible()
    expect(
      await screen.findByLabelText("Datum Unbekannt Vorgehende Entscheidung"),
    ).toBeInTheDocument()
    expect(
      await screen.findByLabelText("Aktenzeichen Vorgehende Entscheidung"),
    ).toBeInTheDocument()
    expect(
      await screen.findByLabelText("Dokumenttyp Vorgehende Entscheidung"),
    ).toBeInTheDocument()
    expect(
      await screen.findByLabelText("Vorgehende Entscheidung speichern"),
    ).toBeDisabled()
  })

  it("renders previousDecisions as list entries", () => {
    const previousDecisions: PreviousDecision[] = [
      generatePreviousDecision({ fileNumber: "123" }),
      generatePreviousDecision({ fileNumber: "345" }),
    ]
    renderComponent(previousDecisions)

    expect(
      screen.queryByLabelText("Vorgehende Entscheidung speichern"),
    ).not.toBeInTheDocument()
    expect(screen.getByText(/123/)).toBeInTheDocument()
    expect(screen.getByText(/345/)).toBeInTheDocument()
  })

  it("creates new previous desision manually", async () => {
    const { user } = renderComponent()
    const input = await screen.findByLabelText(
      "Aktenzeichen Vorgehende Entscheidung",
    )
    await user.type(input, "123")
    const button = screen.getByLabelText("Vorgehende Entscheidung speichern")
    await user.click(button)

    expect(screen.getAllByLabelText("Listen Eintrag").length).toBe(2)
  })

  it("click on list item, opens the list entry in edit mode", async () => {
    const { user } = renderComponent([
      generatePreviousDecision({
        fileNumber: "123",
      }),
    ])
    await user.click(screen.getByTestId("list-entry-0"))

    expect(
      screen.getByLabelText("Gericht Vorgehende Entscheidung"),
    ).toBeVisible()
    expect(
      screen.getByLabelText("Entscheidungsdatum Vorgehende Entscheidung"),
    ).toBeVisible()
    expect(
      screen.getByLabelText("Datum Unbekannt Vorgehende Entscheidung"),
    ).toBeInTheDocument()
    expect(
      screen.getByLabelText("Aktenzeichen Vorgehende Entscheidung"),
    ).toBeInTheDocument()
    expect(
      screen.getByLabelText("Dokumenttyp Vorgehende Entscheidung"),
    ).toBeInTheDocument()
  })

  it("renders manually added decision as editable list item", async () => {
    renderComponent([generatePreviousDecision()])
    expect(screen.getByLabelText("Listen Eintrag")).toBeInTheDocument()
  })

  it("correctly updates value court input", async () => {
    const { user } = renderComponent([generatePreviousDecision()])

    expect(screen.queryByText(/AG Test/)).not.toBeInTheDocument()

    await user.click(screen.getByTestId("list-entry-0"))

    await user.type(
      await screen.findByLabelText("Gericht Vorgehende Entscheidung"),
      "AG",
    )
    const dropdownItems = screen.getAllByLabelText("dropdown-option")
    expect(dropdownItems[0]).toHaveTextContent("AG Test")
    await user.click(dropdownItems[0])
    const button = screen.getByLabelText("Vorgehende Entscheidung speichern")
    await user.click(button)

    expect(screen.getByText(/AG Test/)).toBeVisible()
  })

  it("correctly updates value of fileNumber input", async () => {
    const { user } = renderComponent([generatePreviousDecision()])

    expect(screen.queryByText(/new fileNumber/)).not.toBeInTheDocument()
    await user.click(screen.getByTestId("list-entry-0"))

    const fileNumberInput = await screen.findByLabelText(
      "Aktenzeichen Vorgehende Entscheidung",
    )

    await user.clear(fileNumberInput)
    await user.type(fileNumberInput, "new fileNumber")
    const button = screen.getByLabelText("Vorgehende Entscheidung speichern")
    await user.click(button)

    expect(screen.getByText(/new fileNumber/)).toBeVisible()
  })

  it("correctly toggles value of date known checkbox", async () => {
    const { user } = renderComponent([generatePreviousDecision()])

    await user.click(screen.getByTestId("list-entry-0"))

    const checkbox = await screen.findByLabelText(
      "Datum Unbekannt Vorgehende Entscheidung",
    )

    expect(checkbox).not.toBeChecked()

    await user.click(checkbox)
    expect(checkbox).toBeChecked()
  })

  it("correctly updates value of decision date input", async () => {
    const { user } = renderComponent([generatePreviousDecision()])

    expect(screen.queryByText(/02.02.2022/)).not.toBeInTheDocument()
    await user.click(screen.getByTestId("list-entry-0"))

    const fileNumberInput = await screen.findByLabelText(
      "Entscheidungsdatum Vorgehende Entscheidung",
    )

    await user.clear(fileNumberInput)
    await user.type(fileNumberInput, "02.02.2022")
    const button = screen.getByLabelText("Vorgehende Entscheidung speichern")
    await user.click(button)

    expect(screen.getByText(/02.02.2022/)).toBeVisible()
  })

  it("correctly deletes manually added previous decisions", async () => {
    const { user } = renderComponent([
      generatePreviousDecision(),
      generatePreviousDecision(),
    ])
    const previousDecisions = screen.getAllByLabelText("Listen Eintrag")
    expect(previousDecisions.length).toBe(2)
    await user.click(screen.getByTestId("list-entry-0"))
    await user.click(screen.getByLabelText("Eintrag löschen"))
    expect(screen.getAllByLabelText("Listen Eintrag").length).toBe(1)
  })

  it("correctly deletes previous decisions added by search", async () => {
    const { user } = renderComponent([
      generatePreviousDecision(),
      generatePreviousDecision(),
    ])
    const previousDecisions = screen.getAllByLabelText("Listen Eintrag")
    expect(previousDecisions.length).toBe(2)
    await user.click(screen.getByTestId("list-entry-0"))
    await user.click(screen.getByLabelText("Eintrag löschen"))
    expect(screen.getAllByLabelText("Listen Eintrag").length).toBe(1)
  })

  it("correctly updates deleted values in previous decisions", async () => {
    const { user } = renderComponent([generatePreviousDecision()])

    expect(
      screen.getByText("label1, 01.02.2022, test fileNumber, documentType1"),
    ).toBeInTheDocument()
    await user.click(screen.getByTestId("list-entry-0"))

    const fileNumberInput = await screen.findByLabelText(
      "Aktenzeichen Vorgehende Entscheidung",
    )
    const courtInput = await screen.findByLabelText(
      "Gericht Vorgehende Entscheidung",
    )

    await user.clear(fileNumberInput)
    await user.clear(courtInput)

    await user.click(screen.getByLabelText("Vorgehende Entscheidung speichern"))

    expect(screen.getByText(/01.02.2022, documentType1/)).toBeInTheDocument()
  })

  it("renders from search added previous decisions as editable list item", async () => {
    renderComponent([
      generatePreviousDecision({
        documentNumber: "ABC",
      }),
    ])
    expect(screen.getByLabelText("Listen Eintrag")).toBeInTheDocument()
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
      await screen.findByLabelText("Aktenzeichen Vorgehende Entscheidung"),
      "test",
    )
    await user.keyboard("{Control>}{Enter}")

    expect(screen.getAllByText(/test fileNumber/).length).toBe(1)
  })

  it("adds previous decision from search results", async () => {
    const { user } = renderComponent()

    await user.click(await screen.findByLabelText("Nach Entscheidung suchen"))
    await user.click(await screen.findByLabelText("Treffer übernehmen"))
    expect((await screen.findAllByText(/test fileNumber/)).length).toBe(1)
  })

  it("indicates that search result already added to previous decisions", async () => {
    const { user } = renderComponent([
      generatePreviousDecision({ uuid: "123" }),
    ])
    await user.click(screen.getByText(/Weitere Angabe/))
    await user.click(screen.getByLabelText("Nach Entscheidung suchen"))
    expect(screen.getByText(/Bereits hinzugefügt/)).toBeInTheDocument()
  })

  it("displays error in list and edit component when fields missing", async () => {
    const { user } = renderComponent([generatePreviousDecision()])
    await user.click(screen.getByTestId("list-entry-0"))

    const fileInput = await screen.findByLabelText(
      "Aktenzeichen Vorgehende Entscheidung",
    )
    await user.clear(fileInput)
    await user.click(screen.getByLabelText("Vorgehende Entscheidung speichern"))
    expect(screen.getByText(/Fehlende Daten/)).toBeInTheDocument()
    await user.click(screen.getByTestId("list-entry-0"))
    expect(screen.getAllByText(/Pflichtfeld nicht befüllt/).length).toBe(1)
  })

  it("does not add previous decision with invalid date input", async () => {
    const { user } = renderComponent()

    const dateInput = await screen.findByLabelText(
      "Entscheidungsdatum Vorgehende Entscheidung",
    )
    expect(dateInput).toHaveValue("")

    await user.type(dateInput, "00.00.0231")

    await screen.findByText(/Kein valides Datum/)
    screen.getByLabelText("Vorgehende Entscheidung speichern").click()
    expect(dateInput).toBeVisible()
  })

  it("does not add previous decision with incomplete date input", async () => {
    const { user } = renderComponent()

    const dateInput = await screen.findByLabelText(
      "Entscheidungsdatum Vorgehende Entscheidung",
    )
    expect(dateInput).toHaveValue("")

    await user.type(dateInput, "01")
    await user.tab()

    await screen.findByText(/Unvollständiges Datum/)
    screen.getByLabelText("Vorgehende Entscheidung speichern").click()
    expect(dateInput).toBeVisible()
  })

  it("does not add previous decision with date in future", async () => {
    const { user } = renderComponent()

    const dateInput = await screen.findByLabelText(
      "Entscheidungsdatum Vorgehende Entscheidung",
    )
    expect(dateInput).toHaveValue("")

    await user.type(dateInput, "01.02.2090")
    await user.tab()

    await screen.findByText(/Das Datum darf nicht in der Zukunft liegen/)
    screen.getByLabelText("Vorgehende Entscheidung speichern").click()
    expect(dateInput).toBeVisible()
  })
})
