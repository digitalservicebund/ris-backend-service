import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import CheckboxInput from "@/shared/components/input/CheckboxInput.vue"

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
  const utils = render(CheckboxInput, { props })
  return { user, props, ...utils }
}

describe("Checkbox Input", () => {
  it("shows an Checkbox input element", () => {
    renderComponent()
    const input: HTMLInputElement | null = screen.queryByRole("checkbox")

    expect(input).toBeInTheDocument()
    expect(input?.type).toBe("checkbox")
  })

  it("shows Checkbox Input with an aria label", () => {
    renderComponent({
      ariaLabel: "test-label",
    })
    const input = screen.queryByLabelText("test-label")

    expect(input).toBeInTheDocument()
  })
})
