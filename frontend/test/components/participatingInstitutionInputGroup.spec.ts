import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import ParticipatingInstitutionsInputGroup from "@/components/ParticipatingInstitutionInputGroup.vue"
import { Metadata, MetadatumType } from "@/domain/Norm"

function renderComponent(options?: { modelValue?: Metadata }) {
  const props = {
    modelValue: options?.modelValue ?? {},
  }

  return render(ParticipatingInstitutionsInputGroup, { props })
}

describe("ParticipatingInstitutionsInputGroup", () => {
  it("renders an input field for the type value", async () => {
    renderComponent({
      modelValue: { [MetadatumType.PARTICIPATION_TYPE]: ["test value"] },
    })

    const input = screen.queryByRole("textbox", {
      name: "Art der Mitwirkung",
    }) as HTMLInputElement

    expect(input).toBeVisible()
    expect(input).toHaveValue("test value")
  })

  it("renders an input field for the institution value", async () => {
    renderComponent({
      modelValue: { [MetadatumType.PARTICIPATION_INSTITUTION]: ["test value"] },
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
    const modelValue = {}
    renderComponent({ modelValue })

    const typeInput = screen.queryByRole("textbox", {
      name: "Art der Mitwirkung",
    }) as HTMLInputElement

    const institutionInput = screen.queryByRole("textbox", {
      name: "Mitwirkendes Organ",
    }) as HTMLInputElement

    await user.type(typeInput, "foo")
    await user.type(institutionInput, "bar")

    expect(modelValue).toEqual({
      [MetadatumType.PARTICIPATION_TYPE]: ["foo"],
      [MetadatumType.PARTICIPATION_INSTITUTION]: ["bar"],
    })
  })
})
