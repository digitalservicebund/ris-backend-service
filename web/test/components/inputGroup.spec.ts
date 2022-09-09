import userEvent from "@testing-library/user-event"
import { render } from "@testing-library/vue"
import { createVuetify } from "vuetify"
import InputGroup from "@/components/InputGroup.vue"
import { generateString } from "~/test-helper/dataGenerators"

interface Field {
  id: string
  label: string
  ariaLabel: string
  iconName: string
  requiredText?: string
}

function renderComponent(options?: {
  fields?: Field[]
  modelValue?: Record<string, string>
}) {
  const vuetify = createVuetify()
  const global = { plugins: [vuetify] }
  const props = {
    fields: options?.fields ?? [],
    modelValue: options?.modelValue ?? {},
  }
  const renderResult = render(InputGroup, { global, props })
  const user = userEvent.setup()
  return { user, ...renderResult }
}

function generateField(partialField: Partial<Field> = {}): Field {
  return {
    id: generateString({ prefix: "id-" }),
    label: generateString({ prefix: "Label " }),
    ariaLabel: generateString({ prefix: "Aria Label " }),
    iconName: generateString({ prefix: "icon-" }),
    ...partialField,
  }
}

describe("InputFieldGroup", () => {
  it("renders an input for each defined field", () => {
    const fields = [
      generateField({ ariaLabel: "Foo Label" }),
      generateField({ ariaLabel: "Bar Label" }),
    ]
    const { queryByLabelText } = renderComponent({ fields })

    const fooInput = queryByLabelText("Foo Label")
    const barInput = queryByLabelText("Bar Label")

    expect(fooInput).toBeInTheDocument()
    expect(barInput).toBeInTheDocument()
  })

  it("renders an input for each defined field with required text", () => {
    const fields = [
      generateField({ ariaLabel: "Foo Label", requiredText: "*" }),
    ]
    const { queryByLabelText } = renderComponent({ fields })

    const labelText =
      queryByLabelText("Foo Label")?.parentElement?.firstElementChild

    expect(labelText).toBeInTheDocument()
    expect(labelText?.textContent).toMatch(/^ Label.*\*$/)
  })

  it("shows input fields in correct order", () => {
    const fields = [
      generateField({ ariaLabel: "Foo Label" }),
      generateField({ ariaLabel: "Bar Label" }),
      generateField({ ariaLabel: "Baz Label" }),
    ]
    const { queryAllByRole } = renderComponent({ fields })

    const inputs = queryAllByRole("textbox")

    expect(inputs[0]).toContainHTML("Foo Label")
    expect(inputs[1]).toContainHTML("Bar Label")
    expect(inputs[2]).toContainHTML("Baz Label")
  })

  it("shows the correct model value entry in the associated input", () => {
    const fields = [
      generateField({ id: "foo", ariaLabel: "Foo Label" }),
      generateField({ id: "bar", ariaLabel: "Bar Label" }),
    ]
    const modelValue = { foo: "foo value", bar: "bar value" }
    const { getByLabelText } = renderComponent({ fields, modelValue })

    const fooInput = getByLabelText("Foo Label")
    const barInput = getByLabelText("Bar Label")

    expect(fooInput).toHaveValue("foo value")
    expect(barInput).toHaveValue("bar value")
  })

  it("shows empty value for inputs with missing model value entry", () => {
    const fields = [generateField({ id: "foo" })]
    const modelValue = {}
    const { getByRole } = renderComponent({ fields, modelValue })

    const input = getByRole("textbox")

    expect(input).toHaveValue("")
  })

  it("emits a model update event when user types into input", async () => {
    const fields = [generateField({ id: "foo" })]
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
    const fields = [generateField({ id: "foo" })]
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
