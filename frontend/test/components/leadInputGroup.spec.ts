import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import LeadInputGroup from "@/components/LeadInputGroup.vue"
import { Metadata, MetadatumType } from "@/domain/Norm"

function renderComponent(options?: { modelValue?: Metadata }) {
  const props = {
    modelValue: options?.modelValue ?? {},
  }

  return render(LeadInputGroup, { props })
}

describe("LeadInputGroup", () => {
  it("renders an input field for the jurisdiction value", async () => {
    renderComponent({
      modelValue: { [MetadatumType.LEAD_JURISDICTION]: ["test value"] },
    })

    const input = screen.queryByRole("textbox", {
      name: "Ressort",
    }) as HTMLInputElement

    expect(input).toBeVisible()
    expect(input).toHaveValue("test value")
  })

  it("renders an input field for the unit value", async () => {
    renderComponent({
      modelValue: { [MetadatumType.LEAD_UNIT]: ["test value"] },
    })

    const input = screen.queryByRole("textbox", {
      name: "Organisationseinheit",
    }) as HTMLInputElement

    expect(input).toBeVisible()
    expect(input).toHaveValue("test value")
  })

  it("updates the model value when user types into the input fields", async () => {
    const user = userEvent.setup()
    const modelValue = {}
    renderComponent({ modelValue })

    const jurisdictionInput = screen.queryByRole("textbox", {
      name: "Ressort",
    }) as HTMLInputElement

    const unitInput = screen.queryByRole("textbox", {
      name: "Organisationseinheit",
    }) as HTMLInputElement

    await user.type(jurisdictionInput, "foo")
    await user.type(unitInput, "bar")

    expect(modelValue).toEqual({
      [MetadatumType.LEAD_JURISDICTION]: ["foo"],
      [MetadatumType.LEAD_UNIT]: ["bar"],
    })
  })
})
