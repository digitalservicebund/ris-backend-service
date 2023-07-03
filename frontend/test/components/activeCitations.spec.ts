import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import ActiveCitations from "@/components/ActiveCitations.vue"
import ActiveCitation from "@/domain/activeCitation"
import { CitationStyle } from "@/domain/citationStyle"
import { Court, DocumentType } from "@/domain/documentUnit"
import comboboxItemService from "@/services/comboboxItemService"
import documentUnitService from "@/services/documentUnitService"
import { ComboboxItem } from "@/shared/components/input/types"

function renderComponent(options?: { modelValue?: ActiveCitation[] }) {
  const props = {
    modelValue: options?.modelValue ? options?.modelValue : [],
  }

  const user = userEvent.setup()
  return {
    user,
    ...render(ActiveCitations, {
      props,
      global: {
        stubs: { routerLink: { template: "<a><slot/></a>" } },
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
  dataSource?:
    | "NEURIS"
    | "MIGRATION"
    | "PROCEEDING_DECISION"
    | "ACTIVE_CITATION"
}) {
  const activeCitation = new ActiveCitation({
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
  })
  return activeCitation
}

describe("Active Citations", async () => {
  global.ResizeObserver = require("resize-observer-polyfill")

  vi.spyOn(
    documentUnitService,
    "searchByLinkedDocumentUnit"
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
        totalElements: 20,
        totalPages: 2,
        number: 0,
        numberOfElements: 20,
        first: true,
        last: false,
      },
    })
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

  const citationStyle: CitationStyle = {
    uuid: "123",
    jurisShortcut: "Änderungen",
    label: "Änderungen",
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

  const dropdownCitationStyleItems: ComboboxItem[] = [
    {
      label: citationStyle.label,
      value: citationStyle,
      additionalInformation: citationStyle.jurisShortcut,
    },
  ]
  vi.spyOn(comboboxItemService, "getCourts").mockImplementation(() =>
    Promise.resolve({ status: 200, data: dropdownCourtItems })
  )

  vi.spyOn(comboboxItemService, "getDocumentTypes").mockImplementation(() =>
    Promise.resolve({ status: 200, data: dropdownDocumentTypesItems })
  )

  vi.spyOn(comboboxItemService, "getCitationStyles").mockImplementation(() =>
    Promise.resolve({ status: 200, data: dropdownCitationStyleItems })
  )

  it("renders empty active citation in edit mode, when no activeCitations in list", async () => {
    renderComponent()
    expect(screen.getAllByLabelText("Listen Eintrag").length).toBe(1)
    expect(screen.getByLabelText("Art der Zitierung")).toBeVisible()
    expect(
      screen.getByLabelText("Entscheidungsdatum Aktivzitierung")
    ).toBeVisible()
    expect(
      screen.getByLabelText("Entscheidungsdatum Aktivzitierung")
    ).toBeInTheDocument()
    expect(
      screen.getByLabelText("Aktenzeichen Aktivzitierung")
    ).toBeInTheDocument()
    expect(
      screen.getByLabelText("Dokumenttyp Aktivzitierung")
    ).toBeInTheDocument()
  })

  it("renders activeCitations as list entries", () => {
    const modelValue: ActiveCitation[] = [
      generateActiveCitation({ fileNumber: "123" }),
      generateActiveCitation({ fileNumber: "345" }),
    ]
    renderComponent({ modelValue })

    expect(screen.queryByLabelText("Art der Zitierung")).not.toBeInTheDocument()
    expect(screen.getByText(/123/)).toBeInTheDocument()
    expect(screen.getByText(/345/)).toBeInTheDocument()
  })

  it("creates new active citation manually", async () => {
    const { user } = renderComponent()
    const input = screen.getByLabelText("Aktenzeichen Aktivzitierung")
    await user.type(input, "123")
    const button = screen.getByLabelText("Aktivzitierung speichern")
    await user.click(button)

    expect(screen.getAllByLabelText("Listen Eintrag").length).toBe(1)
  })

  it("click on edit icon, opens the list entry in edit mode", async () => {
    const { user } = renderComponent({
      modelValue: [
        generateActiveCitation({
          fileNumber: "123",
          dataSource: "ACTIVE_CITATION",
        }),
      ],
    })
    const button = screen.getByLabelText("Eintrag bearbeiten")
    await user.click(button)

    expect(screen.getByLabelText("Art der Zitierung")).toBeVisible()
    expect(
      screen.getByLabelText("Entscheidungsdatum Aktivzitierung")
    ).toBeVisible()
    expect(
      screen.getByLabelText("Entscheidungsdatum Aktivzitierung")
    ).toBeInTheDocument()
    expect(
      screen.getByLabelText("Aktenzeichen Aktivzitierung")
    ).toBeInTheDocument()
    expect(
      screen.getByLabelText("Dokumenttyp Aktivzitierung")
    ).toBeInTheDocument()
  })

  it("renders manually added active citations as editable list item", async () => {
    renderComponent({
      modelValue: [
        generateActiveCitation({
          dataSource: "ACTIVE_CITATION",
        }),
      ],
    })
    expect(screen.getByLabelText("Eintrag bearbeiten")).toBeInTheDocument()
  })

  it("correctly updates value citation style input", async () => {
    const { user } = renderComponent({
      modelValue: [
        generateActiveCitation({
          dataSource: "ACTIVE_CITATION",
        }),
      ],
    })

    expect(screen.queryByText(/Änderungen/)).not.toBeInTheDocument()

    const editButton = screen.getByLabelText("Eintrag bearbeiten")
    await user.click(editButton)

    await user.type(
      await screen.findByLabelText("Art der Zitierung"),
      "Änderungen"
    )
    const dropdownItems = screen.getAllByLabelText("dropdown-option")
    expect(dropdownItems[0]).toHaveTextContent("Änderungen")
    await user.click(dropdownItems[0])
    const button = screen.getByLabelText("Aktivzitierung speichern")
    await user.click(button)

    expect(screen.getByText(/Änderungen/)).toBeVisible()
  })

  it("correctly updates value document type input", async () => {
    const { user } = renderComponent({
      modelValue: [
        generateActiveCitation({
          dataSource: "ACTIVE_CITATION",
        }),
      ],
    })

    expect(screen.queryByText(/EuGH-Vorlage/)).not.toBeInTheDocument()

    const editButton = screen.getByLabelText("Eintrag bearbeiten")
    await user.click(editButton)

    await user.type(
      await screen.findByLabelText("Dokumenttyp Aktivzitierung"),
      "Ant"
    )
    const dropdownItems = screen.getAllByLabelText("dropdown-option")
    expect(dropdownItems[0]).toHaveTextContent("EuGH-Vorlage")
    await user.click(dropdownItems[0])
    const button = screen.getByLabelText("Aktivzitierung speichern")
    await user.click(button)

    expect(screen.getByText(/EuGH-Vorlage/)).toBeVisible()
  })

  it("correctly updates value court input", async () => {
    const { user } = renderComponent({
      modelValue: [
        generateActiveCitation({
          dataSource: "ACTIVE_CITATION",
        }),
      ],
    })

    expect(screen.queryByText(/AG Test/)).not.toBeInTheDocument()

    const editButton = screen.getByLabelText("Eintrag bearbeiten")
    await user.click(editButton)

    await user.type(
      await screen.findByLabelText("Gericht Aktivzitierung"),
      "AG"
    )
    const dropdownItems = screen.getAllByLabelText("dropdown-option")
    expect(dropdownItems[0]).toHaveTextContent("AG Test")
    await user.click(dropdownItems[0])
    const button = screen.getByLabelText("Aktivzitierung speichern")
    await user.click(button)

    expect(screen.getByText(/AG Test/)).toBeVisible()
  })

  it("correctly updates value of fileNumber input", async () => {
    const { user } = renderComponent({
      modelValue: [
        generateActiveCitation({
          dataSource: "ACTIVE_CITATION",
        }),
      ],
    })

    expect(screen.queryByText(/new fileNumber/)).not.toBeInTheDocument()
    const editButton = screen.getByLabelText("Eintrag bearbeiten")
    await user.click(editButton)

    const fileNumberInput = await screen.findByLabelText(
      "Aktenzeichen Aktivzitierung"
    )

    await user.clear(fileNumberInput)
    await user.type(fileNumberInput, "new fileNumber")
    const button = screen.getByLabelText("Aktivzitierung speichern")
    await user.click(button)

    expect(screen.getByText(/new fileNumber/)).toBeVisible()
  })

  it("correctly updates value of decision date input", async () => {
    const { user } = renderComponent({
      modelValue: [
        generateActiveCitation({
          dataSource: "ACTIVE_CITATION",
        }),
      ],
    })

    expect(screen.queryByText(/02.02.2022/)).not.toBeInTheDocument()
    const editButton = screen.getByLabelText("Eintrag bearbeiten")
    await user.click(editButton)

    const fileNumberInput = await screen.findByLabelText(
      "Entscheidungsdatum Aktivzitierung"
    )

    await user.clear(fileNumberInput)
    await user.type(fileNumberInput, "02.02.2022")
    const button = screen.getByLabelText("Aktivzitierung speichern")
    await user.click(button)

    expect(screen.getByText(/02.02.2022/)).toBeVisible()
  })

  it("correctly deletes manually added active citations", async () => {
    const { user } = renderComponent({
      modelValue: [
        generateActiveCitation({
          dataSource: "ACTIVE_CITATION",
        }),
        generateActiveCitation({
          dataSource: "ACTIVE_CITATION",
        }),
      ],
    })
    const activeCitations = screen.getAllByLabelText("Listen Eintrag")
    expect(activeCitations.length).toBe(2)
    const buttonList = screen.getAllByLabelText("Eintrag löschen")
    await user.click(buttonList[0])
    expect(screen.getAllByLabelText("Listen Eintrag").length).toBe(1)
  })

  it("correctly deletes active citations added by search", async () => {
    const modelValue: ActiveCitation[] = [
      generateActiveCitation(),
      generateActiveCitation(),
    ]
    const { user } = renderComponent({ modelValue })
    const activeCitations = screen.getAllByLabelText("Listen Eintrag")
    expect(activeCitations.length).toBe(2)
    const buttonList = screen.getAllByLabelText("Eintrag löschen")
    await user.click(buttonList[0])
    expect(screen.getAllByLabelText("Listen Eintrag").length).toBe(1)
  })

  it("correctly updates deleted values in active citations", async () => {
    const { user } = renderComponent({
      modelValue: [
        generateActiveCitation({
          dataSource: "ACTIVE_CITATION",
        }),
      ],
    })

    expect(
      screen.getByText("label1, 01.02.2022, test fileNumber, documentType1")
    ).toBeInTheDocument()
    const editButton = screen.getByLabelText("Eintrag bearbeiten")
    await user.click(editButton)

    const fileNumberInput = await screen.findByLabelText(
      "Aktenzeichen Aktivzitierung"
    )
    const courtInput = await screen.findByLabelText("Gericht Aktivzitierung")
    const documentTypeInput = await screen.findByLabelText(
      "Dokumenttyp Aktivzitierung"
    )
    const decisionDateInput = await screen.findByLabelText(
      "Entscheidungsdatum Aktivzitierung"
    )

    await user.clear(fileNumberInput)
    await user.clear(courtInput)
    await user.clear(documentTypeInput)
    await user.clear(decisionDateInput)

    expect(
      screen.queryByText(/label1, 01.02.2022, documentType1, test fileNumber/)
    ).not.toBeInTheDocument()
  })

  it("renders from search added active citations as non-editable list item", async () => {
    renderComponent({
      modelValue: [generateActiveCitation()],
    })
    expect(
      screen.queryByLabelText("Eintrag bearbeiten")
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
    const modelValue: ActiveCitation[] = [
      generateActiveCitation({ uuid: "123" }),
    ]
    const { user } = renderComponent({ modelValue })
    await user.click(screen.getByText(/Weitere Angabe/))
    await user.click(screen.getByLabelText("Nach Entscheidung suchen"))
    expect(screen.getByText(/Bereits hinzugefügt/)).toBeInTheDocument()
  })

  it("displays error in list and edit component when fields missing", async () => {
    const modelValue: ActiveCitation[] = [
      generateActiveCitation({ dataSource: "ACTIVE_CITATION" }),
    ]
    const { user } = renderComponent({ modelValue })
    const editButton = screen.getByLabelText("Eintrag bearbeiten")
    await user.click(editButton)

    const courtInput = await screen.findByLabelText("Gericht Aktivzitierung")
    await user.clear(courtInput)
    await user.click(screen.getByLabelText("Aktivzitierung speichern"))
    expect(screen.getByLabelText(/Fehlerhafte Eingabe/)).toBeInTheDocument()
    await user.click(editButton)
    expect(screen.getAllByText(/Pflichtfeld nicht befüllt/).length).toBe(1)
  })
})
