import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { ComboboxItem } from "@/components/input/types"
import NormReferenceInput from "@/components/NormReferenceInput.vue"
import { NormAbbreviation } from "@/domain/normAbbreviation"
import NormReference from "@/domain/normReference"
import comboboxItemService from "@/services/comboboxItemService"
import documentUnitService from "@/services/documentUnitService"

function renderComponent(options?: { modelValue?: NormReference }) {
  // eslint-disable-next-line testing-library/await-async-events
  const user = userEvent.setup()
  const props = {
    modelValue: new NormReference({ ...options?.modelValue }),
  }
  const utils = render(NormReferenceInput, { props })
  return { screen, user, props, ...utils }
}

describe("NormReferenceEntry", () => {
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
  vi.spyOn(documentUnitService, "validateSingleNorm").mockImplementation(() =>
    Promise.resolve({ status: 200, data: "Ok" }),
  )
  it("render empty norm input group on initial load", () => {
    renderComponent()
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

  it("render values if given", () => {
    const { screen } = renderComponent({
      modelValue: {
        normAbbreviation: { id: "123", abbreviation: "ABC" },
        singleNorm: "12",
        dateOfVersion: "2022-01-31",
        dateOfRelevance: "2023",
      } as NormReference,
    })

    const abbreviationField = screen.getByLabelText("RIS-Abkürzung der Norm")

    const singleNormField = screen.getByLabelText("Einzelnorm der Norm")

    const versionField = screen.getByLabelText("Fassungsdatum der Norm")

    const relevanceField = screen.getByLabelText("Jahr der Norm")

    expect(abbreviationField).toHaveValue("ABC")
    expect(singleNormField).toHaveValue("12")
    expect(versionField).toHaveValue("31.01.2022")
    expect(relevanceField).toHaveValue("2023")
    expect(screen.getByLabelText("Norm speichern")).toBeEnabled()
  })

  it("Add norm without valid single norm not possible", async () => {
    const { user, emitted } = renderComponent({
      modelValue: {
        singleNorm: "12",
      } as NormReference,
    })

    vi.spyOn(documentUnitService, "validateSingleNorm").mockImplementation(() =>
      Promise.resolve({ status: 200, data: "Validation error" }),
    )

    const singleNormInput = await screen.findByLabelText("Einzelnorm der Norm")
    expect(singleNormInput).toHaveValue("12")
    const button = screen.getByLabelText("Norm speichern")
    await user.click(button)

    await screen.findByText(/Inhalt nicht valide/)
    expect(emitted("update:modelValue")).toEqual(undefined)
  })

  it("Add norm without all required fields filled possible", async () => {
    const { user, emitted } = renderComponent({
      modelValue: {
        dateOfVersion: "2022-01-31T23:00:00Z",
        dateOfRelevance: "2023",
      } as NormReference,
    })

    const button = screen.getByLabelText("Norm speichern")
    await user.click(button)

    expect(emitted("addEntry")).toBeTruthy()
  })

  it("New input removes error message", async () => {
    const { user } = renderComponent({
      modelValue: {
        singleNorm: "12",
      } as NormReference,
    })

    const singleNormInput = await screen.findByLabelText("Einzelnorm der Norm")
    expect(singleNormInput).toHaveValue("12")
    const button = screen.getByLabelText("Norm speichern")
    await user.click(button)

    await screen.findByText(/Inhalt nicht valide/)

    await user.type(singleNormInput, "{backspace}")

    expect(screen.queryByText(/Inhalt nicht valide/)).not.toBeInTheDocument()
  })

  it("correctly updates the value of ris abbreviation input", async () => {
    const { user, emitted } = renderComponent()
    const abbreviationField = screen.getByLabelText("RIS-Abkürzung der Norm")

    await user.type(abbreviationField, "1000")
    const dropdownItems = screen.getAllByLabelText(
      "dropdown-option",
    ) as HTMLElement[]
    expect(dropdownItems[0]).toHaveTextContent("1000g-BefV")
    await user.click(dropdownItems[0])

    const button = screen.getByLabelText("Norm speichern")
    await user.click(button)
    expect(emitted("update:modelValue")).toEqual([
      [
        {
          dateOfRelevance: undefined,
          dateOfVersion: undefined,
          normAbbreviation: {
            abbreviation: "1000g-BefV",
          },
          singleNorm: undefined,
          hasForeignSource: false,
        },
      ],
    ])
  })

  it("correctly updates the value of the single norm input", async () => {
    const { user } = renderComponent()
    const singleNormInput = await screen.findByLabelText("Einzelnorm der Norm")

    await user.type(singleNormInput, "§ 123")
    expect(singleNormInput).toHaveValue("§ 123")
  })

  it("correctly updates the value of the version date input", async () => {
    const { user } = renderComponent()

    const versionField = screen.getByLabelText("Fassungsdatum der Norm")
    await user.type(versionField, "31.01.2022")

    expect(versionField).toHaveValue("31.01.2022")
  })

  it("correctly updates the value of the version date input", async () => {
    const { user } = renderComponent()

    const relevanceField = screen.getByLabelText("Jahr der Norm")
    await user.type(relevanceField, "2023")

    expect(relevanceField).toHaveValue("2023")
  })
})
