import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import LeadInputGroup from "@/components/LeadInputGroup.vue"

function renderComponent(options?: {
  modelValue?: { jurisdiction?: string; unit?: string }
}) {
  const props = {
    modelValue: options?.modelValue ?? { jurisdiction: "", unit: "" },
  }

  return render(LeadInputGroup, { props })
}

describe("LeadInputGroup", () => {
  it("renders an input field for the jurisdiction value", async () => {
    renderComponent({ modelValue: { jurisdiction: "test value" } })

    const input = screen.queryByRole("textbox", {
      name: "Ressort",
    }) as HTMLInputElement

    expect(input).toBeVisible()
    expect(input).toHaveValue("test value")
  })

  it("renders an input field for the unit value", async () => {
    renderComponent({ modelValue: { unit: "test value" } })

    const input = screen.queryByRole("textbox", {
      name: "Organisationseinheit",
    }) as HTMLInputElement

    expect(input).toBeVisible()
    expect(input).toHaveValue("test value")
  })

  it("updates the model value when user types into the input fields", async () => {
    const user = userEvent.setup()
    const modelValue = { jurisdiction: "", unit: "" }
    renderComponent({ modelValue })

    const jurisdictionInput = screen.queryByRole("textbox", {
      name: "Ressort",
    }) as HTMLInputElement

    const unitInput = screen.queryByRole("textbox", {
      name: "Organisationseinheit",
    }) as HTMLInputElement

    await user.type(jurisdictionInput, "foo")
    await user.type(unitInput, "bar")
    await userEvent.tab() // Remove once text inputs are no more lazy.

    expect(modelValue).toEqual({ jurisdiction: "foo", unit: "bar" })
  })
})
