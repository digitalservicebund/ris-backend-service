import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import TextInput from "@/shared/components/input/TextInput.vue"

type TextInputProps = InstanceType<typeof TextInput>["$props"]

function renderComponent(props?: Partial<TextInputProps>) {
  let modelValue = props?.modelValue ?? ""

  const effectiveProps: TextInputProps = {
    id: props?.id ?? "identifier",
    value: props?.value,
    modelValue,
    "onUpdate:modelValue":
      props?.["onUpdate:modelValue"] ?? ((value) => (modelValue = value ?? "")),
    ariaLabel: props?.ariaLabel ?? "aria-label",
    placeholder: props?.placeholder,
    readOnly: props?.readOnly,
    fullHeight: props?.fullHeight,
    hasError: props?.hasError,
    size: props?.size,
  }

  return render(TextInput, { props: effectiveProps })
}

describe("TextInput", () => {
  it("shows an text input element", () => {
    renderComponent()
    const input: HTMLInputElement | null = screen.queryByRole("textbox")
    expect(input).toBeInTheDocument()
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
    const user = userEvent.setup()
    const { emitted } = renderComponent()
    const input: HTMLInputElement = screen.getByRole("textbox")

    await user.type(input, "ab")
    expect(emitted().input).toHaveLength(2)
    expect(emitted().input).toEqual([[expect.any(Event)], [expect.any(Event)]])
  })

  it("emits model update event when user types into input", async () => {
    const user = userEvent.setup()
    const { emitted } = renderComponent()
    const input: HTMLInputElement = screen.getByRole("textbox")

    await user.type(input, "a")
    expect(emitted("update:modelValue")).toEqual([["a"]])
  })

  it("renders a validation error", () => {
    renderComponent({ hasError: true })
    const input = screen.getByRole("textbox")
    expect(input).toHaveClass("has-error")
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
