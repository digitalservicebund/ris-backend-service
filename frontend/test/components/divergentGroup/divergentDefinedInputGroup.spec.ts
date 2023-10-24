import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createPinia, setActivePinia } from "pinia"
import DivergentDefinedInputGroup from "@/components/divergentGroup/DivergentDefinedInputGroup.vue"
import { Metadata, MetadataSectionName, NormCategory } from "@/domain/norm"

type DivergentDefinedInputGroupProps = InstanceType<
  typeof DivergentDefinedInputGroup
>["$props"]

function renderComponent(props?: DivergentDefinedInputGroupProps) {
  const effectiveProps = {
    modelValue: props?.modelValue ?? {},
    "onUpdate:modelValue": props?.["onUpdate:modelValue"],
    id: props?.id,
    label: props?.label,
    sectionName: props?.sectionName,
  }
  return render(DivergentDefinedInputGroup, { props: effectiveProps })
}

function getControls() {
  const dateInput = screen.getByLabelText(
    "Bestimmtes abweichendes Außerkrafttretedatum Date Input",
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

  return {
    dateInput,
    amendmentNormCheckBox,
    baseNormCheckBox,
    transitionalNormCheckBox,
  }
}

describe("DivergentExpirationDefinedInputGroup", () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
  })

  it("renders all inputs and correct model value entry", () => {
    renderComponent({
      id: "divergentExpirationDefinedInputGroup",
      label: "Bestimmtes abweichendes Außerkrafttretedatum",
      sectionName: MetadataSectionName.DIVERGENT_EXPIRATION_DEFINED,
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
    expect(dateInput).toHaveValue("01.01.2023")
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
      id: "divergentExpirationDefinedInputGroup",
      label: "Bestimmtes abweichendes Außerkrafttretedatum",
      sectionName: MetadataSectionName.DIVERGENT_EXPIRATION_DEFINED,
    })

    const {
      dateInput,
      amendmentNormCheckBox,
      baseNormCheckBox,
      transitionalNormCheckBox,
    } = getControls()

    await userEvent.type(dateInput, "12.05.2020")
    await rerender({ modelValue })
    await userEvent.tab()

    await userEvent.type(dateInput, "12.05.2020")
    await rerender({ modelValue })
    await userEvent.tab()

    await user.click(amendmentNormCheckBox)
    await rerender({ modelValue })
    await user.click(baseNormCheckBox)
    await rerender({ modelValue })
    await user.click(transitionalNormCheckBox)
    await rerender({ modelValue })

    expect(modelValue).toEqual({
      DATE: ["2020-05-12"],
      NORM_CATEGORY: [
        NormCategory.AMENDMENT_NORM,
        NormCategory.BASE_NORM,
        NormCategory.TRANSITIONAL_NORM,
      ],
    })
  })

  it("should change the modelvalue when clearing the input", async () => {
    const user = userEvent.setup()
    let modelValue: Metadata = {
      DATE: ["2020-05-12"],
      NORM_CATEGORY: [
        NormCategory.AMENDMENT_NORM,
        NormCategory.BASE_NORM,
        NormCategory.TRANSITIONAL_NORM,
      ],
    }
    const onUpdateModelValue = vi.fn().mockImplementation((value) => {
      modelValue = value
    })

    const { rerender } = renderComponent({
      modelValue,
      "onUpdate:modelValue": onUpdateModelValue,
      id: "divergentExpirationDefinedInputGroup",
      label: "Bestimmtes abweichendes Außerkrafttretedatum",
      sectionName: MetadataSectionName.DIVERGENT_EXPIRATION_DEFINED,
    })

    const {
      dateInput,
      amendmentNormCheckBox,
      baseNormCheckBox,
      transitionalNormCheckBox,
    } = getControls()

    expect(dateInput).toHaveValue("12.05.2020")
    await userEvent.clear(dateInput)
    await rerender({ modelValue })
    expect(modelValue.DATE).toBeUndefined()

    expect(amendmentNormCheckBox).toBeChecked()
    await user.click(amendmentNormCheckBox)
    await rerender({ modelValue })
    expect(modelValue.NORM_CATEGORY).not.toContain(NormCategory.AMENDMENT_NORM)

    expect(baseNormCheckBox).toBeChecked()
    await user.click(baseNormCheckBox)
    await rerender({ modelValue })
    expect(modelValue.NORM_CATEGORY).not.toContain(NormCategory.BASE_NORM)

    expect(transitionalNormCheckBox).toBeChecked()
    await user.click(transitionalNormCheckBox)
    await rerender({ modelValue })
    expect(modelValue.NORM_CATEGORY).not.toContain(
      NormCategory.TRANSITIONAL_NORM,
    )
  })
})
