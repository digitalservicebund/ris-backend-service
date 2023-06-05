import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import DivergentExpirationUndefinedInputGroup from "@/components/DivergentExpirationUndefinedInputGroup.vue"
import {
  Metadata,
  MetadatumType,
  NormCategory,
  UndefinedDate,
} from "@/domain/Norm"

function renderComponent(options?: { modelValue?: Metadata }) {
  const props = {
    modelValue: options?.modelValue ?? {},
  }
  return render(DivergentExpirationUndefinedInputGroup, { props })
}

describe("DivergentExpirationUndefinedInputGroup", () => {
  it("renders all inputs and correct model value entry", () => {
    renderComponent({
      modelValue: {
        [MetadatumType.UNDEFINED_DATE]: [UndefinedDate.UNDEFINED_NOT_PRESENT],
        [MetadatumType.NORM_CATEGORY]: [
          NormCategory.BASE_NORM,
          NormCategory.TRANSITIONAL_NORM,
          NormCategory.AMENDMENT_NORM,
        ],
      },
    })

    const Date = screen.getByLabelText(
      "Unbestimmtes abweichendes Außerkrafttretedatum Dropdown"
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

    expect(Date).toBeInTheDocument()
    expect(Date).toHaveValue("nicht vorhanden")

    expect(amendmentNormCheckBox).toBeChecked()
    expect(baseNormCheckBox).toBeChecked()
    expect(transitionalNormCheckBox).toBeChecked()
  })

  it("should change the modelvalue when update the input", async () => {
    const user = userEvent.setup()
    const modelValue = {}
    renderComponent({ modelValue })

    const dropdown = screen.getByLabelText(
      "Unbestimmtes abweichendes Außerkrafttretedatum Dropdown"
    ) as HTMLInputElement

    await userEvent.click(dropdown)
    await userEvent.click(screen.getByText("unbestimmt (unbekannt)"))

    const checkBoxInputs = screen.getAllByRole("checkbox")
    await user.click(checkBoxInputs[0])
    await user.click(checkBoxInputs[1])
    await user.click(checkBoxInputs[2])

    expect(modelValue).toEqual({
      [MetadatumType.UNDEFINED_DATE]: [UndefinedDate.UNDEFINED_UNKNOWN],
      [MetadatumType.NORM_CATEGORY]: [
        NormCategory.AMENDMENT_NORM,
        NormCategory.BASE_NORM,
        NormCategory.TRANSITIONAL_NORM,
      ],
    })
  })
})
