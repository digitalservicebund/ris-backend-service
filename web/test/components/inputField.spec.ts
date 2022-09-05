import { render } from "@testing-library/vue"
import { createVuetify } from "vuetify"
import InputField from "@/components/InputField.vue"

function renderComponent(options?: {
  id?: string
  label?: string
  iconName?: string
  slot?: string
}) {
  const id = options?.id ?? "identifier"
  const vuetify = createVuetify()
  const global = { plugins: [vuetify] }
  const slots = { default: options?.slot ?? `<input id="${id}" />` }
  const props = {
    id,
    label: options?.label ?? "label",
    iconName: options?.iconName ?? "icon-name",
  }

  return render(InputField, { global, slots, props })
}

describe("InputField", () => {
  it("shows input with given label", () => {
    const { queryByLabelText } = renderComponent({ label: "test label" })

    const input = queryByLabelText("test label")

    expect(input).not.toBeNull()
  })

  it("shows input with given icon", () => {
    const { queryByTestId } = renderComponent({ iconName: "test-icon" })

    const icon = queryByTestId("icon")

    expect(icon).not.toBeNull()
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

    expect(input).not.toBeNull()
    expect(input?.type).toBe("radio")
  })
})
