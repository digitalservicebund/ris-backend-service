import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { defineComponent } from "vue"
import TextInput from "@/components/TextInput.vue"

type TextInputProps = InstanceType<typeof TextInput>["$props"]

function renderComponent(props?: Partial<TextInputProps>) {
  let modelValue = props?.modelValue ?? ""

  const effectiveProps: TextInputProps = {
    id: props?.id ?? "identifier",
    modelValue,
    "onUpdate:modelValue":
      props?.["onUpdate:modelValue"] ??
      ((value: string) => (modelValue = value ?? "")),
    ariaLabel: props?.ariaLabel ?? "aria-label",
    placeholder: props?.placeholder,
    readOnly: props?.readOnly,
    fullHeight: props?.fullHeight,
    hasError: props?.hasError,
    size: props?.size,
    type: props?.type,
  }

  return render(TextInput, { props: effectiveProps })
}

describe("TextInput", () => {
  it("shows an text input element", () => {
    renderComponent()
    const input: HTMLInputElement | null = screen.queryByRole("textbox")
    expect(input).toBeInTheDocument()
  })

  it("shows the value", () => {
    renderComponent({ modelValue: "test" })
    const input: HTMLInputElement = screen.getByRole("textbox")
    expect(input).toHaveValue("test")
  })

  it("sets the input type to text by default", () => {
    renderComponent()
    const input: HTMLInputElement = screen.getByRole("textbox")
    expect(input).toHaveAttribute("type", "text")
  })

  it("sets the input type to something other than text", () => {
    renderComponent({ type: "number" })
    const input: HTMLInputElement = screen.getByRole("spinbutton")
    expect(input).toHaveAttribute("type", "number")
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

  it("emits input events when user types into input", async () => {
    const handleInput = vi.fn()

    const withInputEvent = defineComponent({
      components: { TextInput },
      data: () => ({ value: "" }),
      methods: { handleInput },
      template: `
        <TextInput
          aria-label="aria-label"
          id="identifier"
          v-model="value"
          @input="handleInput"
        />`,
    })

    render(withInputEvent)
    const input: HTMLInputElement = screen.getByRole("textbox")

    await userEvent.type(input, "ab")
    expect(handleInput).toHaveBeenCalledTimes(2)
    expect(handleInput).toHaveBeenNthCalledWith(1, expect.any(InputEvent))
    expect(handleInput).toHaveBeenNthCalledWith(2, expect.any(InputEvent))
  })

  it("emits model update event when user types into input", async () => {
    const { emitted } = renderComponent()
    const input: HTMLInputElement = screen.getByRole("textbox")
    await userEvent.type(input, "a")
    expect(emitted("update:modelValue")).toEqual([["a"]])
  })

  it("renders a validation error", () => {
    renderComponent({ hasError: true })
    const input = screen.getByRole("textbox")
    expect(input).toHaveClass("has-error")
  })

  it("clears errors on type", async () => {
    const { emitted } = renderComponent({ hasError: true })
    const input: HTMLInputElement = screen.getByRole("textbox")
    await userEvent.type(input, "a")
    expect(emitted("update:validationError")).toEqual([[undefined]])
  })

  it("renders a read-only input", () => {
    renderComponent({ readOnly: true })
    const input = screen.getByRole("textbox")
    expect(input).toHaveAttribute("readonly")
  })

  it("does not rennder a read-only input", () => {
    renderComponent({ readOnly: false })
    const input = screen.getByRole("textbox")
    expect(input).not.toHaveAttribute("readonly")
  })

  it("renders the regular variant by default", () => {
    renderComponent()
    const input = screen.getByRole("textbox")
    expect(input).not.toHaveClass("ds-input-medium")
    expect(input).not.toHaveClass("ds-input-small")
  })

  it("renders the regular variant", () => {
    renderComponent({ size: "regular" })
    const input = screen.getByRole("textbox")
    expect(input).not.toHaveClass("ds-input-medium")
    expect(input).not.toHaveClass("ds-input-small")
  })

  it("renders the medium variant", () => {
    renderComponent({ size: "medium" })
    const input = screen.getByRole("textbox")
    expect(input).toHaveClass("ds-input-medium")
    expect(input).not.toHaveClass("ds-input-small")
  })

  it("renders the small variant", () => {
    renderComponent({ size: "small" })
    const input = screen.getByRole("textbox")
    expect(input).not.toHaveClass("ds-input-medium")
    expect(input).toHaveClass("ds-input-small")
  })
})
