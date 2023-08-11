import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createPinia, setActivePinia } from "pinia"
import NormReferences from "@/components/NormReferences.vue"
import { NormAbbreviation } from "@/domain/normAbbreviation"
import NormReference from "@/domain/normReference"
import comboboxItemService from "@/services/comboboxItemService"
import documentUnitService from "@/services/documentUnitService"
import { ComboboxItem } from "@/shared/components/input/types"

function renderComponent(options?: { modelValue?: NormReference[] }) {
  const props = {
    modelValue: options?.modelValue ? options?.modelValue : [],
  }

  const user = userEvent.setup()
  return {
    user,
    ...render(NormReferences, {
      props,
      global: {
        stubs: { routerLink: { template: "<a><slot/></a>" } },
      },
    }),
  }
}

function generateNormReference(options?: {
  normAbbreviation?: NormAbbreviation
  singleNorm?: string
  dateOfVersion?: string
  dateOfRelevance?: string
}) {
  const normReference = new NormReference({
    normAbbreviation: options?.normAbbreviation ?? { abbreviation: "ABC" },
    singleNorm: options?.singleNorm ?? "",
    dateOfVersion: options?.dateOfVersion ?? "2022-02-01",
    dateOfRelevance: options?.dateOfRelevance ?? "2022",
  })
  return normReference
}

describe("Norm references", () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
  })
  const normAbbreviation: NormAbbreviation = {
    abbreviation: "1000g-BefV",
  }
  const dropdownAbbreviationItems: ComboboxItem[] = [
    {
      label: normAbbreviation.abbreviation,
      value: normAbbreviation,
    },
  ]
  vi.spyOn(comboboxItemService, "getRisAbbreviations").mockImplementation(() =>
    Promise.resolve({ status: 200, data: dropdownAbbreviationItems }),
  )

  it("renders empty norm reference in edit mode, when no norm references in list", () => {
    renderComponent()
    expect(screen.getAllByLabelText("Listen Eintrag").length).toBe(1)
    expect(screen.getByLabelText("RIS-Abkürzung der Norm")).toBeInTheDocument()
    expect(screen.getByLabelText("Einzelnorm der Norm")).toBeInTheDocument()
    expect(screen.getByLabelText("Fassungsdatum der Norm")).toBeInTheDocument()
    expect(screen.getByLabelText("Jahr der Norm")).toBeInTheDocument()
    expect(screen.getByLabelText("Jahr der Norm")).toBeInTheDocument()
    expect(screen.getByLabelText("Norm speichern")).toBeDisabled()
    expect(
      screen.queryByText(/Pflichtfeld nicht befüllt/),
    ).not.toBeInTheDocument()
  })

  it("renders norm references as list entries", () => {
    const modelValue: NormReference[] = [
      generateNormReference({ singleNorm: "§ 123" }),
      generateNormReference({ singleNorm: "§ 345" }),
    ]
    renderComponent({ modelValue })

    expect(screen.getAllByLabelText("Eintrag bearbeiten").length).toBe(2)
    expect(screen.getAllByLabelText("Eintrag löschen").length).toBe(2)
    expect(screen.getAllByLabelText("Listen Eintrag").length).toBe(2)
    expect(
      screen.queryByLabelText("RIS-Abkürzung der Norm"),
    ).not.toBeInTheDocument()
    expect(screen.getByText(/§ 123/)).toBeInTheDocument()
    expect(screen.getByText(/§ 345/)).toBeInTheDocument()
  })

  it("click on edit icon, opens the list entry in edit mode", async () => {
    vi.spyOn(documentUnitService, "validateSingleNorm").mockImplementation(() =>
      Promise.resolve({ status: 200, data: "Ok" }),
    )
    const { user } = renderComponent({
      modelValue: [
        generateNormReference({
          singleNorm: "§ 123",
        }),
      ],
    })
    const button = screen.getByLabelText("Eintrag bearbeiten")
    await user.click(button)

    expect(screen.getByLabelText("RIS-Abkürzung der Norm")).toBeInTheDocument()
    expect(screen.getByLabelText("Einzelnorm der Norm")).toBeInTheDocument()
    expect(screen.getByLabelText("Fassungsdatum der Norm")).toBeInTheDocument()
    expect(screen.getByLabelText("Jahr der Norm")).toBeInTheDocument()
  })

  it("correctly deletes norm reference", async () => {
    const { user } = renderComponent({
      modelValue: [generateNormReference(), generateNormReference()],
    })

    expect(screen.getAllByLabelText("Listen Eintrag").length).toBe(2)
    const buttonList = screen.getAllByLabelText("Eintrag löschen")
    await user.click(buttonList[0])
    expect(screen.getAllByLabelText("Listen Eintrag").length).toBe(1)
  })

  it("click on 'Weitere Angabe' adds new emptry list entry", async () => {
    const modelValue: NormReference[] = [
      generateNormReference(),
      generateNormReference(),
    ]
    const { user } = renderComponent({ modelValue })
    const normsRefernces = screen.getAllByLabelText("Listen Eintrag")
    expect(normsRefernces.length).toBe(2)
    const button = screen.getByLabelText("Weitere Angabe")
    await user.click(button)
    expect(screen.getAllByLabelText("Listen Eintrag").length).toBe(3)
  })

  it("displays error in list and edit component when fields missing", async () => {
    const modelValue: NormReference[] = [
      generateNormReference({
        normAbbreviation: { abbreviation: "CDE" },
      }),
    ]
    const { user } = renderComponent({ modelValue })
    await screen.findByText(/CDE, 01.02.2022, 2022/)
    const editButton = screen.getByLabelText("Eintrag bearbeiten")
    await user.click(editButton)

    const abbreviationInput = await screen.findByLabelText(
      "RIS-Abkürzung der Norm",
    )
    screen.getAllByLabelText("Auswahl zurücksetzen")[1].click()
    await user.clear(abbreviationInput)
    expect(abbreviationInput).toHaveValue("")
    await user.click(screen.getByLabelText("Norm speichern"))
    await screen.findByText(/01.02.2022, 2022/)
    expect(screen.getByLabelText(/Fehlerhafte Eingabe/)).toBeInTheDocument()
    await user.click(editButton)
    expect(screen.getAllByText(/Pflichtfeld nicht befüllt/).length).toBe(1)
  })
})
