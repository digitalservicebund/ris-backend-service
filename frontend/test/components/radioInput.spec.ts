import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { defineComponent } from "vue"
import RadioInput from "@/shared/components/input/RadioInput.vue"

type RadioInputProps = InstanceType<typeof RadioInput>["$props"]

function renderComponent(
  props?: Partial<RadioInputProps>,
  attrs?: Record<string, unknown>,
) {
  let modelValue = props?.modelValue ?? ""

  const effectiveProps: RadioInputProps = {
    modelValue,
    "onUpdate:modelValue":
      props?.["onUpdate:modelValue"] ??
      ((value) => (modelValue = value ?? false)),
    size: props?.size,
    validationError: props?.validationError,
  }

  return render(RadioInput, { props: effectiveProps, attrs })
}

describe("Radio Input", () => {
  it("renders a radio", () => {
    renderComponent()
    const input = screen.getByRole("radio")
    expect(input).toBeInTheDocument()
  })

  it("renders the ID", () => {
    renderComponent(undefined, { id: "test-id" })
    const input = screen.getByRole("radio")
    expect(input).toHaveAttribute("id", "test-id")
  })

  it("renders the name", () => {
    renderComponent(undefined, { name: "test-name" })
    const input = screen.getByRole("radio")
    expect(input).toHaveAttribute("name", "test-name")
  })

  it("renders the value", () => {
    renderComponent(undefined, { value: "test-value" })
    const input = screen.getByRole("radio")

    // This is a false positive, 'value' here doesn't mean the checked state
    // of the radio, but the value it has in the form when checked.
    /* eslint-disable-next-line jest-dom/prefer-to-have-value */
    expect(input).toHaveAttribute("value", "test-value")
  })

  it("renders an aria label", () => {
    renderComponent(undefined, { "aria-label": "test-label" })
    const input = screen.getByLabelText("test-label")
    expect(input).toBeInTheDocument()
  })

  it("renders the the radio as checked", () => {
    renderComponent({ modelValue: "foo" }, { value: "foo" })
    const input = screen.getByRole("radio")
    expect(input).toBeChecked()
  })

  it("renders the the radio as unchecked", () => {
    renderComponent({ modelValue: "foo" }, { value: "bar" })
    const input = screen.getByRole("radio")
    expect(input).not.toBeChecked()
  })

  it("toggles the radio on click", async () => {
    const dummyGroup = defineComponent({
      components: { RadioInput },
      data: () => ({ value: "" }),
      template: `
        <RadioInput
          :model-value="value"
          @update:model-value="$emit('update:modelValue', $event)"
          name="radio"
          value="foo"
        />
        <RadioInput
          :model-value="value"
          @update:model-value="$emit('update:modelValue', $event)"
          name="radio"
          value="bar"
        />
      `,
    })

    const user = userEvent.setup()
    const { emitted } = render(dummyGroup)

    const radios = screen.getAllByRole("radio") as HTMLInputElement[]
    await user.click(radios[0])
    await user.click(radios[1])

    expect(emitted("update:modelValue")).toEqual([["foo"], ["bar"]])
  })

  it("shows validation errors", () => {
    renderComponent({
      validationError: { defaultMessage: "test-error", field: "test-field" },
    })

    const input = screen.getByRole("radio")
    expect(input).toHaveClass("has-error")
  })

  it("renders the regular variant by default", () => {
    renderComponent()
    const input = screen.getByRole("radio")
    expect(input).not.toHaveClass("ds-radio-small")
    expect(input).not.toHaveClass("ds-radio-mini")
  })

  it("renders the regular variant when specified", () => {
    renderComponent({ size: "large" })
    const input = screen.getByRole("radio")
    expect(input).not.toHaveClass("ds-radio-small")
    expect(input).not.toHaveClass("ds-radio-mini")
  })

  it("renders the medium variant when specified", () => {
    renderComponent({ size: "medium" })
    const input = screen.getByRole("radio")
    expect(input).toHaveClass("ds-radio-small")
    expect(input).not.toHaveClass("ds-radio-mini")
  })

  it("renders the small variant when specified", () => {
    renderComponent({ size: "small" })
    const input = screen.getByRole("radio")
    expect(input).toHaveClass("ds-radio-mini")
    expect(input).not.toHaveClass("ds-radio-small")
  })

  it("renders the radio as disabled", () => {
    renderComponent(undefined, { disabled: true })
    const input = screen.getByRole("radio")
    expect(input).toBeDisabled()
  })

  it("renders the radio as enabled", () => {
    renderComponent(undefined, { disabled: false })
    const input = screen.getByRole("radio")
    expect(input).toBeEnabled()
  })
})
