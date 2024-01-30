import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createPinia, setActivePinia } from "pinia"
import ActiveCitations from "@/components/ActiveCitations.vue"
import ActiveCitation from "@/domain/activeCitation"
import { CitationType } from "@/domain/citationType"
import { Court, DocumentType } from "@/domain/documentUnit"
import comboboxItemService from "@/services/comboboxItemService"
import documentUnitService from "@/services/documentUnitService"
import { ComboboxItem } from "@/shared/components/input/types"

function renderComponent(options?: { modelValue?: ActiveCitation[] }) {
  const props = {
    modelValue: options?.modelValue ? options?.modelValue : [],
  }

  // eslint-disable-next-line testing-library/await-async-events
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
  referenceFound?: boolean
  court?: Court
  decisionDate?: string
  fileNumber?: string
  documentType?: DocumentType
  citationStyle?: CitationType
}) {
  const activeCitation = new ActiveCitation({
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
    citationType: options?.citationStyle ?? {
      uuid: "123",
      jurisShortcut: "Änderungen",
      label: "Änderungen",
    },
    referenceFound: options?.referenceFound ?? false,
  })
  return activeCitation
}

describe("active citations", () => {
  global.ResizeObserver = require("resize-observer-polyfill")
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

  const court: Court = {
    type: "AG",
    location: "Test",
    label: "AG Test",
  }

  const documentType: DocumentType = {
    jurisShortcut: "Ant",
    label: "EuGH-Vorlage",
  }

  const citationStyle: CitationType = {
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
    Promise.resolve({ status: 200, data: dropdownCourtItems }),
  )

  vi.spyOn(comboboxItemService, "getDocumentTypes").mockImplementation(() =>
    Promise.resolve({ status: 200, data: dropdownDocumentTypesItems }),
  )

  vi.spyOn(comboboxItemService, "getCitationTypes").mockImplementation(() =>
    Promise.resolve({ status: 200, data: dropdownCitationStyleItems }),
  )

  it("renders empty active citation in edit mode, when no activeCitations in list", async () => {
    renderComponent()
    expect((await screen.findAllByLabelText("Listen Eintrag")).length).toBe(1)
    expect(screen.getByLabelText("Art der Zitierung")).toBeVisible()
    expect(screen.getByLabelText("Gericht der Aktivzitierung")).toBeVisible()
    expect(
      screen.getByLabelText("Entscheidungsdatum der Aktivzitierung"),
    ).toBeVisible()
    expect(
      screen.getByLabelText("Aktenzeichen Aktivzitierung"),
    ).toBeInTheDocument()
    expect(
      screen.getByLabelText("Dokumenttyp der Aktivzitierung"),
    ).toBeInTheDocument()
    expect(screen.getByLabelText("Aktivzitierung speichern")).toBeDisabled()
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
    const input = await screen.findByLabelText("Aktenzeichen Aktivzitierung")
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
        }),
      ],
    })
    const itemHeader = screen.getByLabelText("Listen Eintrag")
    await user.click(itemHeader)

    expect(screen.getByLabelText("Art der Zitierung")).toBeVisible()
    expect(
      screen.getByLabelText("Entscheidungsdatum der Aktivzitierung"),
    ).toBeVisible()
    expect(
      screen.getByLabelText("Entscheidungsdatum der Aktivzitierung"),
    ).toBeInTheDocument()
    expect(
      screen.getByLabelText("Aktenzeichen Aktivzitierung"),
    ).toBeInTheDocument()
    expect(
      screen.getByLabelText("Dokumenttyp der Aktivzitierung"),
    ).toBeInTheDocument()
  })

  it("renders manually added active citations as editable list item", async () => {
    renderComponent({
      modelValue: [generateActiveCitation()],
    })
    expect(screen.getByLabelText("Listen Eintrag")).toBeInTheDocument()
  })

  it("correctly updates value citation style input", async () => {
    const { user } = renderComponent({
      modelValue: [
        generateActiveCitation({
          citationStyle: {
            uuid: "123",
            jurisShortcut: "ABC",
            label: "ABC",
          },
        }),
      ],
    })

    expect(screen.queryByText(/Änderungen/)).not.toBeInTheDocument()

    const itemHeader = screen.getByLabelText("Listen Eintrag")
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
    const { user } = renderComponent({
      modelValue: [generateActiveCitation()],
    })

    expect(screen.queryByText(/EuGH-Vorlage/)).not.toBeInTheDocument()

    const itemHeader = screen.getByLabelText("Listen Eintrag")
    await user.click(itemHeader)

    await user.type(
      await screen.findByLabelText("Dokumenttyp der Aktivzitierung"),
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
    const { user } = renderComponent({
      modelValue: [generateActiveCitation()],
    })

    expect(screen.queryByText(/AG Test/)).not.toBeInTheDocument()

    const itemHeader = screen.getByLabelText("Listen Eintrag")
    await user.click(itemHeader)

    await user.type(
      await screen.findByLabelText("Gericht der Aktivzitierung"),
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
    const { user } = renderComponent({
      modelValue: [generateActiveCitation()],
    })

    expect(screen.queryByText(/new fileNumber/)).not.toBeInTheDocument()
    const itemHeader = screen.getByLabelText("Listen Eintrag")
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
    const { user } = renderComponent({
      modelValue: [generateActiveCitation()],
    })

    expect(screen.queryByText(/02.02.2022/)).not.toBeInTheDocument()
    const itemHeader = screen.getByLabelText("Listen Eintrag")
    await user.click(itemHeader)

    const fileNumberInput = await screen.findByLabelText(
      "Entscheidungsdatum der Aktivzitierung",
    )

    await user.clear(fileNumberInput)
    await user.type(fileNumberInput, "02.02.2022")
    const button = screen.getByLabelText("Aktivzitierung speichern")
    await user.click(button)

    expect(screen.getByText(/02.02.2022/)).toBeVisible()
  })

  it("correctly deletes manually added active citations", async () => {
    const { user } = renderComponent({
      modelValue: [generateActiveCitation(), generateActiveCitation()],
    })
    const activeCitations = screen.getAllByLabelText("Listen Eintrag")
    expect(activeCitations.length).toBe(2)
    await user.click(activeCitations[0])
    await user.click(screen.getByLabelText("Eintrag löschen"))
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
    await user.click(activeCitations[0])
    await user.click(screen.getByLabelText("Eintrag löschen"))
    expect(screen.getAllByLabelText("Listen Eintrag").length).toBe(1)
  })

  it("correctly updates deleted values in active citations", async () => {
    const { user } = renderComponent({
      modelValue: [generateActiveCitation()],
    })

    expect(
      screen.getByText(
        "Änderungen, label1, 01.02.2022, test fileNumber, documentType1",
      ),
    ).toBeInTheDocument()
    const itemHeader = screen.getByLabelText("Listen Eintrag")
    await user.click(itemHeader)

    const fileNumberInput = await screen.findByLabelText(
      "Aktenzeichen Aktivzitierung",
    )
    const courtInput = await screen.findByLabelText(
      "Gericht der Aktivzitierung",
    )

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

  it("adds active citation from search results", async () => {
    const { user } = renderComponent()

    await user.click(await screen.findByLabelText("Nach Entscheidung suchen"))
    await user.click(await screen.findByLabelText("Treffer übernehmen"))
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
    const modelValue: ActiveCitation[] = [generateActiveCitation()]
    const { user } = renderComponent({ modelValue })
    const itemHeader = screen.getByLabelText("Listen Eintrag")
    await user.click(itemHeader)

    const fileInput = await screen.findByLabelText(
      "Aktenzeichen Aktivzitierung",
    )
    await user.clear(fileInput)
    await user.click(screen.getByLabelText("Aktivzitierung speichern"))
    expect(screen.getByLabelText(/Fehlerhafte Eingabe/)).toBeInTheDocument()
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
    const { user } = renderComponent({
      modelValue: [
        generateActiveCitation({
          documentNumber: "123",
          referenceFound: true,
          citationStyle: {
            label: "invalid",
          },
        }),
      ],
    })
    const itemHeader = screen.getByLabelText("Listen Eintrag")
    await user.click(itemHeader)

    expect(screen.getByText("Art der Zitierung *")).toBeVisible()
    expect(screen.getAllByText(/Pflichtfeld nicht befüllt/).length).toBe(1)
  })
})
