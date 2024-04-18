import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { ComboboxItem } from "@/components/input/types"
import NormReferences from "@/components/NormReferences.vue"
import { NormAbbreviation } from "@/domain/normAbbreviation"
import NormReference from "@/domain/normReference"
import SingleNorm from "@/domain/singleNorm"
import comboboxItemService from "@/services/comboboxItemService"
import documentUnitService from "@/services/documentUnitService"

function renderComponent(options?: { modelValue?: NormReference[] }) {
  const props = {
    modelValue: options?.modelValue ? options?.modelValue : [],
  }

  // eslint-disable-next-line testing-library/await-async-events
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
  singleNorms?: SingleNorm[]
}) {
  const normReference = new NormReference({
    normAbbreviation: options?.normAbbreviation ?? { abbreviation: "ABC" },
    singleNorms: options?.singleNorms ?? [],
  })
  return normReference
}

describe("Norm references", () => {
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

  it("renders empty norm reference in edit mode, when no norm references in list", async () => {
    renderComponent()
    expect((await screen.findAllByLabelText("Listen Eintrag")).length).toBe(1)
    expect(await screen.findByLabelText("RIS-Abkürzung")).toBeInTheDocument()
  })

  it("renders norm references as list entries", () => {
    const modelValue: NormReference[] = [
      generateNormReference({
        singleNorms: [new SingleNorm({ singleNorm: "§ 123" })],
      }),
      generateNormReference({
        singleNorms: [new SingleNorm({ singleNorm: "§ 345" })],
      }),
    ]
    renderComponent({ modelValue })

    expect(screen.getAllByLabelText("Listen Eintrag").length).toBe(2)
    expect(screen.queryByLabelText("RIS-Abkürzung")).not.toBeInTheDocument()
    expect(screen.getByText(/§ 123/)).toBeInTheDocument()
    expect(screen.getByText(/§ 345/)).toBeInTheDocument()
  })

  it("click on list item, opens the list entry in edit mode", async () => {
    vi.spyOn(documentUnitService, "validateSingleNorm").mockImplementation(() =>
      Promise.resolve({ status: 200, data: "Ok" }),
    )
    const { user } = renderComponent({
      modelValue: [
        generateNormReference({
          singleNorms: [new SingleNorm({ singleNorm: "§ 123" })],
        }),
      ],
    })
    const itemHeader = screen.getByLabelText("Listen Eintrag")
    await user.click(itemHeader)

    expect(screen.getByLabelText("RIS-Abkürzung")).toBeInTheDocument()
    expect(screen.getByLabelText("Einzelnorm der Norm")).toBeInTheDocument()
    expect(screen.getByLabelText("Fassungsdatum der Norm")).toBeInTheDocument()
    expect(screen.getByLabelText("Jahr der Norm")).toBeInTheDocument()
  })

  it("updates norm reference", async () => {
    const { user, emitted } = renderComponent({
      modelValue: [generateNormReference()],
    })

    const norms = screen.getAllByLabelText("Listen Eintrag")
    expect(norms.length).toBe(1)
    await user.click(norms[0])
    const abbreviationField = screen.getByLabelText("RIS-Abkürzung")
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
        [
          {
            normAbbreviation: {
              abbreviation: "1000g-BefV",
            },
            singleNorms: [],
            normAbbreviationRawValue: undefined,
            hasForeignSource: false,
          },
        ],
      ],
    ])
  })

  it("deletes norm reference", async () => {
    const { user } = renderComponent({
      modelValue: [generateNormReference(), generateNormReference()],
    })

    const norms = screen.getAllByLabelText("Listen Eintrag")
    expect(norms.length).toBe(2)
    await user.click(norms[0])
    await user.click(screen.getByLabelText("Eintrag löschen"))
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

  it("render summary with one single norms", async () => {
    const modelValue: NormReference[] = [
      generateNormReference({
        normAbbreviation: {
          abbreviation: "1000g-BefV",
        },
        singleNorms: [new SingleNorm({ singleNorm: "§ 123" })],
      }),
    ]
    renderComponent({ modelValue })

    expect(screen.getByLabelText("Listen Eintrag")).toHaveTextContent(
      "1000g-BefV, § 123",
    )
  })

  it("render summary with multiple single norms", async () => {
    const modelValue: NormReference[] = [
      generateNormReference({
        normAbbreviation: {
          abbreviation: "1000g-BefV",
        },
        singleNorms: [
          new SingleNorm({ singleNorm: "§ 123" }),
          new SingleNorm({
            singleNorm: "§ 345",
            dateOfRelevance: "02-02-2022",
            dateOfVersion: "2022",
          }),
        ],
      }),
    ]
    renderComponent({ modelValue })

    expect(screen.getByLabelText("Listen Eintrag")).toHaveTextContent(
      "1000g-BefV1000g-BefV, § 1231000g-BefV, § 345, 01.01.2022, 02-02-2022",
    )
  })

  it("render summary with no single norms", async () => {
    const modelValue: NormReference[] = [
      generateNormReference({
        normAbbreviation: {
          abbreviation: "1000g-BefV",
        },
        singleNorms: [
          new SingleNorm({ singleNorm: "§ 123" }),
          new SingleNorm({
            singleNorm: "§ 345",
            dateOfRelevance: "02-02-2022",
            dateOfVersion: "2022",
          }),
        ],
      }),
    ]
    renderComponent({ modelValue })

    expect(screen.getByLabelText("Listen Eintrag")).toHaveTextContent(
      "1000g-BefV1000g-BefV, § 1231000g-BefV, § 345, 01.01.2022, 02-02-2022",
    )
  })

  it("render error badge, when norm reference is ambiguous", async () => {
    const modelValue: NormReference[] = [
      new NormReference({
        normAbbreviationRawValue: "EWGAssRBes 1/80",
      }),
    ]

    renderComponent({ modelValue })

    expect(screen.getByText("Mehrdeutiger Verweis")).toBeInTheDocument()
  })

  it("render error badge, when required fields missing", async () => {
    const modelValue: NormReference[] = [
      new NormReference({
        singleNorms: [new SingleNorm({ singleNorm: "§ 123" })],
      }),
    ]

    renderComponent({ modelValue })

    // Todo:
    // add check for error badge when implemented
  })
})
