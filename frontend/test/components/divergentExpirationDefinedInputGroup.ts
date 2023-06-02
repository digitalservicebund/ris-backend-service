import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import DivergentExpirationDefinedInputGroup from "@/components/DivergentExpirationDefinedInputGroup.vue"
import { Metadata, NormCategory } from "@/domain/Norm"

function renderComponent(options?: { modelValue?: Metadata }) {
  const props = {
    modelValue: options?.modelValue ?? {},
  }
  return render(DivergentExpirationDefinedInputGroup, { props })
}

function getControls() {
  const dateInput = screen.getByLabelText(
    "Bestimmtes abweichendes Außerkrafttretedatum Date Input"
  ) as HTMLInputElement

  const amendmentNormCheckBox = screen.getByRole("checkbox", {
    name: "Änderungsnorm",
  }) as HTMLInputElement

  const baseNormCheckBox = screen.getByRole("checkbox", {
    name: "Stammnorm",
  }) as HTMLInputElement

  const transitionalNormCheckBox = screen.getByRole("checkbox", {
    name: "Übergangsnorm",
  }) as HTMLInputElement

  return {
    dateInput,
    amendmentNormCheckBox,
    baseNormCheckBox,
    transitionalNormCheckBox,
  }
}

describe("DivergentExpirationDefinedInputGroup", () => {
  it("renders all inputs and correct model value entry", () => {
    renderComponent({
      modelValue: {
        DATE: ["2023-01-01"],
        NORM_CATEGORY: [
          NormCategory.BASE_NORM,
          NormCategory.TRANSITIONAL_NORM,
          NormCategory.AMENDMENT_NORM,
        ],
      },
    })

    const {
      dateInput,
      amendmentNormCheckBox,
      baseNormCheckBox,
      transitionalNormCheckBox,
    } = getControls()

    expect(dateInput).toBeInTheDocument()
    expect(dateInput).toHaveValue("2023-01-01")
    expect(amendmentNormCheckBox).toBeChecked()
    expect(baseNormCheckBox).toBeChecked()
    expect(transitionalNormCheckBox).toBeChecked()
  })

  it("should change the modelvalue when update the input", async () => {
    const user = userEvent.setup()
    const modelValue = {}
    renderComponent({ modelValue })

    const {
      dateInput,
      amendmentNormCheckBox,
      baseNormCheckBox,
      transitionalNormCheckBox,
    } = getControls()

    await userEvent.type(dateInput, "2020-05-12")
    await userEvent.tab()

    await userEvent.type(dateInput, "2020-05-12")
    await userEvent.tab()

    await user.click(amendmentNormCheckBox)
    await user.click(baseNormCheckBox)
    await user.click(transitionalNormCheckBox)

    expect(modelValue).toEqual({
      DATE: ["2020-05-12T00:00:00.000Z"],
      NORM_CATEGORY: [
        NormCategory.AMENDMENT_NORM,
        NormCategory.BASE_NORM,
        NormCategory.TRANSITIONAL_NORM,
      ],
    })
  })

  it("should change the modelvalue when clearing the input", async () => {
    const user = userEvent.setup()
    const modelValue: Metadata = {
      DATE: ["2020-05-12T00:00:00.000Z"],
      NORM_CATEGORY: [
        NormCategory.AMENDMENT_NORM,
        NormCategory.BASE_NORM,
        NormCategory.TRANSITIONAL_NORM,
      ],
    }
    renderComponent({ modelValue })

    const {
      dateInput,
      amendmentNormCheckBox,
      baseNormCheckBox,
      transitionalNormCheckBox,
    } = getControls()

    expect(dateInput).toHaveValue("2020-05-12")
    await user.type(dateInput, "{backspace}")
    expect(modelValue.DATE).toBeUndefined()

    expect(amendmentNormCheckBox).toBeChecked()
    await user.click(amendmentNormCheckBox)
    expect(modelValue.NORM_CATEGORY).not.toContain(NormCategory.AMENDMENT_NORM)

    expect(baseNormCheckBox).toBeChecked()
    await user.click(baseNormCheckBox)
    expect(modelValue.NORM_CATEGORY).not.toContain(NormCategory.BASE_NORM)

    expect(transitionalNormCheckBox).toBeChecked()
    await user.click(transitionalNormCheckBox)
    expect(modelValue.NORM_CATEGORY).not.toContain(
      NormCategory.TRANSITIONAL_NORM
    )
  })
})
