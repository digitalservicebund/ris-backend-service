import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { vi } from "vitest"
import { defineComponent } from "vue"
import TextAreaInput from "@/shared/components/input/TextAreaInput.vue"

type TextAreaInputProps = InstanceType<typeof TextAreaInput>["$props"]

function renderComponent(
  props: Partial<TextAreaInputProps>,
  attrs?: Record<string, unknown>,
) {
  let modelValue = ""

  const defaultProps: TextAreaInputProps = {
    id: "textarea",
    modelValue: modelValue,
    "onUpdate:modelValue": (value) => (modelValue = value),
    ariaLabel: "aria-label",
    ...props,
  }

  return render(TextAreaInput, { props: defaultProps, attrs })
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

  it("shows textarea with an aria label", () => {
    renderComponent({ ariaLabel: "test-label" })
    const textarea = screen.queryByLabelText("test-label")
    expect(textarea).toBeInTheDocument()
  })

  it("shows textarea with a placeholder", () => {
    renderComponent({ placeholder: "Test Placeholder" })
    const textarea = screen.queryByPlaceholderText("Test Placeholder")
    expect(textarea).toBeInTheDocument()
  })

  it("shows textarea with an error state", () => {
    renderComponent({ hasError: true })
    const textarea = screen.queryByRole("textbox")
    expect(textarea).toHaveClass("has-error")
  })

  it("renders the 'regular' variant by default", () => {
    renderComponent({ size: "regular" })
    const textarea = screen.queryByRole("textbox")
    expect(textarea).toHaveClass("px-24")
  })

  it("renders the 'regular' variant", () => {
    renderComponent({ size: "regular" })
    const textarea = screen.queryByRole("textbox")
    expect(textarea).toHaveClass("px-24")
  })

  it("renders the 'small' variant", () => {
    renderComponent({ size: "small" })
    const textarea = screen.queryByRole("textbox")
    expect(textarea).toHaveClass("px-16")
  })

  it("renders the 'medium' variant", () => {
    renderComponent({ size: "medium" })
    const textarea = screen.queryByRole("textbox")
    expect(textarea).toHaveClass("px-20")
  })

  it("allows to type text inside textarea", async () => {
    const user = userEvent.setup()
    renderComponent({ modelValue: "one" })
    const textarea: HTMLTextAreaElement = screen.getByRole("textbox")

    expect(textarea).toHaveValue("one")

    await user.type(textarea, " two")
    expect(textarea).toHaveValue("one two")
  })

  it("displays the model value", () => {
    renderComponent({ modelValue: "one" })
    const textarea: HTMLTextAreaElement = screen.getByRole("textbox")

    expect(textarea).toHaveValue("one")
  })

  it("updates the model value", async () => {
    const user = userEvent.setup()
    let testModel = "one"
    renderComponent({
      modelValue: testModel,
      "onUpdate:modelValue": (value) => (testModel = value),
    })
    const textarea: HTMLTextAreaElement = screen.getByRole("textbox")

    await user.type(textarea, " two")
    expect(testModel).toBe("one two")
  })

  it("sets the textarea to readonly", () => {
    renderComponent({ readOnly: true })
    const textarea: HTMLTextAreaElement = screen.getByRole("textbox")
    expect(textarea).toHaveAttribute("readonly")
  })

  it("sets the tabindex to -1 when readonly", () => {
    renderComponent({ readOnly: true })
    const textarea: HTMLTextAreaElement = screen.getByRole("textbox")
    expect(textarea).toHaveAttribute("tabindex", "-1")
  })

  it("doesn't set the textarea to readonly", () => {
    renderComponent({ readOnly: false })
    const textarea: HTMLTextAreaElement = screen.getByRole("textbox")
    expect(textarea).not.toHaveAttribute("readonly")
  })

  it("leaves the tabindex alone when not readonly", () => {
    renderComponent({ readOnly: false })
    const textarea: HTMLTextAreaElement = screen.getByRole("textbox")
    expect(textarea).not.toHaveAttribute("tabindex")
  })

  it("sets the tabindex to the given value", () => {
    renderComponent({}, { tabindex: 815 })
    const textarea: HTMLTextAreaElement = screen.getByRole("textbox")
    expect(textarea).toHaveAttribute("tabindex", "815")
  })

  it("renders the number of rows", () => {
    renderComponent({ rows: 5 })
    const textarea: HTMLTextAreaElement = screen.getByRole("textbox")
    expect(textarea).toHaveAttribute("rows", "5")
  })

  it("stops propagation of the enter key event", async () => {
    const wrapperHandler = vi.fn()

    const wrapper = defineComponent({
      components: { TextAreaInput },
      data: () => ({ value: "" }),
      methods: { wrapperHandler },
      template: `
        <div @keypress.enter="wrapperHandler">
          <TextAreaInput id="textarea" v-model="value" aria-label="Test" />
        </div>
      `,
    })

    render(wrapper)

    const user = userEvent.setup()
    const textarea: HTMLTextAreaElement = screen.getByRole("textbox")
    await user.type(textarea, "{enter}")
    expect(wrapperHandler).not.toHaveBeenCalled()
  })

  // Autosizing is tested in the integration tests as it requires some
  // actual rendering in order to get the height of the textarea.
})
