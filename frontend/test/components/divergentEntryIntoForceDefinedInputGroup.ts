import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import DivergentEntryIntoForceDefinedInputGroup from "@/components/DivergentEntryIntoForceDefinedInputGroup.vue"
import { Metadata, MetadatumType, NormCategory } from "@/domain/Norm"

function renderComponent(options?: { modelValue?: Metadata }) {
  const props = {
    modelValue: options?.modelValue ?? {},
  }
  return render(DivergentEntryIntoForceDefinedInputGroup, { props })
}

describe("DivergentEntryIntoForceDefinedInputGroup", () => {
  it("renders all DivergentEntryIntoForceDefined inputs and correct model value entry", () => {
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

    const divergentEntryIntoForceDefinedDate = screen.getByLabelText(
      "Bestimmtes grundsätzliches Inkrafttretedatum Date Input"
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

    expect(divergentEntryIntoForceDefinedDate).toBeInTheDocument()
    expect(divergentEntryIntoForceDefinedDate).toHaveValue("2023-01-01")

    expect(amendmentNormCheckBox).toBeChecked()
    expect(baseNormCheckBox).toBeChecked()
    expect(transitionalNormCheckBox).toBeChecked()
  })

  it("should change the modelvalue when update the input", async () => {
    const user = userEvent.setup()
    const modelValue = {}
    renderComponent({ modelValue })

    const divergentEntryIntoForceDefinedDate = screen.getByLabelText(
      "Bestimmtes grundsätzliches Inkrafttretedatum Date Input"
    ) as HTMLInputElement

    await userEvent.type(divergentEntryIntoForceDefinedDate, "2020-05-12")
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
