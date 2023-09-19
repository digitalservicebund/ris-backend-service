import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createPinia, setActivePinia } from "pinia"
import OtherOfficialAnnouncementInputGroup from "@/components/officialReference/OtherOfficialAnnouncementInputGroup.vue"
import { Metadata, MetadatumType } from "@/domain/norm"

type OtherOfficialAnnouncementInputGroupProps = InstanceType<
  typeof OtherOfficialAnnouncementInputGroup
>["$props"]

function renderComponent(
  props?: Partial<OtherOfficialAnnouncementInputGroupProps>,
) {
  const effectiveProps = {
    modelValue: props?.modelValue ?? {},
    "onUpdate:modelValue": props?.["onUpdate:modelValue"] ?? vi.fn(),
  }

  const utils = render(OtherOfficialAnnouncementInputGroup, {
    props: effectiveProps,
  })
  // eslint-disable-next-line testing-library/await-async-events
  const user = userEvent.setup()
  return { user, ...utils }
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

    const otherOfficialReferenceInput = screen.getByRole("textbox", {
      name: "Sonstige amtliche Fundstelle",
    })

    expect(otherOfficialReferenceInput).toBeInTheDocument()
    expect(otherOfficialReferenceInput).toHaveValue("test value")
  })

  it("shows the correct model value entry in the associated input", () => {
    renderComponent({
      modelValue: {
        [MetadatumType.OTHER_OFFICIAL_REFERENCE]: ["foo"],
      },
    })

    const otherOfficialReferenceInput = screen.getByDisplayValue("foo")
    expect(otherOfficialReferenceInput).toBeInTheDocument()
  })

  it("emits update model value event when input value changes", async () => {
    let modelValue: Metadata = {}
    const updateModelValue = vi.fn().mockImplementation((data: Metadata) => {
      modelValue = data
    })

    const { user } = renderComponent({
      modelValue,
      "onUpdate:modelValue": updateModelValue,
    })

    const input = screen.getAllByRole("textbox")
    await user.type(input[0], "foo")

    expect(modelValue).toEqual({
      [MetadatumType.OTHER_OFFICIAL_REFERENCE]: ["foo"],
    })
  })

  it("emits update model value event when input value is cleared", async () => {
    let modelValue: Metadata = { OTHER_OFFICIAL_REFERENCE: ["test"] }
    const updateModelValue = vi.fn().mockImplementation((data: Metadata) => {
      modelValue = data
    })

    const { user } = renderComponent({
      modelValue,
      "onUpdate:modelValue": updateModelValue,
    })

    const input = screen.getByRole("textbox")
    expect(input).toHaveValue("test")

    await user.clear(input)
    expect(modelValue).toEqual({ OTHER_OFFICIAL_REFERENCE: undefined })
  })
})
