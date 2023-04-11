import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import SubjectAreaInputGroup from "@/components/SubjectAreaInputGroup.vue"
import { Metadata, MetadatumType } from "@/domain/Norm"

function renderComponent(options?: { modelValue?: Metadata }) {
  const props = {
    modelValue: options?.modelValue ?? {},
  }

  return render(SubjectAreaInputGroup, { props })
}

describe("SubjectAreaInputGroup", () => {
  it("renders an input field for the FNA value", async () => {
    renderComponent({
      modelValue: { [MetadatumType.SUBJECT_FNA]: ["test value"] },
    })

    const input = screen.queryByRole("textbox", {
      name: "FNA-Nummer",
    }) as HTMLInputElement

    expect(input).toBeVisible()
    expect(input).toHaveValue("test value")
  })
  it("renders an input field for the previousFna value", async () => {
    renderComponent({
      modelValue: { [MetadatumType.SUBJECT_PREVIOUS_FNA]: ["test value"] },
    })

    const input = screen.queryByRole("textbox", {
      name: "Frühere FNA-Nummer",
    }) as HTMLInputElement

    expect(input).toBeVisible()
    expect(input).toHaveValue("test value")
  })
  it("renders an input field for the gesta value", async () => {
    renderComponent({
      modelValue: { [MetadatumType.SUBJECT_GESTA]: ["test value"] },
    })

    const input = screen.queryByRole("textbox", {
      name: "GESTA-Nummer",
    }) as HTMLInputElement

    expect(input).toBeVisible()
    expect(input).toHaveValue("test value")
  })
  it("renders an input field for the bgb3 value", async () => {
    renderComponent({
      modelValue: { [MetadatumType.SUBJECT_BGB_3]: ["test value"] },
    })

    const input = screen.queryByRole("textbox", {
      name: "Bundesgesetzblatt Teil III",
    }) as HTMLInputElement

    expect(input).toBeVisible()
    expect(input).toHaveValue("test value")
  })

  it("updates the model value when user types into the input fields", async () => {
    const user = userEvent.setup()
    const modelValue = {}
    renderComponent({ modelValue })

    const fnaInput = screen.queryByRole("textbox", {
      name: "FNA-Nummer",
    }) as HTMLInputElement

    const previousFnaInput = screen.queryByRole("textbox", {
      name: "Frühere FNA-Nummer",
    }) as HTMLInputElement

    const gestaInput = screen.queryByRole("textbox", {
      name: "GESTA-Nummer",
    }) as HTMLInputElement

    const bgb3input = screen.queryByRole("textbox", {
      name: "Bundesgesetzblatt Teil III",
    }) as HTMLInputElement

    await user.type(fnaInput, "foo")
    await user.type(previousFnaInput, "bar")
    await user.type(gestaInput, "baz")
    await user.type(bgb3input, "ban")

    expect(modelValue).toEqual({
      [MetadatumType.SUBJECT_FNA]: ["foo"],
      [MetadatumType.SUBJECT_PREVIOUS_FNA]: ["bar"],
      [MetadatumType.SUBJECT_GESTA]: ["baz"],
      [MetadatumType.SUBJECT_BGB_3]: ["ban"],
    })
  })
})
