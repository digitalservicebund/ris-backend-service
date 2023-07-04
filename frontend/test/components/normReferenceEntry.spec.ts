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
  vi.mock("@/services/featureToggleService", () => {
    return {
      default: {
        isEnabled: vi.fn().mockReturnValue(true),
      },
    }
  })

  it("render empty norm input entry", () => {
    renderComponent()
    expect(screen.getByLabelText("Norm RIS-Abkürzung")).toBeInTheDocument()
    expect(screen.getByLabelText("Norm Einzelnorm")).toBeInTheDocument()
    expect(screen.getByLabelText("Norm Fassungsdatum")).toBeInTheDocument()
    expect(screen.getByLabelText("Norm Jahr")).toBeInTheDocument()
  })

  it("render values from modelValue prop", () => {
    const { screen } = renderComponent({
      modelValue: {
        normAbbreviation: { id: "123", abbreviation: "ABC" },
        singleNorm: "12",
        dateOfVersion: "2022-01-31T23:00:00Z",
        dateOfRelevance: "2023",
      } as NormReference,
    })

    const abbreviationField = screen.getByLabelText("Norm RIS-Abkürzung")

    const singleNormField = screen.getByLabelText("Norm Einzelnorm")

    const versionField = screen.getByLabelText("Norm Fassungsdatum")

    const relevanceField = screen.getByLabelText("Norm Jahr")

    expect(abbreviationField).toHaveValue("ABC")
    expect(singleNormField).toHaveValue("12")
    expect(versionField).toHaveValue("31.01.2022")
    expect(relevanceField).toHaveValue("2023")
  })
})
