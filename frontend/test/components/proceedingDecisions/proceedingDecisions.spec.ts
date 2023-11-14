import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createPinia, setActivePinia } from "pinia"
import ProceedingDecisions from "@/components/proceedingDecisions/ProceedingDecisions.vue"
import { Court, DocumentType } from "@/domain/documentUnit"
import ProceedingDecision from "@/domain/proceedingDecision"
import comboboxItemService from "@/services/comboboxItemService"
import documentUnitService from "@/services/documentUnitService"
import { ComboboxItem } from "@/shared/components/input/types"

function renderComponent(options?: { modelValue?: ProceedingDecision[] }) {
  const props = {
    modelValue: options?.modelValue ? options?.modelValue : [],
  }

  // eslint-disable-next-line testing-library/await-async-events
  const user = userEvent.setup()
  return {
    user,
    ...render(ProceedingDecisions, {
      props,
      global: {
        stubs: { routerLink: { template: "<a><slot/></a>" } },
      },
    }),
  }
}

function generateProceedingDecision(options?: {
  uuid?: string
  documentNumber?: string
  court?: Court
  decisionDate?: string
  fileNumber?: string
  documentType?: DocumentType
  dataSource?:
    | "NEURIS"
    | "MIGRATION"
    | "PROCEEDING_DECISION"
    | "ACTIVE_CITATION"
  dateKnown?: boolean
}) {
  const activeCitation = new ProceedingDecision({
    uuid: options?.uuid ?? "123",
    documentNumber: "ABC",
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
    dataSource: options?.dataSource ?? "NEURIS",
    dateKnown: options?.dateKnown ?? true,
  })
  return activeCitation
}

