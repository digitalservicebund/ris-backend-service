import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import AgeIndicationInputGroup from "@/components/AgeIndicationInputGroup.vue"
import { Metadata, MetadatumType } from "@/domain/Norm"

function renderComponent(options?: { modelValue?: Metadata }) {
  const props = {
    modelValue: options?.modelValue ?? {},
  }

  return render(AgeIndicationInputGroup, { props })
}

describe("AgeIndicationInputGroup", () => {
  it("renders an input field for the Starting Age value", async () => {
    renderComponent({
      modelValue: { [MetadatumType.RANGE_START]: ["test value"] },
    })

    const input = screen.queryByRole("textbox", {
      name: "Anfang",
    }) as HTMLInputElement

    expect(input).toBeVisible()
    expect(input).toHaveValue("test value")
  })

  it("renders an input field for the End Age value", async () => {
    renderComponent({
      modelValue: { [MetadatumType.RANGE_END]: ["test value"] },
    })

    const input = screen.queryByRole("textbox", {
      name: "Ende",
    }) as HTMLInputElement

    expect(input).toBeVisible()
    expect(input).toHaveValue("test value")
  })

  it("clears the input fields for Starting Age and End Age", async () => {
    const user = userEvent.setup()
    const modelValue: Metadata = {
      RANGE_START: ["test start"],
      RANGE_END: ["test end"],
    }
    renderComponent({ modelValue })

    const startInput = screen.queryByRole("textbox", {
      name: "Anfang",
    }) as HTMLInputElement

    const endInput = screen.queryByRole("textbox", {
      name: "Ende",
    }) as HTMLInputElement

    expect(startInput).toHaveValue("test start")
    await user.clear(startInput)
    expect(modelValue.RANGE_START).toBeUndefined()

    expect(endInput).toHaveValue("test end")
    await user.clear(endInput)
    expect(modelValue.RANGE_END).toBeUndefined()
  })
})
