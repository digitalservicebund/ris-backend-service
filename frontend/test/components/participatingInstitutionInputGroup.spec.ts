import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import ParticipatingInstitutionsInputGroup from "@/components/ParticipatingInstitutionInputGroup.vue"
import { Metadata } from "@/domain/Norm"

function renderComponent(options?: { modelValue?: Metadata }) {
  const props = {
    modelValue: options?.modelValue ?? {},
  }

  return render(ParticipatingInstitutionsInputGroup, { props })
}

function getControls() {
  const typeInput = screen.queryByRole("textbox", {
    name: "Art der Mitwirkung",
  }) as HTMLInputElement

  const institutionInput = screen.queryByRole("textbox", {
    name: "Mitwirkendes Organ",
  }) as HTMLInputElement

  return {
    typeInput,
    institutionInput,
  }
}

describe("ParticipatingInstitutionsInputGroup", () => {
  it("renders an input field for the type value", async () => {
    renderComponent({ modelValue: { PARTICIPATION_TYPE: ["test value"] } })

    const { typeInput } = getControls()
    expect(typeInput).toBeVisible()
    expect(typeInput).toHaveValue("test value")
  })

  it("renders an input field for the institution value", async () => {
    renderComponent({
      modelValue: { PARTICIPATION_INSTITUTION: ["test value"] },
    })

    const { institutionInput } = getControls()
    expect(institutionInput).toBeInTheDocument()
    expect(institutionInput).toBeVisible()
    expect(institutionInput).toHaveValue("test value")
  })

  it("updates the model value when user types into the input fields", async () => {
    const user = userEvent.setup()
    const modelValue = {}
    renderComponent({ modelValue })

    const { typeInput, institutionInput } = getControls()
    await user.type(typeInput, "foo")
    await user.type(institutionInput, "bar")

    expect(modelValue).toEqual({
      PARTICIPATION_TYPE: ["foo"],
      PARTICIPATION_INSTITUTION: ["bar"],
    })
  })

  it("updates the model value when user clears the input fields", async () => {
    const user = userEvent.setup()
    const modelValue: Metadata = {
      PARTICIPATION_TYPE: ["foo"],
      PARTICIPATION_INSTITUTION: ["bar"],
    }
    renderComponent({ modelValue })

    const { typeInput, institutionInput } = getControls()

    expect(typeInput).toHaveValue("foo")
    await user.clear(typeInput)
    expect(modelValue.PARTICIPATION_TYPE).toBeUndefined()

    expect(institutionInput).toHaveValue("bar")
    await user.clear(institutionInput)
    expect(modelValue.PARTICIPATION_INSTITUTION).toBeUndefined()
  })
})