describe("ProceedingDecisions", () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
  })

  vi.spyOn(
    documentUnitService,
    "searchByLinkedDocumentUnit",
  ).mockImplementation(() =>
    Promise.resolve({
      status: 200,
      data: {
        content: [
          new ProceedingDecision({
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
        totalElements: 20,
        totalPages: 2,
        number: 0,
        numberOfElements: 20,
        first: true,
        last: false,
      },
    }),
  )

  vi.spyOn(window, "scrollTo").mockImplementation(() => vi.fn())

  const court: Court = {
    type: "AG",
    location: "Test",
    label: "AG Test",
  }

  const documentType: DocumentType = {
    jurisShortcut: "Ant",
    label: "EuGH-Vorlage",
  }

  const dropdownCourtItems: ComboboxItem[] = [
    {
      label: court.label,
      value: court,
      additionalInformation: court.revoked,
    },
  ]

  const dropdownDocumentTypesItems: ComboboxItem[] = [
    {
      label: documentType.label,
      value: documentType,
      additionalInformation: documentType.jurisShortcut,
    },
  ]

  vi.spyOn(comboboxItemService, "getCourts").mockImplementation(() =>
    Promise.resolve({ status: 200, data: dropdownCourtItems }),
  )

  vi.spyOn(comboboxItemService, "getDocumentTypes").mockImplementation(() =>
    Promise.resolve({ status: 200, data: dropdownDocumentTypesItems }),
  )

  it("renders empty proceeding decision in edit mode, when no proceedingDecisions in list", async () => {
    renderComponent()
    expect(screen.getAllByLabelText("Listen Eintrag").length).toBe(1)
    expect(screen.getByLabelText("Gericht Rechtszug")).toBeVisible()
    expect(screen.getByLabelText("Entscheidungsdatum Rechtszug")).toBeVisible()
    expect(
      screen.getByLabelText("Datum Unbekannt Rechtszug"),
    ).toBeInTheDocument()
    expect(screen.getByLabelText("Aktenzeichen Rechtszug")).toBeInTheDocument()
    expect(screen.getByLabelText("Dokumenttyp Rechtszug")).toBeInTheDocument()
    expect(
      screen.getByLabelText("Vorgehende Entscheidung speichern"),
    ).toBeDisabled()
  })

  it("renders proceedingDecisions as list entries", () => {
    const modelValue: ProceedingDecision[] = [
      generateProceedingDecision({ fileNumber: "123" }),
      generateProceedingDecision({ fileNumber: "345" }),
    ]
    renderComponent({ modelValue })

    expect(
      screen.queryByLabelText("Vorgehende Entscheidung speichern"),
    ).not.toBeInTheDocument()
    expect(screen.getByText(/123/)).toBeInTheDocument()
    expect(screen.getByText(/345/)).toBeInTheDocument()
  })

  it("creates new proceeding desision manually", async () => {
    const { user } = renderComponent()
    const input = screen.getByLabelText("Aktenzeichen Rechtszug")
    await user.type(input, "123")
    const button = screen.getByLabelText("Vorgehende Entscheidung speichern")
    await user.click(button)

    expect(screen.getAllByLabelText("Listen Eintrag").length).toBe(1)
  })

  it("click on edit icon, opens the list entry in edit mode", async () => {
    const { user } = renderComponent({
      modelValue: [
        generateProceedingDecision({
          fileNumber: "123",
          dataSource: "PROCEEDING_DECISION",
        }),
      ],
    })
    const button = screen.getByLabelText("Eintrag bearbeiten")
    await user.click(button)

    expect(screen.getByLabelText("Gericht Rechtszug")).toBeVisible()
    expect(screen.getByLabelText("Entscheidungsdatum Rechtszug")).toBeVisible()
    expect(
      screen.getByLabelText("Datum Unbekannt Rechtszug"),
    ).toBeInTheDocument()
    expect(screen.getByLabelText("Aktenzeichen Rechtszug")).toBeInTheDocument()
    expect(screen.getByLabelText("Dokumenttyp Rechtszug")).toBeInTheDocument()
  })

  it("renders manually added decision as editable list item", async () => {
    renderComponent({
      modelValue: [
        generateProceedingDecision({
          dataSource: "PROCEEDING_DECISION",
        }),
      ],
    })
    expect(screen.getByLabelText("Eintrag bearbeiten")).toBeInTheDocument()
  })

  it("correctly updates value court input", async () => {
    const { user } = renderComponent({
      modelValue: [
        generateProceedingDecision({
          dataSource: "PROCEEDING_DECISION",
        }),
      ],
    })

    expect(screen.queryByText(/AG Test/)).not.toBeInTheDocument()

    const editButton = screen.getByLabelText("Eintrag bearbeiten")
    await user.click(editButton)

    await user.type(await screen.findByLabelText("Gericht Rechtszug"), "AG")
    const dropdownItems = screen.getAllByLabelText("dropdown-option")
    expect(dropdownItems[0]).toHaveTextContent("AG Test")
    await user.click(dropdownItems[0])
    const button = screen.getByLabelText("Vorgehende Entscheidung speichern")
    await user.click(button)

    expect(screen.getByText(/AG Test/)).toBeVisible()
  })

  it("correctly updates value of fileNumber input", async () => {
    const { user } = renderComponent({
      modelValue: [
        generateProceedingDecision({
          dataSource: "PROCEEDING_DECISION",
        }),
      ],
    })

    expect(screen.queryByText(/new fileNumber/)).not.toBeInTheDocument()
    const editButton = screen.getByLabelText("Eintrag bearbeiten")
    await user.click(editButton)

    const fileNumberInput = await screen.findByLabelText(
      "Aktenzeichen Rechtszug",
    )

    await user.clear(fileNumberInput)
    await user.type(fileNumberInput, "new fileNumber")
    const button = screen.getByLabelText("Vorgehende Entscheidung speichern")
    await user.click(button)

    expect(screen.getByText(/new fileNumber/)).toBeVisible()
  })

  it("correctly updates value of decision date input", async () => {
    const { user } = renderComponent({
      modelValue: [
        generateProceedingDecision({
          dataSource: "PROCEEDING_DECISION",
        }),
      ],
    })

    expect(screen.queryByText(/02.02.2022/)).not.toBeInTheDocument()
    const editButton = screen.getByLabelText("Eintrag bearbeiten")
    await user.click(editButton)

    const fileNumberInput = await screen.findByLabelText(
      "Entscheidungsdatum Rechtszug",
    )

    await user.clear(fileNumberInput)
    await user.type(fileNumberInput, "02.02.2022")
    const button = screen.getByLabelText("Vorgehende Entscheidung speichern")
    await user.click(button)

    expect(screen.getByText(/02.02.2022/)).toBeVisible()
  })

  it("correctly deletes manually added active citations", async () => {
    const { user } = renderComponent({
      modelValue: [
        generateProceedingDecision({
          dataSource: "PROCEEDING_DECISION",
        }),
        generateProceedingDecision({
          dataSource: "PROCEEDING_DECISION",
        }),
      ],
    })
    const proceedingDecisions = screen.getAllByLabelText("Listen Eintrag")
    expect(proceedingDecisions.length).toBe(2)
    const buttonList = screen.getAllByLabelText("Eintrag löschen")
    await user.click(buttonList[0])
    expect(screen.getAllByLabelText("Listen Eintrag").length).toBe(1)
  })

  it("correctly deletes active citations added by search", async () => {
    const modelValue: ProceedingDecision[] = [
      generateProceedingDecision(),
      generateProceedingDecision(),
    ]
    const { user } = renderComponent({ modelValue })
    const proceedingDecisions = screen.getAllByLabelText("Listen Eintrag")
    expect(proceedingDecisions.length).toBe(2)
    const buttonList = screen.getAllByLabelText("Eintrag löschen")
    await user.click(buttonList[0])
    expect(screen.getAllByLabelText("Listen Eintrag").length).toBe(1)
  })

  it("correctly updates deleted values in active citations", async () => {
    const { user } = renderComponent({
      modelValue: [
        generateProceedingDecision({
          dataSource: "PROCEEDING_DECISION",
        }),
      ],
    })

    expect(
      screen.getByText(
        "label1, 01.02.2022, test fileNumber, documentTypeShortcut1",
      ),
    ).toBeInTheDocument()
    const editButton = screen.getByLabelText("Eintrag bearbeiten")
    await user.click(editButton)

    const fileNumberInput = await screen.findByLabelText(
      "Aktenzeichen Rechtszug",
    )
    const courtInput = await screen.findByLabelText("Gericht Rechtszug")

    await user.clear(fileNumberInput)
    await user.clear(courtInput)

    await user.click(screen.getByLabelText("Vorgehende Entscheidung speichern"))

    expect(
      screen.getByText(/01.02.2022, documentTypeShortcut1/),
    ).toBeInTheDocument()
  })

  it("renders from search added active citations as non-editable list item", async () => {
    renderComponent({
      modelValue: [generateProceedingDecision()],
    })
    expect(
      screen.queryByLabelText("Eintrag bearbeiten"),
    ).not.toBeInTheDocument()
  })

  it("lists search results", async () => {
    const { user } = renderComponent()

    expect(screen.queryByText(/test fileNumber/)).not.toBeInTheDocument()
    await user.click(screen.getByLabelText("Nach Entscheidung suchen"))

    expect(screen.getAllByText(/test fileNumber/).length).toBe(1)
  })

  it("adds active citation from search results", async () => {
    const { user } = renderComponent()

    await user.click(screen.getByLabelText("Nach Entscheidung suchen"))
    await user.click(screen.getByLabelText("Treffer übernehmen"))
    expect(screen.getAllByText(/test fileNumber/).length).toBe(1)
  })

  it("indicates that search result already added to active citations", async () => {
    const modelValue: ProceedingDecision[] = [
      generateProceedingDecision({ uuid: "123" }),
    ]
    const { user } = renderComponent({ modelValue })
    await user.click(screen.getByText(/Weitere Angabe/))
    await user.click(screen.getByLabelText("Nach Entscheidung suchen"))
    expect(screen.getByText(/Bereits hinzugefügt/)).toBeInTheDocument()
  })

  it("displays error in list and edit component when fields missing", async () => {
    const modelValue: ProceedingDecision[] = [
      generateProceedingDecision({ dataSource: "PROCEEDING_DECISION" }),
    ]
    const { user } = renderComponent({ modelValue })
    const editButton = screen.getByLabelText("Eintrag bearbeiten")
    await user.click(editButton)

    const fileInput = await screen.findByLabelText("Aktenzeichen Rechtszug")
    await user.clear(fileInput)
    await user.click(screen.getByLabelText("Vorgehende Entscheidung speichern"))
    expect(screen.getByLabelText(/Fehlerhafte Eingabe/)).toBeInTheDocument()
    await user.click(editButton)
    expect(screen.getAllByText(/Pflichtfeld nicht befüllt/).length).toBe(1)
  })
})
