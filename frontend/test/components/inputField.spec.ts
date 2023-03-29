import { render, screen } from "@testing-library/vue"
import InputField, {
  LabelPosition,
} from "@/shared/components/input/InputField.vue"

function renderComponent(options?: {
  id?: string
  label?: string
  slot?: string
  errorMessage?: string
  required?: true
  labelPosition?: LabelPosition
}) {
  const id = options?.id ?? "identifier"
  const slots = { default: options?.slot ?? `<input id="${id}" />` }
  const props = {
    id,
    label: options?.label,
    required: options?.required ?? options?.required,
    errorMessage: options?.errorMessage,
    labelPosition: options?.labelPosition,
  }

  return render(InputField, { slots, props })
}

describe("InputField", () => {
  it("shows input with given label", () => {
    renderComponent({ label: "test label" })

    const input = screen.queryByLabelText("test label", { exact: false })

    expect(input).toBeInTheDocument()
  })

  it("shows input with given label and required text", () => {
    renderComponent({
      label: "test label",
      required: true,
    })

    const input = screen.queryByLabelText("test label *", { exact: false })
    expect(input).toBeInTheDocument()
  })

  it("shows input with given error message", () => {
    renderComponent({ errorMessage: "error message" })

    const icon = screen.queryByText("error message")

    expect(icon).toBeInTheDocument()
  })

  it("injects given input element into slot", () => {
    renderComponent({
      slot: "<template v-slot='slotProps'><input aria-label='test-input' v-bind='slotProps' type='radio' /></template>",
      id: "test-identifier",
      label: "test label",
    })

    const input = screen.getByLabelText("test-input") as HTMLInputElement

    expect(input).toBeInTheDocument()
    expect(input?.type).toBe("radio")
  })

  it("does not render label if not given", () => {
    renderComponent({
      id: "test",
    })
    expect(screen.queryByLabelText("test")).not.toBeInTheDocument
  })

  it("shows label after the input field", () => {
    renderComponent({ label: "test label", labelPosition: LabelPosition.RIGHT })

    const input = screen.queryByLabelText("test label", {
      exact: false,
    }) as HTMLInputElement
    expect(input).toBeInTheDocument()
    const label = screen.queryByText("test label", {
      exact: false,
    }) as HTMLLabelElement
    expect(label).toBeInTheDocument()

    expect(input.compareDocumentPosition(label)).toBe(
      Node.DOCUMENT_POSITION_FOLLOWING
    )
  })
})
