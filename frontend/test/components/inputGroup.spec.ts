import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import InputGroup from "@/shared/components/input/InputGroup.vue"
import { InputField } from "@/shared/components/input/types"
import { generateTextInputField } from "~/test-helper/dataGenerators"

function renderComponent(options?: {
  fields?: InputField[]
  modelValue?: Record<string, string>
}) {
  const props = {
    fields: options?.fields ?? [],
    modelValue: options?.modelValue ?? {},
  }
  const utils = render(InputGroup, { props })
  const user = userEvent.setup()
  return { user, ...utils }
}

describe("InputFieldGroup", () => {
  it("renders an input for each defined field", () => {
    const fields = [
      generateTextInputField({ inputAttributes: { ariaLabel: "Foo Label" } }),
      generateTextInputField({ inputAttributes: { ariaLabel: "Bar Label" } }),
    ]
    renderComponent({ fields })

    const fooInput = screen.queryByLabelText("Foo Label")
    const barInput = screen.queryByLabelText("Bar Label")

    expect(fooInput).toBeInTheDocument()
    expect(barInput).toBeInTheDocument()
  })

  it("shows input fields in correct order", () => {
    const fields = [
      generateTextInputField({ inputAttributes: { ariaLabel: "Foo Label" } }),
      generateTextInputField({ inputAttributes: { ariaLabel: "Bar Label" } }),
      generateTextInputField({ inputAttributes: { ariaLabel: "Baz Label" } }),
    ]
    renderComponent({ fields })

    const inputs = screen.queryAllByRole("textbox")

    expect(inputs[0]).toContainHTML("Foo Label")
    expect(inputs[1]).toContainHTML("Bar Label")
    expect(inputs[2]).toContainHTML("Baz Label")
  })

  it("shows the correct model value entry in the associated input", () => {
    const fields = [
      generateTextInputField({ name: "foo" }),
      generateTextInputField({ name: "bar" }),
    ]
    const modelValue = { foo: "foo value", bar: "bar value" }
    renderComponent({ fields, modelValue })

    const fooInput = screen.queryByDisplayValue("foo value")
    const barInput = screen.queryByDisplayValue("bar value")

    expect(fooInput).toBeInTheDocument()
    expect(barInput).toBeInTheDocument()
  })

  it("shows empty value for inputs with missing model value entry", () => {
    const fields = [generateTextInputField({ name: "foo" })]
    const modelValue = {}
    renderComponent({ fields, modelValue })

    const input = screen.getByRole("textbox")

    expect(input).toHaveValue("")
  })

  it("emits update model value event when input value changes", async () => {
    const fields = [generateTextInputField({ name: "foo" })]
    const modelValue = { foo: "ab" }
    const { emitted, user } = renderComponent({
      fields,
      modelValue,
    })

    const input = screen.getByRole("textbox")
    await user.type(input, "c")

    expect(emitted()["update:modelValue"]).toHaveLength(1)
    expect(emitted()["update:modelValue"]).toEqual([[{ foo: "abc" }]])
  })

  it("emits a model update event also for inputs without model value entry", async () => {
    const fields = [generateTextInputField({ name: "foo" })]
    const modelValue = {}
    const { emitted, user } = renderComponent({
      fields,
      modelValue,
    })

    const input = screen.getByRole("textbox")
    await user.type(input, "a")

    expect(emitted()["update:modelValue"]).toHaveLength(1)
    expect(emitted()["update:modelValue"]).toEqual([[{ foo: "a" }]])
  })
})
