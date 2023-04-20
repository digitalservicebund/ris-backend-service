import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import OtherOfficialReferenceInputGroup from "@/components/OtherOfficialReferenceInputGroup.vue"
import { Metadata, MetadatumType } from "@/domain/Norm"

function renderComponent(options?: { modelValue?: Metadata }) {
  const props = {
    modelValue: options?.modelValue ?? {},
  }
  return render(OtherOfficialReferenceInputGroup, { props })
}

describe("OtherOfficialReferenceInputGroup", () => {
  it("renders all inputs", () => {
    renderComponent({
      modelValue: {
        [MetadatumType.ENTITY]: ["test value"],
      },
    })

    const entityInput = screen.queryByRole("textbox", {
      name: "Sonstige amtliche Fundstelle",
    }) as HTMLInputElement

    expect(entityInput).toBeInTheDocument()
    expect(entityInput).toHaveValue("test value")
  })

  it("shows the correct model value entry in the associated input", () => {
    renderComponent({
      modelValue: {
        [MetadatumType.ENTITY]: ["foo"],
      },
    })

    const entityInput = screen.queryByDisplayValue("foo")
    expect(entityInput).toBeInTheDocument()
  })

  it("emits update model value event when input value changes", async () => {
    const user = userEvent.setup()
    const modelValue = {}
    renderComponent({ modelValue })

    const input = screen.getAllByRole("textbox")
    await user.type(input[0], "foo")

    expect(modelValue).toEqual({
      [MetadatumType.ENTITY]: ["foo"],
    })
  })
})
