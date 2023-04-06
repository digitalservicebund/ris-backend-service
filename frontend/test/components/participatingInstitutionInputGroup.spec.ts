import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import ParticipatingInstitutionsInputGroup from "@/components/ParticipatingInstitutionInputGroup.vue"

function renderComponent(options?: {
  modelValue?: { type?: string; institution?: string }
}) {
  const props = {
    modelValue: options?.modelValue ?? { type: "", institution: "" },
  }

  return render(ParticipatingInstitutionsInputGroup, { props })
}

describe("ParticipatingInstitutionsInputGroup", () => {
  it("renders an input field for the jurisdiction type value", async () => {
    renderComponent({
      modelValue: { type: "test value" },
    })

    const input = screen.queryByRole("textbox", {
      name: "Art der Mitwirkung",
    }) as HTMLInputElement

    expect(input).toBeVisible()
    expect(input).toHaveValue("test value")
  })

  it("renders an input field for the jurisdiction institution value", async () => {
    renderComponent({
      modelValue: { institution: "test value" },
    })

    const input = screen.queryByRole("textbox", {
      name: "Mitwirkendes Organ",
    }) as HTMLInputElement

    expect(input).toBeInTheDocument()
    expect(input).toBeVisible()
    expect(input).toHaveValue("test value")
  })

  it("updates the model value when user types into the input fields", async () => {
    const user = userEvent.setup()
    const modelValue = { type: "", institution: "" }
    renderComponent({ modelValue })

    const typeInput = screen.queryByRole("textbox", {
      name: "Art der Mitwirkung",
    }) as HTMLInputElement

    const institutionInput = screen.queryByRole("textbox", {
      name: "Mitwirkendes Organ",
    }) as HTMLInputElement

    await user.type(typeInput, "foo")
    await user.type(institutionInput, "bar")

    expect(modelValue).toEqual({ type: "foo", institution: "bar" })
  })
})
