import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import DivergentExpirationDefinedInputGroup from "@/components/DivergentExpirationDefinedInputGroup.vue"
import { Metadata, MetadatumType, NormCategory } from "@/domain/Norm"

function renderComponent(options?: { modelValue?: Metadata }) {
  const props = {
    modelValue: options?.modelValue ?? {},
  }
  return render(DivergentExpirationDefinedInputGroup, { props })
}

describe("DivergentExpirationDefinedInputGroup", () => {
  it("renders all inputs and correct model value entry", () => {
    renderComponent({
      modelValue: {
        [MetadatumType.DATE]: ["2023-01-01"],
        [MetadatumType.NORM_CATEGORY]: [
          NormCategory.BASE_NORM,
          NormCategory.TRANSITIONAL_NORM,
          NormCategory.AMENDMENT_NORM,
        ],
      },
    })

    const date = screen.getByLabelText(
      "Bestimmtes grundsätzliches Außerkrafttretedatum Date Input"
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

    expect(date).toBeInTheDocument()
    expect(date).toHaveValue("2023-01-01")

    expect(amendmentNormCheckBox).toBeChecked()
    expect(baseNormCheckBox).toBeChecked()
    expect(transitionalNormCheckBox).toBeChecked()
  })

  it("should change the modelvalue when update the input", async () => {
    const user = userEvent.setup()
    const modelValue = {}
    renderComponent({ modelValue })

    const Date = screen.getByLabelText(
      "Bestimmtes grundsätzliches Außerkrafttretedatum Date Input"
    ) as HTMLInputElement

    await userEvent.type(Date, "2020-05-12")
    await userEvent.tab()

    await userEvent.type(Date, "2020-05-12")
    await userEvent.tab()

    const checkBoxInputs = screen.getAllByRole("checkbox")
    await user.click(checkBoxInputs[0])
    await user.click(checkBoxInputs[1])
    await user.click(checkBoxInputs[2])

    expect(modelValue).toEqual({
      [MetadatumType.DATE]: ["2020-05-12T00:00:00.000Z"],
      [MetadatumType.NORM_CATEGORY]: [
        NormCategory.AMENDMENT_NORM,
        NormCategory.BASE_NORM,
        NormCategory.TRANSITIONAL_NORM,
      ],
    })
  })
})
