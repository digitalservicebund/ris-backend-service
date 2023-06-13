import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import DivergentUndefinedInputGroup from "@/components/DivergentUndefinedInputGroup.vue"
import {
  Metadata,
  MetadataSectionName,
  MetadatumType,
  NormCategory,
  UndefinedDate,
} from "@/domain/Norm"

function renderComponent(options?: {
  modelValue?: Metadata
  id: string
  label: string
  sectionName: MetadataSectionName
}) {
  const props = {
    modelValue: options?.modelValue ?? {},
    id: options?.id,
    label: options?.label,
    sectionName: options?.sectionName,
  }
  return render(DivergentUndefinedInputGroup, { props })
}

describe("DivergentEntryIntoForceUndefinedInputGroup", () => {
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
      id: "divergentEntryIntoForceUndefinedDate",
      label: "Unbestimmtes abweichendes Inkrafttretedatum",
      sectionName: MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED,
    })

    const dropdown = screen.getByLabelText(
      "Unbestimmtes abweichendes Inkrafttretedatum Dropdown"
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

    expect(dropdown).toBeInTheDocument()
    expect(dropdown).toHaveValue("nicht vorhanden")

    expect(amendmentNormCheckBox).toBeChecked()
    expect(baseNormCheckBox).toBeChecked()
    expect(transitionalNormCheckBox).toBeChecked()
  })

  it("should change the modelvalue when update the input", async () => {
    const user = userEvent.setup()
    const modelValue = {}
    renderComponent({
      modelValue: modelValue,
      id: "divergentEntryIntoForceUndefinedDate",
      label: "Unbestimmtes abweichendes Inkrafttretedatum",
      sectionName: MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED,
    })

    const dropdown = screen.getByLabelText(
      "Unbestimmtes abweichendes Inkrafttretedatum Dropdown"
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
