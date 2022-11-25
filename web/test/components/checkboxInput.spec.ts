import userEvent from "@testing-library/user-event"
import { render } from "@testing-library/vue"
import CheckboxInput from "@/components/CheckboxInput.vue"

function renderComponent(options?: {
  ariaLabel?: string
  value?: string
  modelValue?: string
}) {
  const user = userEvent.setup()
  const props = {
    id: "identifier",
    value: options?.value,
    modelValue: options?.modelValue,
    ariaLabel: options?.ariaLabel ?? "aria-label",
  }
  const renderResult = render(CheckboxInput, { props })
  return { user, props, ...renderResult }
}

describe("Checkbox Input", () => {
  it("shows an Checkbox input element", () => {
    const { queryByRole } = renderComponent()
    const input: HTMLInputElement | null = queryByRole("checkbox")

    expect(input).toBeInTheDocument()
    expect(input?.type).toBe("checkbox")
  })

  it("shows Checkbox Input with an aria label", () => {
    const { queryByLabelText } = renderComponent({
      ariaLabel: "test-label",
    })
    const input = queryByLabelText("test-label")

    expect(input).toBeInTheDocument()
  })
})
