import { render } from "@testing-library/vue"
import { createVuetify } from "vuetify"
import InputField from "@/components/InputField.vue"

function renderComponent(options?: {
  id?: string
  label?: string
  iconName?: string
  slot?: string
  requiredText?: string
}) {
  const id = options?.id ?? "identifier"
  const vuetify = createVuetify()
  const global = { plugins: [vuetify] }
  const slots = { default: options?.slot ?? `<input id="${id}" />` }
  const props = {
    id,
    label: options?.label ?? "label",
    iconName: options?.iconName ?? "icon-name",
    requiredText: options?.requiredText ?? options?.requiredText,
  }

  return render(InputField, { global, slots, props })
}

describe("InputField", () => {
  it("shows input with given label", () => {
    const { queryByLabelText } = renderComponent({ label: "test label" })

    const input = queryByLabelText("test label")

    expect(input).toBeInTheDocument()
  })

  it("shows input with given label and required text", () => {
    const { queryByLabelText } = renderComponent({
      label: "test label",
      requiredText: "*",
    })

    const input = queryByLabelText("test label*")

    expect(input).toBeInTheDocument()
  })

  it("shows input with given icon", () => {
    const { queryByTestId } = renderComponent({ iconName: "test-icon" })

    const icon = queryByTestId("icon")

    expect(icon).toBeInTheDocument()
  })

  it("injects given input element into slot", () => {
    const { queryByLabelText } = renderComponent({
      slot: "<template v-slot='slotProps'><input v-bind='slotProps' type='radio' /></template>",
      id: "test-identifier",
      label: "test label",
    })

    const input: HTMLInputElement | null = queryByLabelText("test label", {
      selector: "input",
    })

    expect(input).toBeInTheDocument()
    expect(input?.type).toBe("radio")
  })
})
