import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createPinia, setActivePinia } from "pinia"
import EnsuingDecisions from "@/components/EnsuingDecisions.vue"
import { Court, DocumentType } from "@/domain/documentUnit"
import EnsuingDecision from "@/domain/ensuingDecision"
import comboboxItemService from "@/services/comboboxItemService"
import documentUnitService from "@/services/documentUnitService"
import { ComboboxItem } from "@/shared/components/input/types"

function renderComponent(options?: { modelValue?: EnsuingDecision[] }) {
  const props = {
    modelValue: options?.modelValue ? options?.modelValue : [],
  }

  // eslint-disable-next-line testing-library/await-async-events
  const user = userEvent.setup()
  return {
    user,
    ...render(EnsuingDecisions, {
      props,
      global: {
        stubs: { routerLink: { template: "<a><slot/></a>" } },
      },
    }),
  }
}

function generateEnsuingDecision(options?: {
  uuid?: string
  documentNumber?: string
  court?: Court
  decisionDate?: string
  fileNumber?: string
  documentType?: DocumentType
  referencedDocumentationUnitId?: string
  pending?: boolean
  note?: string
}) {
  const ensuingDecision = new EnsuingDecision({
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
    referencedDocumentationUnitId:
      options?.referencedDocumentationUnitId ?? undefined,
    pending: options?.pending ?? false,
    note: options?.note ?? undefined,
  })
  return ensuingDecision
}

