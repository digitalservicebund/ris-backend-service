import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import NormProviderInputGroup from "@/components/NormProviderInputGroup.vue"
import { Metadata } from "@/domain/Norm"

function renderComponent(options?: { modelValue?: Metadata }) {
  const props = {
    modelValue: options?.modelValue ?? {},
  }

  return render(NormProviderInputGroup, { props })
}

function getControls() {
  const entityInput = screen.queryByRole("textbox", {
    name: "Staat, Land, Stadt, Landkreis oder juristische Person, deren Hoheitsgewalt oder Rechtsmacht die Norm trägt",
  }) as HTMLInputElement

  const decidingBodyInput = screen.queryByRole("textbox", {
    name: "Beschließendes Organ",
  }) as HTMLInputElement

  const resolutionMajorityInput = screen.queryByRole("checkbox", {
    name: "Beschlussfassung mit qualifizierter Mehrheit",
  }) as HTMLInputElement

  return { entityInput, decidingBodyInput, resolutionMajorityInput }
}

describe("NormProviderInputGroup", () => {
  it("renders an input field for the entity value", async () => {
    renderComponent({ modelValue: { ENTITY: ["test entity"] } })

    const { entityInput } = getControls()
    expect(entityInput).toBeVisible()
    expect(entityInput).toHaveValue("test entity")
  })

  it("renders an input field for the deciding body value", async () => {
    renderComponent({ modelValue: { DECIDING_BODY: ["test deciding body"] } })

    const { decidingBodyInput } = getControls()
    expect(decidingBodyInput).toBeVisible()
    expect(decidingBodyInput).toHaveValue("test deciding body")
  })

  it("renders an input field for the resolution majority value", async () => {
    renderComponent({ modelValue: { RESOLUTION_MAJORITY: [true] } })

    const { resolutionMajorityInput } = getControls()
    expect(resolutionMajorityInput).toBeVisible()
    expect(resolutionMajorityInput).toBeChecked()
  })

  it("updates the model value when user types into the input fields and checkbox", async () => {
    const user = userEvent.setup()
    const modelValue = {}
    renderComponent({ modelValue })

    const { entityInput, decidingBodyInput, resolutionMajorityInput } =
      getControls()

    await user.type(entityInput, "foo")
    await user.type(decidingBodyInput, "bar")
    await user.click(resolutionMajorityInput)

    expect(modelValue).toEqual({
      ENTITY: ["foo"],
      DECIDING_BODY: ["bar"],
      RESOLUTION_MAJORITY: [true],
    })
  })

  it("updates the model value when user types clears the input fields and checkbox", async () => {
    const user = userEvent.setup()
    const modelValue: Metadata = {
      ENTITY: ["foo"],
      DECIDING_BODY: ["bar"],
      RESOLUTION_MAJORITY: [true],
    }
    renderComponent({ modelValue })

    const { entityInput, decidingBodyInput, resolutionMajorityInput } =
      getControls()

    expect(entityInput).toHaveValue("foo")
    await user.clear(entityInput)
    expect(modelValue.ENTITY).toBeUndefined()

    expect(decidingBodyInput).toHaveValue("bar")
    await user.clear(decidingBodyInput)
    expect(modelValue.DECIDING_BODY).toBeUndefined()

    expect(resolutionMajorityInput).toBeChecked()
    await user.click(resolutionMajorityInput)
    expect(modelValue.RESOLUTION_MAJORITY).toEqual([false])
  })
})
