import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import NormReferenceInput from "@/components/NormReferenceInput.vue"
import NormReference from "@/domain/normReference"

function renderComponent(options?: { modelValue?: NormReference }) {
  const user = userEvent.setup()
  const props = {
    modelValue: new NormReference({ ...options?.modelValue }),
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
    expect(screen.getByLabelText("RIS-Abkürzung der Norm")).toBeInTheDocument()
    expect(screen.getByLabelText("Einzelnorm der Norm")).toBeInTheDocument()
    expect(screen.getByLabelText("Fassungsdatum der Norm")).toBeInTheDocument()
    expect(screen.getByLabelText("Jahr der Norm")).toBeInTheDocument()
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

    const abbreviationField = screen.getByLabelText("RIS-Abkürzung der Norm")

    const singleNormField = screen.getByLabelText("Einzelnorm der Norm")

    const versionField = screen.getByLabelText("Fassungsdatum der Norm")

    const relevanceField = screen.getByLabelText("Jahr der Norm")

    expect(abbreviationField).toHaveValue("ABC")
    expect(singleNormField).toHaveValue("12")
    expect(versionField).toHaveValue("31.01.2022")
    expect(relevanceField).toHaveValue("2023")
  })
})