describe("EnsuingDecisions", () => {
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
          new EnsuingDecision({
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

  it("renders empty ensuing decision in edit mode, when no ensuingDecisions in list", async () => {
    renderComponent()
    expect(screen.getByLabelText("Anhängige Entscheidung")).toBeVisible()
    expect(
      screen.getByLabelText("Gericht Nachgehende Entscheidung"),
    ).toBeVisible()
    expect(
      screen.getByLabelText("Entscheidungsdatum Nachgehende Entscheidung"),
    ).toBeVisible()
    expect(
      screen.getByLabelText("Aktenzeichen Nachgehende Entscheidung"),
    ).toBeInTheDocument()
    expect(
      screen.getByLabelText("Dokumenttyp Nachgehende Entscheidung"),
    ).toBeInTheDocument()
    expect(screen.getByLabelText("Vermerk")).toBeInTheDocument()
    expect(
      screen.getByLabelText("Nachgehende Entscheidung speichern"),
    ).toBeDisabled()
  })

  it("renders ensuing decisions as list entries", () => {
    const modelValue: EnsuingDecision[] = [
      generateEnsuingDecision({ fileNumber: "123" }),
      generateEnsuingDecision({ fileNumber: "345" }),
    ]
    renderComponent({ modelValue })

    expect(
      screen.queryByLabelText("Nachgehende Entscheidung speichern"),
    ).not.toBeInTheDocument()
    expect(screen.getByText(/123/)).toBeInTheDocument()
    expect(screen.getByText(/345/)).toBeInTheDocument()
  })

  it("creates new ensuing desision manually", async () => {
    const { user } = renderComponent()
    const input = screen.getByLabelText("Aktenzeichen Nachgehende Entscheidung")
    await user.type(input, "123")
    const button = screen.getByLabelText("Nachgehende Entscheidung speichern")
    await user.click(button)

    expect(screen.getAllByLabelText("Listen Eintrag").length).toBe(1)
  })

  it("click on edit icon, opens the list entry in edit mode", async () => {
    const { user } = renderComponent({
      modelValue: [
        generateEnsuingDecision({
          fileNumber: "123",
        }),
      ],
    })

    expect(
      screen.queryByLabelText("Nachgehende Entscheidung speichern"),
    ).not.toBeInTheDocument()

    const button = screen.getByLabelText("Eintrag bearbeiten")
    await user.click(button)

    expect(
      screen.getByLabelText("Nachgehende Entscheidung speichern"),
    ).toBeInTheDocument()
  })

  it("correctly toggles value of date known checkbox", async () => {
    const { user } = renderComponent({
      modelValue: [generateEnsuingDecision()],
    })

    const editButton = screen.getByLabelText("Eintrag bearbeiten")
    await user.click(editButton)

    const checkbox = await screen.findByLabelText("Anhängige Entscheidung")

    expect(checkbox).not.toBeChecked()

    await user.click(checkbox)
    expect(checkbox).toBeChecked()
  })

  it("correctly updates value court input", async () => {
    const { user } = renderComponent({
      modelValue: [generateEnsuingDecision()],
    })

    expect(screen.queryByText(/AG Test/)).not.toBeInTheDocument()

    const editButton = screen.getByLabelText("Eintrag bearbeiten")
    await user.click(editButton)

    await user.type(
      await screen.findByLabelText("Gericht Nachgehende Entscheidung"),
      "AG",
    )
    const dropdownItems = screen.getAllByLabelText("dropdown-option")
    expect(dropdownItems[0]).toHaveTextContent("AG Test")
    await user.click(dropdownItems[0])
    const button = screen.getByLabelText("Nachgehende Entscheidung speichern")
    await user.click(button)

    expect(screen.getByText(/AG Test/)).toBeVisible()
  })

  it("correctly updates value of fileNumber input", async () => {
    const { user } = renderComponent({
      modelValue: [generateEnsuingDecision()],
    })

    expect(screen.queryByText(/new fileNumber/)).not.toBeInTheDocument()
    const editButton = screen.getByLabelText("Eintrag bearbeiten")
    await user.click(editButton)

    const fileNumberInput = await screen.findByLabelText(
      "Aktenzeichen Nachgehende Entscheidung",
    )

    await user.clear(fileNumberInput)
    await user.type(fileNumberInput, "new fileNumber")
    const button = screen.getByLabelText("Nachgehende Entscheidung speichern")
    await user.click(button)

    expect(screen.getByText(/new fileNumber/)).toBeVisible()
  })

  it("correctly updates value of decision date input", async () => {
    const { user } = renderComponent({
      modelValue: [generateEnsuingDecision()],
    })

    expect(screen.queryByText(/02.02.2022/)).not.toBeInTheDocument()
    const editButton = screen.getByLabelText("Eintrag bearbeiten")
    await user.click(editButton)

    const fileNumberInput = await screen.findByLabelText(
      "Entscheidungsdatum Nachgehende Entscheidung",
    )

    await user.clear(fileNumberInput)
    await user.type(fileNumberInput, "02.02.2022")
    const button = screen.getByLabelText("Nachgehende Entscheidung speichern")
    await user.click(button)

    expect(screen.getByText(/02.02.2022/)).toBeVisible()
  })

  it("correctly deletes ensuing decision", async () => {
    const { user } = renderComponent({
      modelValue: [generateEnsuingDecision(), generateEnsuingDecision()],
    })
    const proceedingDecisions = screen.getAllByLabelText("Listen Eintrag")
    expect(proceedingDecisions.length).toBe(2)
    const buttonList = screen.getAllByLabelText("Eintrag löschen")
    await user.click(buttonList[0])
    expect(screen.getAllByLabelText("Listen Eintrag").length).toBe(1)
  })

  it("renders from search added ensuing decisions as non-editable list item", async () => {
    renderComponent({
      modelValue: [
        generateEnsuingDecision({
          documentNumber: "ABC",
          referencedDocumentationUnitId: "abc",
        }),
      ],
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

  it("adds ensuing decision from search results", async () => {
    const { user } = renderComponent()

    await user.click(screen.getByLabelText("Nach Entscheidung suchen"))
    await user.click(screen.getByLabelText("Treffer übernehmen"))
    expect(screen.getAllByText(/test fileNumber/).length).toBe(1)
  })

  it("indicates that search result already added to ensuing decisions", async () => {
    const modelValue: EnsuingDecision[] = [
      generateEnsuingDecision({ uuid: "123" }),
    ]
    const { user } = renderComponent({ modelValue })
    await user.click(screen.getByText(/Weitere Angabe/))
    await user.click(screen.getByLabelText("Nach Entscheidung suchen"))
    expect(screen.getByText(/Bereits hinzugefügt/)).toBeInTheDocument()
  })

  it("displays error in list and edit component when fields missing", async () => {
    const modelValue: EnsuingDecision[] = [generateEnsuingDecision()]
    const { user } = renderComponent({ modelValue })
    const editButton = screen.getByLabelText("Eintrag bearbeiten")
    await user.click(editButton)

    const fileInput = await screen.findByLabelText(
      "Aktenzeichen Nachgehende Entscheidung",
    )
    await user.clear(fileInput)
    await user.click(
      screen.getByLabelText("Nachgehende Entscheidung speichern"),
    )
    expect(screen.getByLabelText(/Fehlerhafte Eingabe/)).toBeInTheDocument()
    await user.click(editButton)
    expect(screen.getAllByText(/Pflichtfeld nicht befüllt/).length).toBe(1)
  })
})
