import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import PrintAnnouncementInputGroup from "@/components/PrintAnnouncementInputGroup.vue"
import { Metadata, MetadatumType } from "@/domain/Norm"

function renderComponent(options?: { modelValue?: Metadata }) {
  const props = {
    modelValue: options?.modelValue ?? {},
  }
  const utils = render(PrintAnnouncementInputGroup, { props })
  const user = userEvent.setup()
  return { user, ...utils }
}

describe("PrintAnnouncementInputGroup", () => {
  it("renders all print announcement inputs", () => {
    renderComponent({
      modelValue: {
        [MetadatumType.ANNOUNCEMENT_GAZETTE]: ["test value"],
        [MetadatumType.YEAR]: ["test value"],
        [MetadatumType.NUMBER]: ["test value"],
        [MetadatumType.PAGE_NUMBER]: ["test value"],
        [MetadatumType.ADDITIONAL_INFO]: ["test value"],
        [MetadatumType.EXPLANATION]: ["test value"],
      },
    })

    const announcementGazetteInput = screen.queryByRole("textbox", {
      name: "Verkündungsblatt",
    }) as HTMLInputElement

    const yearInput = screen.queryByRole("textbox", {
      name: "Jahr",
    }) as HTMLInputElement

    const numberInput = screen.queryByRole("textbox", {
      name: "Nummer",
    }) as HTMLInputElement

    const pageNumberInput = screen.queryByRole("textbox", {
      name: "Seitenzahl",
    }) as HTMLInputElement

    const additionalInfoInput = screen.queryByRole("textbox", {
      name: "Zusatzangaben",
    }) as HTMLInputElement

    const explanationInput = screen.queryByRole("textbox", {
      name: "Erläuterungen",
    }) as HTMLInputElement

    expect(announcementGazetteInput).toBeInTheDocument()
    expect(announcementGazetteInput).toHaveValue("test value")

    expect(yearInput).toBeInTheDocument()
    expect(yearInput).toHaveValue("test value")

    expect(numberInput).toBeInTheDocument()
    expect(numberInput).toHaveValue("test value")

    expect(pageNumberInput).toBeInTheDocument()
    expect(pageNumberInput).toHaveValue("test value")

    expect(additionalInfoInput).toBeInTheDocument()
    expect(additionalInfoInput).toHaveValue("test value")

    expect(explanationInput).toBeInTheDocument()
    expect(explanationInput).toHaveValue("test value")
  })

  it("shows the correct model value entry in the associated input", () => {
    renderComponent({
      modelValue: {
        [MetadatumType.ANNOUNCEMENT_GAZETTE]: ["abc"],
        [MetadatumType.YEAR]: ["2012"],
        [MetadatumType.NUMBER]: ["123"],
        [MetadatumType.PAGE_NUMBER]: ["2"],
        [MetadatumType.ADDITIONAL_INFO]: ["Info Text"],
        [MetadatumType.EXPLANATION]: ["Explanation text"],
      },
    })

    const announcementGazetteInput = screen.queryByDisplayValue("abc")
    const yearInput = screen.queryByDisplayValue("2012")
    const numberInput = screen.queryByDisplayValue("123")
    const pageNumberInput = screen.queryByDisplayValue("2")
    const additionalInfoInput = screen.queryByDisplayValue("Info Text")
    const explanationInput = screen.queryByDisplayValue("Explanation text")

    expect(announcementGazetteInput).toBeInTheDocument()
    expect(yearInput).toBeInTheDocument()
    expect(numberInput).toBeInTheDocument()
    expect(pageNumberInput).toBeInTheDocument()
    expect(additionalInfoInput).toBeInTheDocument()
    expect(explanationInput).toBeInTheDocument()
  })

  it("emits update model value event when an input value changes", async () => {
    const user = userEvent.setup()
    const modelValue = {}
    renderComponent({ modelValue })

    const input = screen.getAllByRole("textbox")
    await user.type(input[0], "foo")
    await user.type(input[1], "2023")
    await user.type(input[2], "ban")
    await user.type(input[3], "baz")
    await user.type(input[4], "foo bar")
    await user.type(input[5], "bar foo")

    expect(modelValue).toEqual({
      [MetadatumType.ANNOUNCEMENT_GAZETTE]: ["foo"],
      [MetadatumType.YEAR]: ["2023"],
      [MetadatumType.NUMBER]: ["ban"],
      [MetadatumType.PAGE_NUMBER]: ["baz"],
      [MetadatumType.ADDITIONAL_INFO]: ["foo bar"],
      [MetadatumType.EXPLANATION]: ["bar foo"],
    })
  })
})
