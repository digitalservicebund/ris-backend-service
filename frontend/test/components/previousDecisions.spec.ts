import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createPinia, setActivePinia } from "pinia"
import PreviousDecisions from "@/components/PreviousDecisions.vue"
import { Court, DocumentType } from "@/domain/documentUnit"
import PreviousDecision from "@/domain/previousDecision"
import comboboxItemService from "@/services/comboboxItemService"
import documentUnitService from "@/services/documentUnitService"
import { ComboboxItem } from "@/shared/components/input/types"

function renderComponent(options?: { modelValue?: PreviousDecision[] }) {
  const props = {
    modelValue: options?.modelValue ? options?.modelValue : [],
  }

  // eslint-disable-next-line testing-library/await-async-events
  const user = userEvent.setup()
  return {
    user,
    ...render(PreviousDecisions, {
      props,
      global: {
        stubs: { routerLink: { template: "<a><slot/></a>" } },
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
    uuid: options?.uuid ?? "123",
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
  beforeEach(async () => {
    setActivePinia(createPinia())
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

  it("renders empty previous decision in edit mode, when no previousDecisions in list", async () => {
    renderComponent()
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
    expect(
      screen.getByLabelText("Vorgehende Entscheidung speichern"),
    ).toBeDisabled()
  })

  it("renders proceedingDecisions as list entries", () => {
    const modelValue: PreviousDecision[] = [
      generatePreviousDecision({ fileNumber: "123" }),
      generatePreviousDecision({ fileNumber: "345" }),
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
    const input = screen.getByLabelText("Aktenzeichen Vorgehende Entscheidung")
    await user.type(input, "123")
    const button = screen.getByLabelText("Vorgehende Entscheidung speichern")
    await user.click(button)

    expect(screen.getAllByLabelText("Listen Eintrag").length).toBe(1)
  })

  it("click on edit icon, opens the list entry in edit mode", async () => {
    const { user } = renderComponent({
      modelValue: [
        generatePreviousDecision({
          fileNumber: "123",
        }),
      ],
    })
    const button = screen.getByLabelText("Eintrag bearbeiten")
    await user.click(button)

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
    renderComponent({
      modelValue: [generatePreviousDecision()],
    })
    expect(screen.getByLabelText("Eintrag bearbeiten")).toBeInTheDocument()
  })

  it("correctly updates value court input", async () => {
    const { user } = renderComponent({
      modelValue: [generatePreviousDecision()],
    })

    expect(screen.queryByText(/AG Test/)).not.toBeInTheDocument()

    const editButton = screen.getByLabelText("Eintrag bearbeiten")
    await user.click(editButton)

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
    const { user } = renderComponent({
      modelValue: [generatePreviousDecision()],
    })

    expect(screen.queryByText(/new fileNumber/)).not.toBeInTheDocument()
    const editButton = screen.getByLabelText("Eintrag bearbeiten")
    await user.click(editButton)

    const fileNumberInput = await screen.findByLabelText(
      "Aktenzeichen Vorgehende Entscheidung",
    )

    await user.clear(fileNumberInput)
    await user.type(fileNumberInput, "new fileNumber")
    const button = screen.getByLabelText("Vorgehende Entscheidung speichern")
    await user.click(button)

    expect(screen.getByText(/new fileNumber/)).toBeVisible()
  })

  it("correctly updates value of decision date input", async () => {
    const { user } = renderComponent({
      modelValue: [generatePreviousDecision()],
    })

    expect(screen.queryByText(/02.02.2022/)).not.toBeInTheDocument()
    const editButton = screen.getByLabelText("Eintrag bearbeiten")
    await user.click(editButton)

    const fileNumberInput = await screen.findByLabelText(
      "Entscheidungsdatum Vorgehende Entscheidung",
    )

    await user.clear(fileNumberInput)
    await user.type(fileNumberInput, "02.02.2022")
    const button = screen.getByLabelText("Vorgehende Entscheidung speichern")
    await user.click(button)

    expect(screen.getByText(/02.02.2022/)).toBeVisible()
  })

  it("correctly deletes manually added active citations", async () => {
    const { user } = renderComponent({
      modelValue: [generatePreviousDecision(), generatePreviousDecision()],
    })
    const proceedingDecisions = screen.getAllByLabelText("Listen Eintrag")
    expect(proceedingDecisions.length).toBe(2)
    const buttonList = screen.getAllByLabelText("Eintrag löschen")
    await user.click(buttonList[0])
    expect(screen.getAllByLabelText("Listen Eintrag").length).toBe(1)
  })

  it("correctly deletes active citations added by search", async () => {
    const modelValue: PreviousDecision[] = [
      generatePreviousDecision(),
      generatePreviousDecision(),
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
      modelValue: [generatePreviousDecision()],
    })

    expect(
      screen.getByText(
        "label1, 01.02.2022, test fileNumber, documentTypeShortcut1",
      ),
    ).toBeInTheDocument()
    const editButton = screen.getByLabelText("Eintrag bearbeiten")
    await user.click(editButton)

    const fileNumberInput = await screen.findByLabelText(
      "Aktenzeichen Vorgehende Entscheidung",
    )
    const courtInput = await screen.findByLabelText(
      "Gericht Vorgehende Entscheidung",
    )

    await user.clear(fileNumberInput)
    await user.clear(courtInput)

    await user.click(screen.getByLabelText("Vorgehende Entscheidung speichern"))

    expect(
      screen.getByText(/01.02.2022, documentTypeShortcut1/),
    ).toBeInTheDocument()
  })

  it("renders from search added active citations as non-editable list item", async () => {
    renderComponent({
      modelValue: [generatePreviousDecision({ documentNumber: "ABC" })],
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
    const modelValue: PreviousDecision[] = [
      generatePreviousDecision({ uuid: "123" }),
    ]
    const { user } = renderComponent({ modelValue })
    await user.click(screen.getByText(/Weitere Angabe/))
    await user.click(screen.getByLabelText("Nach Entscheidung suchen"))
    expect(screen.getByText(/Bereits hinzugefügt/)).toBeInTheDocument()
  })

  it("displays error in list and edit component when fields missing", async () => {
    const modelValue: PreviousDecision[] = [generatePreviousDecision()]
    const { user } = renderComponent({ modelValue })
    const editButton = screen.getByLabelText("Eintrag bearbeiten")
    await user.click(editButton)

    const fileInput = await screen.findByLabelText(
      "Aktenzeichen Vorgehende Entscheidung",
    )
    await user.clear(fileInput)
    await user.click(screen.getByLabelText("Vorgehende Entscheidung speichern"))
    expect(screen.getByLabelText(/Fehlerhafte Eingabe/)).toBeInTheDocument()
    await user.click(editButton)
    expect(screen.getAllByText(/Pflichtfeld nicht befüllt/).length).toBe(1)
  })
})
