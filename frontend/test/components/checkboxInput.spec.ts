import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import CheckboxInput from "@/shared/components/input/CheckboxInput.vue"

type CheckboxInputProps = InstanceType<typeof CheckboxInput>["$props"]

function renderComponent(
  props?: Partial<CheckboxInputProps>,
  attrs?: Record<string, unknown>,
) {
  let modelValue = props?.modelValue ?? false

  const effectiveProps: CheckboxInputProps = {
    modelValue,
    "onUpdate:modelValue":
      props?.["onUpdate:modelValue"] ??
      ((value) => (modelValue = value ?? false)),
    size: props?.size,
    validationError: props?.validationError,
  }

  return render(CheckboxInput, { props: effectiveProps, attrs })
}

describe("Checkbox Input", () => {
  it("renders a checkbox", () => {
    renderComponent()
    const input = screen.getByRole("checkbox")
    expect(input).toBeInTheDocument()
  })

  it("renders the ID", () => {
    renderComponent(undefined, { id: "test-id" })
    const input = screen.getByRole("checkbox")
    expect(input).toHaveAttribute("id", "test-id")
  })

  it("renders an aria label", () => {
    renderComponent(undefined, { "aria-label": "test-label" })
    const input = screen.getByLabelText("test-label")
    expect(input).toBeInTheDocument()
  })

  it("renders the the checkbox as checked", () => {
    renderComponent({ modelValue: true })
    const input = screen.getByRole("checkbox")
    expect(input).toBeChecked()
  })

  it("renders the the checkbox as unchecked", () => {
    renderComponent({ modelValue: false })
    const input = screen.getByRole("checkbox")
    expect(input).not.toBeChecked()
  })

  it("toggles the checkbox on click", async () => {
    const user = userEvent.setup()
    const { emitted } = renderComponent({ modelValue: false })

    const input = screen.getByRole("checkbox") as HTMLInputElement
    await user.click(input)
    await user.click(input)

    expect(emitted("update:modelValue")).toEqual([[true], [false]])
  })

  it("shows validation errors", () => {
    renderComponent({
      validationError: { defaultMessage: "test-error", field: "test-field" },
    })

    const input = screen.getByRole("checkbox")
    expect(input).toHaveClass("has-error")
  })

  it("renders the regular variant by default", () => {
    renderComponent()
    const input = screen.getByRole("checkbox")
    expect(input).not.toHaveClass("ds-checkbox-small")
  })

  it("renders the regular variant when specified", () => {
    renderComponent({ size: "regular" })
    const input = screen.getByRole("checkbox")
    expect(input).not.toHaveClass("ds-checkbox-small")
  })

  it("renders the small variant when specified", () => {
    renderComponent({ size: "small" })
    const input = screen.getByRole("checkbox")
    expect(input).toHaveClass("ds-checkbox-small")
  })

  it("renders the checkbox as disabled", () => {
    renderComponent(undefined, { disabled: true })
    const input = screen.getByRole("checkbox")
    expect(input).toBeDisabled()
  })

  it("renders the checkbox as enabled", () => {
    renderComponent(undefined, { disabled: false })
    const input = screen.getByRole("checkbox")
    expect(input).toBeEnabled()
  })
})
