import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import OtherOfficialAnnouncementInputGroup from "@/components/OtherOfficialAnnouncementInputGroup.vue"
import { Metadata, MetadatumType } from "@/domain/Norm"

function renderComponent(options?: { modelValue?: Metadata }) {
  const props = {
    modelValue: options?.modelValue ?? {},
  }
  return render(OtherOfficialAnnouncementInputGroup, { props })
}

describe("OtherOfficialReferenceInputGroup", () => {
  it("renders all inputs", () => {
    renderComponent({
      modelValue: {
        [MetadatumType.OTHER_OFFICIAL_REFERENCE]: ["test value"],
      },
    })

    const otherOfficialReferenceInput = screen.queryByRole("textbox", {
      name: "Sonstige amtliche Fundstelle",
    }) as HTMLInputElement

    expect(otherOfficialReferenceInput).toBeInTheDocument()
    expect(otherOfficialReferenceInput).toHaveValue("test value")
  })

  it("shows the correct model value entry in the associated input", () => {
    renderComponent({
      modelValue: {
        [MetadatumType.OTHER_OFFICIAL_REFERENCE]: ["foo"],
      },
    })

    const otherOfficialReferenceInput = screen.queryByDisplayValue("foo")
    expect(otherOfficialReferenceInput).toBeInTheDocument()
  })

  it("emits update model value event when input value changes", async () => {
    const user = userEvent.setup()
    const modelValue = {}
    renderComponent({ modelValue })

    const input = screen.getAllByRole("textbox")
    await user.type(input[0], "foo")

    expect(modelValue).toEqual({
      [MetadatumType.OTHER_OFFICIAL_REFERENCE]: ["foo"],
    })
  })
})
