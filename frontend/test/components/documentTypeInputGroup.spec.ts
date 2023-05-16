import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import DocumentTypeInputGroup from "@/components/DocumentTypeInputGroup.vue"
import { Metadata, MetadatumType, NormCategory } from "@/domain/Norm"

function renderComponent(options?: { modelValue?: Metadata }) {
  const props = {
    modelValue: options?.modelValue ?? {},
  }
  return render(DocumentTypeInputGroup, { props })
}

describe("DocumentTypeInputGroup", () => {
  it("should render the components with the initial state of the modelvalue", () => {
    renderComponent({
      modelValue: {
        [MetadatumType.TYPE_NAME]: ["test value"],
        [MetadatumType.NORM_CATEGORY]: [
          NormCategory.BASE_NORM,
          NormCategory.TRANSITIONAL_NORM,
          NormCategory.AMENDMENT_NORM,
        ],
        [MetadatumType.TEMPLATE_NAME]: [
          "test value 1",
          "test value 2",
          "test value 3",
        ],
      },
    })

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

    const chips = screen.getAllByLabelText("chip")
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

    const textInput = screen.getAllByRole("textbox")
    await user.type(textInput[0], "foo")
    await user.type(textInput[1], "bar")
    await user.type(textInput[1], "{enter}")
    await user.type(textInput[1], "foo")
    await user.type(textInput[1], "{enter}")
    await user.type(textInput[1], "baz")
    await user.type(textInput[1], "{enter}")

    const chipList = screen.getAllByLabelText("chip")
    expect(chipList.length).toBe(3)

    const checkBoxInputs = screen.getAllByRole("checkbox")
    await user.click(checkBoxInputs[0])
    await user.click(checkBoxInputs[1])
    await user.click(checkBoxInputs[2])

    expect(checkBoxInputs[0]).toBeChecked()
    expect(checkBoxInputs[1]).toBeChecked()
    expect(checkBoxInputs[2]).toBeChecked()

    expect(modelValue).toEqual({
      [MetadatumType.TYPE_NAME]: ["foo"],
      [MetadatumType.TEMPLATE_NAME]: ["bar", "foo", "baz"],
      [MetadatumType.NORM_CATEGORY]: [
        NormCategory.AMENDMENT_NORM,
        NormCategory.BASE_NORM,
        NormCategory.TRANSITIONAL_NORM,
      ],
    })
  })
})
