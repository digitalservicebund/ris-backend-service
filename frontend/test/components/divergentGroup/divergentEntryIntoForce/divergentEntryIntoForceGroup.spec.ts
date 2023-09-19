import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createPinia, setActivePinia } from "pinia"
import DivergentEntryIntoForceGroup from "@/components/divergentGroup/divergentEntryIntoForce/DivergentEntryIntoForceGroup.vue"
import {
  MetadataSectionName,
  MetadataSections,
  MetadatumType,
  NormCategory,
} from "@/domain/norm"

type DivergentEntryIntoForceGroupProps = InstanceType<
  typeof DivergentEntryIntoForceGroup
>["$props"]

function renderComponent(props?: Partial<DivergentEntryIntoForceGroupProps>) {
  const effectiveProps: DivergentEntryIntoForceGroupProps = {
    modelValue: props?.modelValue ?? {},
    "onUpdate:modelValue": props?.["onUpdate:modelValue"],
  }

  return render(DivergentEntryIntoForceGroup, { props: effectiveProps })
}

describe("DivergentEntryIntoForceGroup", () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
  })

  it("should render the component with 2 radio buttons each for different sections ", () => {
    renderComponent()

    const definedRadio = screen.getByLabelText("bestimmt")
    expect(definedRadio).toBeInTheDocument()
    expect(definedRadio).toBeVisible()

    const undefinedRadio = screen.getByLabelText("unbestimmt")
    expect(undefinedRadio).toBeInTheDocument()
    expect(undefinedRadio).toBeVisible()
  })

  it("renders the correct child component when a radio button is selected ", async () => {
    const user = userEvent.setup()

    let modelValue = {}
    const updateModelValue = vi.fn().mockImplementation((value) => {
      modelValue = value
    })

    const { rerender } = renderComponent({
      modelValue,
      "onUpdate:modelValue": updateModelValue,
    })

    const definedRadio = screen.getByLabelText("bestimmt")
    const undefinedRadio = screen.getByLabelText("unbestimmt")

    expect(definedRadio).toBeChecked()
    expect(undefinedRadio).not.toBeChecked()

    await user.click(undefinedRadio)
    await rerender({ modelValue })
    expect(undefinedRadio).toBeChecked()
    expect(definedRadio).not.toBeChecked()

    const dropDownInputField = screen.getByLabelText(
      "Unbestimmtes abweichendes Inkrafttretedatum Dropdown",
    )

    expect(dropDownInputField).toBeInTheDocument()
    expect(dropDownInputField).toBeVisible()
  })

  it("restores the original data after switching types", async () => {
    const user = userEvent.setup()

    let modelValue: MetadataSections = {
      DIVERGENT_ENTRY_INTO_FORCE_DEFINED: [
        {
          DATE: ["2023-01-01"],
          NORM_CATEGORY: [NormCategory.AMENDMENT_NORM],
        },
      ],
    }

    const updateModelValue = vi.fn().mockImplementation((value) => {
      modelValue = value
    })

    const { rerender, emitted } = renderComponent({
      modelValue,
      "onUpdate:modelValue": updateModelValue,
    })

    const undefinedRadio = screen.getByLabelText("unbestimmt")
    await user.click(undefinedRadio)
    await rerender({ modelValue })
    expect(emitted("update:modelValue")[0]).toEqual([
      {
        DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED: [{}],
      },
    ])

    const definedRadio = screen.getByLabelText("bestimmt")
    await user.click(definedRadio)
    await rerender({ modelValue })
    expect(emitted("update:modelValue")[1]).toEqual([
      {
        DIVERGENT_ENTRY_INTO_FORCE_DEFINED: [
          {
            DATE: ["2023-01-01"],
            NORM_CATEGORY: [NormCategory.AMENDMENT_NORM],
          },
        ],
      },
    ])
  })

  it("initialises with the correct child section based on the modelvalue prop", () => {
    renderComponent({
      modelValue: {
        [MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_DEFINED]: [
          {
            [MetadatumType.DATE]: ["2020-05-12"],
          },
        ],
      },
    })

    const definedRadio = screen.getByLabelText("bestimmt")
    expect(definedRadio).toBeChecked()

    const definedDate = screen.getByLabelText(
      "Bestimmtes abweichendes Inkrafttretedatum Date Input",
    )
    expect(definedDate).toBeVisible()
    expect(definedDate).toHaveValue("12.05.2020")
  })

  it("should by default render the  DivergentEntryIntoForceInputGroup if modelValue is empty", () => {
    renderComponent({ modelValue: {} })

    const definedRadio = screen.getByLabelText("bestimmt")

    expect(definedRadio).toBeChecked()

    const definedDate = screen.getByLabelText(
      "Bestimmtes abweichendes Inkrafttretedatum Date Input",
    )

    expect(definedDate).toBeInTheDocument()
    expect(definedDate).toBeVisible()
  })
})
