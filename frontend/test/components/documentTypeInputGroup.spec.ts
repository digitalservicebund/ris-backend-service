import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import DocumentTypeInputGroup from "@/components/DocumentTypeInputGroup.vue"
import { Metadata, NormCategory } from "@/domain/Norm"

function renderComponent(options?: { modelValue?: Metadata }) {
  const props = {
    modelValue: options?.modelValue ?? {},
  }
  return render(DocumentTypeInputGroup, { props })
}

function getControls() {
  const typeNameInput = screen.queryByRole("textbox", {
    name: "Typbezeichnung",
  }) as HTMLInputElement

  const amendmentNormCheckBox = screen.getByRole("checkbox", {
    name: "Änderungsnorm",
  }) as HTMLInputElement

  const baseNormCheckBox = screen.getByRole("checkbox", {
    name: "Stammnorm",
  }) as HTMLInputElement

  const transitionalNormCheckBox = screen.getByRole("checkbox", {
    name: "Übergangsnorm",
  }) as HTMLInputElement

  const templateNameInput = screen.queryByRole("textbox", {
    name: "Bezeichnung gemäß Vorlage",
  }) as HTMLInputElement

  return {
    typeNameInput,
    amendmentNormCheckBox,
    baseNormCheckBox,
    transitionalNormCheckBox,
    templateNameInput,
  }
}

describe("DocumentTypeInputGroup", () => {
  it("should render the components with the initial state of the modelvalue", () => {
    renderComponent({
      modelValue: {
        TYPE_NAME: ["test value"],
        NORM_CATEGORY: [
          NormCategory.BASE_NORM,
          NormCategory.TRANSITIONAL_NORM,
          NormCategory.AMENDMENT_NORM,
        ],
        TEMPLATE_NAME: ["test value 1", "test value 2", "test value 3"],
      },
    })

    const {
      typeNameInput,
      amendmentNormCheckBox,
      baseNormCheckBox,
      transitionalNormCheckBox,
      templateNameInput,
    } = getControls()

    const chips = screen.getAllByRole("listitem")
    const expectedValues = ["test value 1", "test value 2", "test value 3"]
    chips.forEach((chip, index) => {
      expect(chip).toHaveTextContent(expectedValues[index])
    })

    expect(typeNameInput).toBeInTheDocument()
    expect(typeNameInput).toHaveValue("test value")

    expect(amendmentNormCheckBox).toBeChecked()
    expect(baseNormCheckBox).toBeChecked()
    expect(transitionalNormCheckBox).toBeChecked()

    expect(templateNameInput).toBeInTheDocument()
  })

  it("should change the modelvalue when update the input", async () => {
    const user = userEvent.setup()
    const modelValue = {}
    renderComponent({ modelValue })

    const {
      typeNameInput,
      amendmentNormCheckBox,
      baseNormCheckBox,
      transitionalNormCheckBox,
      templateNameInput,
    } = getControls()

    await user.type(typeNameInput, "foo")
    await user.type(templateNameInput, "bar")
    await user.type(templateNameInput, "{enter}")
    await user.type(templateNameInput, "foo")
    await user.type(templateNameInput, "{enter}")
    await user.type(templateNameInput, "baz")
    await user.type(templateNameInput, "{enter}")

    const chipList = screen.getAllByRole("listitem")
    expect(chipList.length).toBe(3)

    await user.click(amendmentNormCheckBox)
    await user.click(baseNormCheckBox)
    await user.click(transitionalNormCheckBox)

    expect(amendmentNormCheckBox).toBeChecked()
    expect(baseNormCheckBox).toBeChecked()
    expect(transitionalNormCheckBox).toBeChecked()

    expect(modelValue).toEqual({
      TYPE_NAME: ["foo"],
      TEMPLATE_NAME: ["bar", "foo", "baz"],
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
      TYPE_NAME: ["foo"],
      TEMPLATE_NAME: ["bar", "foo", "baz"],
      NORM_CATEGORY: [
        NormCategory.AMENDMENT_NORM,
        NormCategory.BASE_NORM,
        NormCategory.TRANSITIONAL_NORM,
      ],
    }
    renderComponent({ modelValue })

    const {
      typeNameInput,
      amendmentNormCheckBox,
      baseNormCheckBox,
      transitionalNormCheckBox,
    } = getControls()

    expect(typeNameInput).toHaveValue("foo")
    await user.clear(typeNameInput)
    expect(modelValue.TYPE_NAME).toBeUndefined()

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

    const chipList = screen.getAllByRole("listitem")
    expect(chipList.length).toBe(3)
    await user.click(chipList[0])
    await user.type(chipList[0], "{enter}")
    await user.click(chipList[0])
    await user.type(chipList[0], "{enter}")
    await user.click(chipList[0])
    await user.type(chipList[0], "{enter}")
    expect(modelValue.TEMPLATE_NAME).toHaveLength(0)
  })
})
