import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import NormReferenceInput from "@/components/NormReferenceInput.vue"
import NormReference from "@/domain/normReference"

function renderComponent(options?: { modelValue?: NormReference }) {
  const user = userEvent.setup()
  const props = {
    modelValue: options?.modelValue,
  }
  const utils = render(NormReferenceInput, { props })
  return { screen, user, props, ...utils }
}

describe("NormReferenceEntry", () => {
  it("render empty norm input entry", () => {
    renderComponent()
    expect(screen.getByLabelText("RIS-Abkürzung")).toBeInTheDocument()
    expect(screen.getByLabelText("Einzelnorm")).toBeInTheDocument()
    expect(screen.getByLabelText("Fassungsdatum")).toBeInTheDocument()
    expect(screen.getByLabelText("Jahr")).toBeInTheDocument()
  })

  it("render values from modelValue prop", () => {
    const { screen } = renderComponent({
      modelValue: {
        normAbbreviation: { id: "123", abbreviation: "ABC" },
        singleNorm: "12",
        dateOfVersion: "2022-01-31T23:00:00Z",
        dateOfRelevance: "2023",
      },
    })

    const abbreviationField = screen.getByLabelText("RIS-Abkürzung")

    const singleNormField = screen.getByLabelText("Einzelnorm")

    const versionField = screen.getByLabelText("Fassungsdatum")

    const relevanceField = screen.getByLabelText("Jahr")

    expect(abbreviationField).toHaveValue("ABC")
    expect(singleNormField).toHaveValue("12")
    expect(versionField).toHaveValue("31.01.2022")
    expect(relevanceField).toHaveValue("2023")
  })
})
