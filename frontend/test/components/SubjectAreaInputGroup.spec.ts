import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import SubjectAreaInputGroup from "@/components/SubjectAreaInputGroup.vue"
import { Metadata } from "@/domain/Norm"

function renderComponent(options?: { modelValue?: Metadata }) {
  const props = {
    modelValue: options?.modelValue ?? {},
  }

  return render(SubjectAreaInputGroup, { props })
}

function getControls() {
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

  return { fnaInput, previousFnaInput, gestaInput, bgb3input }
}

describe("SubjectAreaInputGroup", () => {
  it("renders an input field for the FNA value", async () => {
    renderComponent({ modelValue: { SUBJECT_FNA: ["test value"] } })

    const { fnaInput } = getControls()
    expect(fnaInput).toBeVisible()
    expect(fnaInput).toHaveValue("test value")
  })

  it("renders an input field for the previousFna value", async () => {
    renderComponent({ modelValue: { SUBJECT_PREVIOUS_FNA: ["test value"] } })

    const { previousFnaInput } = getControls()
    expect(previousFnaInput).toBeVisible()
    expect(previousFnaInput).toHaveValue("test value")
  })

  it("renders an input field for the gesta value", async () => {
    renderComponent({ modelValue: { SUBJECT_GESTA: ["test value"] } })

    const { gestaInput } = getControls()
    expect(gestaInput).toBeVisible()
    expect(gestaInput).toHaveValue("test value")
  })

  it("renders an input field for the bgb3 value", async () => {
    renderComponent({ modelValue: { SUBJECT_BGB_3: ["test value"] } })

    const { bgb3input } = getControls()
    expect(bgb3input).toBeVisible()
    expect(bgb3input).toHaveValue("test value")
  })

  it("updates the model value when user types into the input fields", async () => {
    const user = userEvent.setup()
    const modelValue = {}
    renderComponent({ modelValue })

    const { fnaInput, previousFnaInput, gestaInput, bgb3input } = getControls()

    await user.type(fnaInput, "foo")
    await user.type(previousFnaInput, "bar")
    await user.type(gestaInput, "baz")
    await user.type(bgb3input, "ban")

    expect(modelValue).toEqual({
      SUBJECT_FNA: ["foo"],
      SUBJECT_PREVIOUS_FNA: ["bar"],
      SUBJECT_GESTA: ["baz"],
      SUBJECT_BGB_3: ["ban"],
    })
  })

  it("updates the model value when user types into the input fields", async () => {
    const user = userEvent.setup()
    const modelValue: Metadata = {
      SUBJECT_FNA: ["foo"],
      SUBJECT_PREVIOUS_FNA: ["bar"],
      SUBJECT_GESTA: ["baz"],
      SUBJECT_BGB_3: ["ban"],
    }
    renderComponent({ modelValue })

    const { fnaInput, previousFnaInput, gestaInput, bgb3input } = getControls()

    expect(fnaInput).toHaveValue("foo")
    await user.clear(fnaInput)
    expect(modelValue.SUBJECT_FNA).toBeUndefined()

    expect(previousFnaInput).toHaveValue("bar")
    await user.clear(previousFnaInput)
    expect(modelValue.SUBJECT_PREVIOUS_FNA).toBeUndefined()

    expect(gestaInput).toHaveValue("baz")
    await user.clear(gestaInput)
    expect(modelValue.SUBJECT_GESTA).toBeUndefined()

    expect(bgb3input).toHaveValue("ban")
    await user.clear(bgb3input)
    expect(modelValue.SUBJECT_BGB_3).toBeUndefined()
  })
})
