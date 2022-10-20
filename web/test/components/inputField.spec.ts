import { render } from "@testing-library/vue"
import InputField from "@/components/InputField.vue"

function renderComponent(options?: {
  id?: string
  label?: string
  slot?: string
  errorMessage?: string
  required?: true
}) {
  const id = options?.id ?? "identifier"
  const slots = { default: options?.slot ?? `<input id="${id}" />` }
  const props = {
    id,
    label: options?.label ?? "label",
    required: options?.required ?? options?.required,
    errorMessage: options?.errorMessage,
  }

  return render(InputField, { slots, props })
}

describe("InputField", () => {
  it("shows input with given label", () => {
    const { queryByLabelText } = renderComponent({ label: "test label" })

    const input = queryByLabelText("test label", { exact: false })

    expect(input).toBeInTheDocument()
  })

  it("shows input with given label and required text", () => {
    const { queryByLabelText } = renderComponent({
      label: "test label",
      required: true,
    })

    const input = queryByLabelText("test label *", { exact: false })
    expect(input).toBeInTheDocument()
  })

  it("shows input with given error message", () => {
    const { queryByText } = renderComponent({ errorMessage: "error message" })

    const icon = queryByText("error message")

    expect(icon).toBeInTheDocument()
  })

  it("injects given input element into slot", () => {
    const { container } = renderComponent({
      slot: "<template v-slot='slotProps'><input v-bind='slotProps' type='radio' /></template>",
      id: "test-identifier",
      label: "test label",
    })

    const input = container.getElementsByTagName("input")[0] as HTMLInputElement
    expect(input).toBeInTheDocument()
    expect(input?.type).toBe("radio")
  })
})
