import userEvent from "@testing-library/user-event"
import { render } from "@testing-library/vue"
import InputGroup from "@/components/InputGroup.vue"
import type { InputField } from "@/domain"
import { generateTextInputField } from "~/test-helper/dataGenerators"

function renderComponent(options?: {
  fields?: InputField[]
  modelValue?: Record<string, string>
}) {
  const props = {
    fields: options?.fields ?? [],
    modelValue: options?.modelValue ?? {},
  }
  const renderResult = render(InputGroup, { props })
  const user = userEvent.setup()
  return { user, ...renderResult }
}

describe("InputFieldGroup", () => {
  it("renders an input for each defined field", () => {
    const fields = [
      generateTextInputField({ inputAttributes: { ariaLabel: "Foo Label" } }),
      generateTextInputField({ inputAttributes: { ariaLabel: "Bar Label" } }),
    ]
    const { queryByLabelText } = renderComponent({ fields })

    const fooInput = queryByLabelText("Foo Label")
    const barInput = queryByLabelText("Bar Label")

    expect(fooInput).toBeInTheDocument()
    expect(barInput).toBeInTheDocument()
  })

  it("shows input fields in correct order", () => {
    const fields = [
      generateTextInputField({ inputAttributes: { ariaLabel: "Foo Label" } }),
      generateTextInputField({ inputAttributes: { ariaLabel: "Bar Label" } }),
      generateTextInputField({ inputAttributes: { ariaLabel: "Baz Label" } }),
    ]
    const { queryAllByRole } = renderComponent({ fields })

    const inputs = queryAllByRole("textbox")

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
    const { queryByDisplayValue } = renderComponent({ fields, modelValue })

    const fooInput = queryByDisplayValue("foo value")
    const barInput = queryByDisplayValue("bar value")

    expect(fooInput).toBeInTheDocument()
    expect(barInput).toBeInTheDocument()
  })

  it("shows empty value for inputs with missing model value entry", () => {
    const fields = [generateTextInputField({ name: "foo" })]
    const modelValue = {}
    const { getByRole } = renderComponent({ fields, modelValue })

    const input = getByRole("textbox")

    expect(input).toHaveValue("")
  })

  it("emits a model update event when user types into input", async () => {
    const fields = [generateTextInputField({ name: "foo" })]
    const modelValue = { foo: "ab" }
    const { emitted, user, getByRole } = renderComponent({
      fields,
      modelValue,
    })

    const input = getByRole("textbox")
    await user.type(input, "c")

    expect(emitted()["update:modelValue"]).toHaveLength(1)
    expect(emitted()["update:modelValue"]).toEqual([[{ foo: "abc" }]])
  })

  it("emits a model update event also for inputs without mode value entry", async () => {
    const fields = [generateTextInputField({ name: "foo" })]
    const modelValue = {}
    const { emitted, user, getByRole } = renderComponent({
      fields,
      modelValue,
    })

    const input = getByRole("textbox")
    await user.type(input, "a")

    expect(emitted()["update:modelValue"]).toHaveLength(1)
    expect(emitted()["update:modelValue"]).toEqual([[{ foo: "a" }]])
  })

  // TODO: How to test column count property? (maybe snapshot tests)
})
