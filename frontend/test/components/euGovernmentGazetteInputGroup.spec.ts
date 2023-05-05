import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import EuAnnouncementInputGroup from "@/components/EuAnnouncementInputGroup.vue"
import { Metadata, MetadatumType } from "@/domain/Norm"

function renderComponent(options?: { modelValue?: Metadata }) {
  const props = {
    modelValue: options?.modelValue ?? {},
  }
  const utils = render(EuAnnouncementInputGroup, { props })
  const user = userEvent.setup()
  return { user, ...utils }
}

describe("EuGovernmentGazetteInputGroup", () => {
  it("renders all inputs", () => {
    renderComponent({
      modelValue: {
        [MetadatumType.EU_GOVERNMENT_GAZETTE]: ["Amtsblatt der EU"],
        [MetadatumType.YEAR]: ["test value"],
        [MetadatumType.SERIES]: ["test value"],
        [MetadatumType.NUMBER]: ["test value"],
        [MetadatumType.PAGE]: ["test value"],
        [MetadatumType.ADDITIONAL_INFO]: ["test value"],
        [MetadatumType.EXPLANATION]: ["test value"],
      },
    })

    const euGovernmentGazetteInput = screen.queryByRole("textbox", {
      name: "Amtsblatt der EU",
    }) as HTMLInputElement

    const yearInput = screen.queryByRole("textbox", {
      name: "Jahresangabe",
    }) as HTMLInputElement

    const seriesInput = screen.queryByRole("textbox", {
      name: "Reihe",
    }) as HTMLInputElement

    const numberInput = screen.queryByRole("textbox", {
      name: "Nummer des Amtsblatts",
    }) as HTMLInputElement

    const pageInput = screen.queryByRole("textbox", {
      name: "Seitenzahl",
    }) as HTMLInputElement

    const additionalInfoInput = screen.queryByRole("textbox", {
      name: "Zusatzangaben",
    }) as HTMLInputElement

    const explanationInput = screen.queryByRole("textbox", {
      name: "Erläuterungen",
    }) as HTMLInputElement

    expect(euGovernmentGazetteInput).toBeInTheDocument()
    expect(euGovernmentGazetteInput).toHaveValue("Amtsblatt der EU")

    expect(yearInput).toBeInTheDocument()
    expect(yearInput).toHaveValue("test value")

    expect(seriesInput).toBeInTheDocument()
    expect(seriesInput).toHaveValue("test value")

    expect(numberInput).toBeInTheDocument()
    expect(numberInput).toHaveValue("test value")

    expect(pageInput).toBeInTheDocument()
    expect(pageInput).toHaveValue("test value")

    expect(additionalInfoInput).toBeInTheDocument()
    expect(additionalInfoInput).toHaveValue("test value")

    expect(explanationInput).toBeInTheDocument()
    expect(explanationInput).toHaveValue("test value")
  })

  it("shows the correct model value entry in the associated input", () => {
    renderComponent({
      modelValue: {
        [MetadatumType.EU_GOVERNMENT_GAZETTE]: ["Amtsblatt der EU"],
        [MetadatumType.YEAR]: ["01-01-2023"],
        [MetadatumType.SERIES]: ["foo"],
        [MetadatumType.NUMBER]: ["1"],
        [MetadatumType.PAGE]: ["2"],
        [MetadatumType.ADDITIONAL_INFO]: ["test info"],
        [MetadatumType.EXPLANATION]: ["test explanation"],
      },
    })

    const euGovernmentGazetteInput =
      screen.queryByDisplayValue("Amtsblatt der EU")
    const yearInput = screen.queryByDisplayValue("01-01-2023")
    const seriesInput = screen.queryByDisplayValue("foo")
    const numberInput = screen.queryByDisplayValue("1")
    const pageInput = screen.queryByDisplayValue("2")
    const additionalInfoInput = screen.queryByDisplayValue("test info")
    const explanationInput = screen.queryByDisplayValue("test explanation")

    expect(euGovernmentGazetteInput).toBeInTheDocument()
    expect(yearInput).toBeInTheDocument()
    expect(seriesInput).toBeInTheDocument()
    expect(numberInput).toBeInTheDocument()
    expect(pageInput).toBeInTheDocument()
    expect(additionalInfoInput).toBeInTheDocument()
    expect(explanationInput).toBeInTheDocument()
  })

  it("emits update model value event when an input value changes", async () => {
    const user = userEvent.setup()
    const modelValue = {}
    renderComponent({ modelValue })

    const input = screen.getAllByRole("textbox")
    await user.type(input[1], "2023")
    await user.type(input[2], "0")
    await user.type(input[3], "1")
    await user.type(input[4], "2")
    await user.type(input[5], "foo bar")
    await user.type(input[6], "bar foo")

    expect(modelValue).toEqual({
      [MetadatumType.YEAR]: ["2023"],
      [MetadatumType.SERIES]: ["0"],
      [MetadatumType.NUMBER]: ["1"],
      [MetadatumType.PAGE]: ["2"],
      [MetadatumType.ADDITIONAL_INFO]: ["foo bar"],
      [MetadatumType.EXPLANATION]: ["bar foo"],
    })
  })
})
