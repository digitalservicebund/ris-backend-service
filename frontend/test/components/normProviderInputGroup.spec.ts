import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import NormProviderInputGroup from "@/components/NormProviderInputGroup.vue"
import { Metadata, MetadatumType } from "@/domain/Norm"

function renderComponent(options?: { modelValue?: Metadata }) {
  const props = {
    modelValue: options?.modelValue ?? {},
  }

  return render(NormProviderInputGroup, { props })
}

describe("NormProviderInputGroup", () => {
  it("renders an input field for the entity value", async () => {
    renderComponent({
      modelValue: { [MetadatumType.ENTITY]: ["test entity"] },
    })

    const input = screen.queryByRole("textbox", {
      name: "Staat, Land, Stadt, Landkreis oder juristische Person, deren Hoheitsgewalt oder Rechtsmacht die Norm trägt",
    }) as HTMLInputElement

    expect(input).toBeVisible()
    expect(input).toHaveValue("test entity")
  })
  it("renders an input field for the deciding body value", async () => {
    renderComponent({
      modelValue: { [MetadatumType.DECIDING_BODY]: ["test deciding body"] },
    })

    const input = screen.queryByRole("textbox", {
      name: "Beschließendes Organ",
    }) as HTMLInputElement

    expect(input).toBeVisible()
    expect(input).toHaveValue("test deciding body")
  })
  it("renders an input field for the resolution majority value", async () => {
    renderComponent({
      modelValue: { [MetadatumType.RESOLUTION_MAJORITY]: [true] },
    })

    const input = screen.queryByRole("checkbox", {
      name: "Beschlussfassung mit qualifizierter Mehrheit",
    }) as HTMLInputElement

    expect(input).toBeVisible()
    expect(input.type).toBe("checkbox")
    expect(input).toBeChecked()
  })

  it("updates the model value when user types into the input fields and checkbox", async () => {
    const user = userEvent.setup()
    const modelValue = {}
    renderComponent({ modelValue })

    const entityInput = screen.queryByRole("textbox", {
      name: "Staat, Land, Stadt, Landkreis oder juristische Person, deren Hoheitsgewalt oder Rechtsmacht die Norm trägt",
    }) as HTMLInputElement

    const decidingBodyinput = screen.queryByRole("textbox", {
      name: "Beschließendes Organ",
    }) as HTMLInputElement

    const resolutionMajorityInput = screen.queryByRole("checkbox", {
      name: "Beschlussfassung mit qualifizierter Mehrheit",
    }) as HTMLInputElement

    await user.type(entityInput, "foo")
    await user.type(decidingBodyinput, "bar")
    await user.click(resolutionMajorityInput)

    expect(modelValue).toEqual({
      [MetadatumType.ENTITY]: ["foo"],
      [MetadatumType.DECIDING_BODY]: ["bar"],
      [MetadatumType.RESOLUTION_MAJORITY]: [true],
    })
  })
})
