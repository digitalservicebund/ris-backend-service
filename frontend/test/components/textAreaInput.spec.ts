import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import TextAreaInput from "@/shared/components/input/TextAreaInput.vue"

type TextAreaInputProps = InstanceType<typeof TextAreaInput>["$props"]

function renderComponent(props: Partial<TextAreaInputProps>) {
  let modelValue = ""

  const defaultProps: TextAreaInputProps = {
    id: "textarea",
    modelValue: modelValue,
    "onUpdate:modelValue": (value) => (modelValue = value),
    ariaLabel: "aria-label",
    ...props,
  }

  return render(TextAreaInput, { props: defaultProps })
}

describe("TextAreaInput", () => {
  it("shows an textarea element", () => {
    renderComponent({})
    const input: HTMLTextAreaElement | null = screen.queryByRole("textbox")

    expect(input).toBeInTheDocument()
    expect(input?.tagName).toBe("TEXTAREA")
  })

  it("sets the ID of the textarea", () => {
    renderComponent({ id: "test-id" })
    const input: HTMLTextAreaElement | null = screen.queryByRole("textbox")
    expect(input).toHaveAttribute("id", "test-id")
  })

  it("shows input with an aria label", () => {
    renderComponent({ ariaLabel: "test-label" })
    const input = screen.queryByLabelText("test-label")
    expect(input).toBeInTheDocument()
  })

  it("shows input with a placeholder", () => {
    renderComponent({ placeholder: "Test Placeholder" })
    const input = screen.queryByPlaceholderText("Test Placeholder")
    expect(input).toBeInTheDocument()
  })

  it("allows to type text inside input", async () => {
    const user = userEvent.setup()
    renderComponent({ modelValue: "one" })
    const input: HTMLTextAreaElement = screen.getByRole("textbox")

    expect(input).toHaveValue("one")

    await user.type(input, " two")
    expect(input).toHaveValue("one two")
  })

  it("displays the model value", () => {
    renderComponent({ modelValue: "one" })
    const input: HTMLTextAreaElement = screen.getByRole("textbox")

    expect(input).toHaveValue("one")
  })

  it("updates the model value", async () => {
    const user = userEvent.setup()
    let testModel = "one"
    renderComponent({
      modelValue: testModel,
      "onUpdate:modelValue": (value) => (testModel = value),
    })
    const input: HTMLTextAreaElement = screen.getByRole("textbox")

    await user.type(input, " two")
    expect(testModel).toBe("one two")
  })

  it("sets the textarea to readonly", () => {
    renderComponent({ readOnly: true })
    const input: HTMLTextAreaElement = screen.getByRole("textbox")
    expect(input).toHaveAttribute("readonly")
  })

  it("sets the tabindex to -1 when readonly", () => {
    renderComponent({ readOnly: true })
    const input: HTMLTextAreaElement = screen.getByRole("textbox")
    expect(input).toHaveAttribute("tabindex", "-1")
  })

  it("doesn't set the textarea to readonly", () => {
    renderComponent({ readOnly: false })
    const input: HTMLTextAreaElement = screen.getByRole("textbox")
    expect(input).not.toHaveAttribute("readonly")
  })

  it("leaves the tabindex alone when not readonly", () => {
    renderComponent({ readOnly: false })
    const input: HTMLTextAreaElement = screen.getByRole("textbox")
    expect(input).not.toHaveAttribute("tabindex")
  })

  it("sets the tabindex to the given value", () => {
    // @ts-expect-error It's not a prop we declared but still allowed in
    // practice as a fall-thorugh attribute, so we want to test it.
    renderComponent({ readOnly: true, tabindex: 815 })
    const input: HTMLTextAreaElement = screen.getByRole("textbox")
    expect(input).toHaveAttribute("tabindex", "815")
  })

  it("renders the number of rows", () => {
    renderComponent({ rows: 5 })
    const input: HTMLTextAreaElement = screen.getByRole("textbox")
    expect(input).toHaveAttribute("rows", "5")
  })

  // Autosizing is tested in the integration tests as it requires some
  // actual rendering in order to get the height of the textarea.
})
