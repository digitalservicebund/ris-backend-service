import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import LeadInputGroup from "@/components/LeadInputGroup.vue"
import { Metadata } from "@/domain/Norm"

function renderComponent(options?: { modelValue?: Metadata }) {
  const props = {
    modelValue: options?.modelValue ?? {},
  }

  return render(LeadInputGroup, { props })
}

function getControls() {
  const jurisdictionInput = screen.queryByRole("textbox", {
    name: "Ressort",
  }) as HTMLInputElement

  const unitInput = screen.queryByRole("textbox", {
    name: "Organisationseinheit",
  }) as HTMLInputElement

  return { jurisdictionInput, unitInput }
}

describe("LeadInputGroup", () => {
  it("renders an input field for the jurisdiction value", async () => {
    renderComponent({ modelValue: { LEAD_JURISDICTION: ["test value"] } })

    const { jurisdictionInput } = getControls()
    expect(jurisdictionInput).toBeVisible()
    expect(jurisdictionInput).toHaveValue("test value")
  })

  it("renders an input field for the unit value", async () => {
    renderComponent({ modelValue: { LEAD_UNIT: ["test value"] } })

    const { unitInput } = getControls()
    expect(unitInput).toBeVisible()
    expect(unitInput).toHaveValue("test value")
  })

  it("updates the model value when user types into the input fields", async () => {
    const user = userEvent.setup()
    const modelValue = {}
    renderComponent({ modelValue })

    const { jurisdictionInput, unitInput } = getControls()
    await user.type(jurisdictionInput, "foo")
    await user.type(unitInput, "bar")

    expect(modelValue).toEqual({
      LEAD_JURISDICTION: ["foo"],
      LEAD_UNIT: ["bar"],
    })
  })

  it("updates the model value when user clears the input fields", async () => {
    const user = userEvent.setup()
    const modelValue: Metadata = {
      LEAD_JURISDICTION: ["foo"],
      LEAD_UNIT: ["bar"],
    }
    renderComponent({ modelValue })

    const { jurisdictionInput, unitInput } = getControls()

    expect(jurisdictionInput).toHaveValue("foo")
    await user.clear(jurisdictionInput)
    expect(modelValue.LEAD_JURISDICTION).toBeUndefined()

    expect(unitInput).toHaveValue("bar")
    await user.clear(unitInput)
    expect(modelValue.LEAD_UNIT).toBeUndefined()
  })
})
