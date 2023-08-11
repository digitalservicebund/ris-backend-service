import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createPinia, setActivePinia } from "pinia"
import OtherOfficialAnnouncementInputGroup from "@/components/officialReference/OtherOfficialAnnouncementInputGroup.vue"
import { Metadata, MetadatumType } from "@/domain/Norm"

function renderComponent(options?: { modelValue?: Metadata }) {
  const props = {
    modelValue: options?.modelValue ?? {},
  }
  return render(OtherOfficialAnnouncementInputGroup, { props })
}

describe("OtherOfficialReferenceInputGroup", () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
  })
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

  it("emits update model value event when input value is cleared", async () => {
    const user = userEvent.setup()
    const modelValue: Metadata = { OTHER_OFFICIAL_REFERENCE: ["test"] }
    renderComponent({ modelValue })

    const input = screen.getByRole("textbox")
    expect(input).toHaveValue("test")

    await user.clear(input)
    expect(modelValue).toEqual({ OTHER_OFFICIAL_REFERENCE: undefined })
  })
})
