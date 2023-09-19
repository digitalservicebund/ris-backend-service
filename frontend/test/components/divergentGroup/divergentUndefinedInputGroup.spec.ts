import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createPinia, setActivePinia } from "pinia"
import DivergentUndefinedInputGroup from "@/components/divergentGroup/DivergentUndefinedInputGroup.vue"
import {
  MetadataSectionName,
  MetadatumType,
  NormCategory,
  UndefinedDate,
} from "@/domain/norm"

type DivergentUndefinedInputGroupProps = InstanceType<
  typeof DivergentUndefinedInputGroup
>["$props"]

function renderComponent(props?: DivergentUndefinedInputGroupProps) {
  const effectiveProps = {
    modelValue: props?.modelValue ?? {},
    "onUpdate:modelValue": props?.["onUpdate:modelValue"],
    id: props?.id,
    label: props?.label,
    sectionName: props?.sectionName,
  }
  return render(DivergentUndefinedInputGroup, { props: effectiveProps })
}

describe("DivergentEntryIntoForceUndefinedInputGroup", () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
  })

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
      "Unbestimmtes abweichendes Inkrafttretedatum Dropdown",
    )

    const amendmentNormCheckBox = screen.getByRole("checkbox", {
      name: "Änderungsnorm",
    })
    const baseNormCheckBox = screen.getByRole("checkbox", {
      name: "Stammnorm",
    })
    const transitionalNormCheckBox = screen.getByRole("checkbox", {
      name: "Übergangsnorm",
    })

    expect(dropdown).toBeInTheDocument()
    expect(dropdown).toHaveValue(UndefinedDate.UNDEFINED_NOT_PRESENT)

    expect(amendmentNormCheckBox).toBeChecked()
    expect(baseNormCheckBox).toBeChecked()
    expect(transitionalNormCheckBox).toBeChecked()
  })

  it("should change the modelvalue when update the input", async () => {
    const user = userEvent.setup()
    let modelValue = {}
    const onUpdateModelValue = vi.fn().mockImplementation((value) => {
      modelValue = value
    })

    const { rerender } = renderComponent({
      modelValue: modelValue,
      "onUpdate:modelValue": onUpdateModelValue,
      id: "divergentEntryIntoForceUndefinedDate",
      label: "Unbestimmtes abweichendes Inkrafttretedatum",
      sectionName: MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED,
    })

    const dropdown = screen.getByLabelText(
      "Unbestimmtes abweichendes Inkrafttretedatum Dropdown",
    )

    await userEvent.selectOptions(dropdown, UndefinedDate.UNDEFINED_UNKNOWN)
    await rerender({ modelValue })
    expect(dropdown).toHaveValue(UndefinedDate.UNDEFINED_UNKNOWN)

    const checkBoxInputs = screen.getAllByRole("checkbox")
    await user.click(checkBoxInputs[0])
    await rerender({ modelValue })
    await user.click(checkBoxInputs[1])
    await rerender({ modelValue })
    await user.click(checkBoxInputs[2])
    await rerender({ modelValue })

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
