import userEvent from "@testing-library/user-event"
import { render, screen, within } from "@testing-library/vue"
import PrintAnnouncementInputGroup from "@/components/officialReference/PrintAnnouncementInputGroup.vue"
import { Metadata } from "@/domain/Norm"

function renderComponent(options?: { modelValue?: Metadata }) {
  const props = {
    modelValue: options?.modelValue ?? {},
  }
  const utils = render(PrintAnnouncementInputGroup, { props })
  const user = userEvent.setup()
  return { user, ...utils }
}

function getControls() {
  const announcementGazetteInput = screen.queryByRole("textbox", {
    name: "Verkündungsblatt",
  }) as HTMLInputElement

  const yearInput = screen.queryByRole("textbox", {
    name: "Jahr",
  }) as HTMLInputElement

  const numberInput = screen.queryByRole("textbox", {
    name: "Nummer",
  }) as HTMLInputElement

  const pageInput = screen.queryByRole("textbox", {
    name: "Seitenzahl",
  }) as HTMLInputElement

  const additionalInfoInput = screen.queryByRole("textbox", {
    name: "Zusatzangaben",
  }) as HTMLInputElement

  const additionalInfoChips = within(
    screen.getByTestId("chips-input_printAnnouncementInfo"),
  ).queryAllByTestId("chip")

  const additionalInfoChipValues = within(
    screen.getByTestId("chips-input_printAnnouncementInfo"),
  ).queryAllByTestId("chip-value")

  const explanationInput = screen.queryByRole("textbox", {
    name: "Erläuterungen",
  }) as HTMLInputElement

  const explanationChips = within(
    screen.getByTestId("chips-input_printAnnouncementExplanations"),
  ).queryAllByTestId("chip")

  const explanationChipValues = within(
    screen.getByTestId("chips-input_printAnnouncementExplanations"),
  ).queryAllByTestId("chip-value")

  return {
    announcementGazetteInput,
    yearInput,
    numberInput,
    pageInput,
    additionalInfoInput,
    additionalInfoChips,
    additionalInfoChipValues,
    explanationInput,
    explanationChips,
    explanationChipValues,
  }
}

describe("PrintAnnouncementInputGroup", () => {
  it("renders all print announcement inputs", () => {
    renderComponent({
      modelValue: {
        ANNOUNCEMENT_GAZETTE: ["test announcement gazette"],
        YEAR: ["4711"],
        NUMBER: ["test number"],
        PAGE: ["test page"],
        ADDITIONAL_INFO: ["test additional info 1", "test additional info 2"],
        EXPLANATION: ["test explanation 1", "test explanation 2"],
      },
    })

    const {
      announcementGazetteInput,
      yearInput,
      numberInput,
      pageInput,
      additionalInfoInput,
      additionalInfoChipValues,
      explanationInput,
      explanationChipValues,
    } = getControls()

    expect(announcementGazetteInput).toBeInTheDocument()
    expect(announcementGazetteInput).toHaveValue("test announcement gazette")

    expect(yearInput).toBeInTheDocument()
    expect(yearInput).toHaveValue("4711")

    expect(numberInput).toBeInTheDocument()
    expect(numberInput).toHaveValue("test number")

    expect(pageInput).toBeInTheDocument()
    expect(pageInput).toHaveValue("test page")

    expect(additionalInfoInput).toBeInTheDocument()
    expect(additionalInfoChipValues.map((value) => value.textContent)).toEqual([
      "test additional info 1",
      "test additional info 2",
    ])

    expect(explanationInput).toBeInTheDocument()
    expect(explanationChipValues.map((chip) => chip.textContent)).toEqual([
      "test explanation 1",
      "test explanation 2",
    ])
  })

  it("shows the correct model value entry in the associated input", () => {
    renderComponent({
      modelValue: {
        ANNOUNCEMENT_GAZETTE: ["abc"],
        YEAR: ["2012"],
        NUMBER: ["123"],
        PAGE: ["2"],
        ADDITIONAL_INFO: ["Info Text"],
        EXPLANATION: ["Explanation text"],
      },
    })

    const announcementGazetteInput = screen.queryByDisplayValue("abc")
    expect(announcementGazetteInput).toBeInTheDocument()

    const yearInput = screen.queryByDisplayValue("2012")
    expect(yearInput).toBeInTheDocument()

    const numberInput = screen.queryByDisplayValue("123")
    expect(numberInput).toBeInTheDocument()

    const pageInput = screen.queryByDisplayValue("2")
    expect(pageInput).toBeInTheDocument()
  })

  it("emits update model value event when an input value changes", async () => {
    const user = userEvent.setup()
    const modelValue = {}
    renderComponent({ modelValue })

    const {
      announcementGazetteInput,
      yearInput,
      numberInput,
      pageInput,
      additionalInfoInput,
      explanationInput,
    } = getControls()

    await user.type(announcementGazetteInput, "foo")
    await user.type(yearInput, "2023")
    await user.type(numberInput, "ban")
    await user.type(pageInput, "baz")
    await user.type(
      additionalInfoInput,
      "additional info 1{enter}additional info 2{enter}",
    )
    await user.type(
      explanationInput,
      "explanation 1{enter}explanation 2{enter}",
    )

    expect(modelValue).toEqual({
      ANNOUNCEMENT_GAZETTE: ["foo"],
      YEAR: ["2023"],
      NUMBER: ["ban"],
      PAGE: ["baz"],
      ADDITIONAL_INFO: ["additional info 1", "additional info 2"],
      EXPLANATION: ["explanation 1", "explanation 2"],
    })
  })

  it("emits update model value event when an input value is cleared", async () => {
    const user = userEvent.setup()
    const modelValue: Metadata = {
      ANNOUNCEMENT_GAZETTE: ["abc"],
      YEAR: ["2012"],
      NUMBER: ["123"],
      PAGE: ["2"],
      ADDITIONAL_INFO: ["Info Text"],
      EXPLANATION: ["Explanation text"],
    }
    renderComponent({ modelValue })

    const {
      announcementGazetteInput,
      yearInput,
      numberInput,
      pageInput,
      additionalInfoChips,
      explanationChips,
    } = getControls()

    expect(announcementGazetteInput).toHaveValue("abc")
    await user.clear(announcementGazetteInput)
    expect(modelValue.ANNOUNCEMENT_GAZETTE).toBeUndefined()

    expect(yearInput).toHaveValue("2012")
    await user.clear(yearInput)
    expect(modelValue.YEAR).toBeUndefined()

    expect(numberInput).toHaveValue("123")
    await user.clear(numberInput)
    expect(modelValue.NUMBER).toBeUndefined()

    expect(pageInput).toHaveValue("2")
    await user.clear(pageInput)
    expect(modelValue.PAGE).toBeUndefined()

    for (const chip of additionalInfoChips) {
      const clearButton = within(chip).getByRole("button")
      await user.click(clearButton)
    }
    expect(modelValue.ADDITIONAL_INFO).toBeUndefined()

    for (const chip of explanationChips) {
      const clearButton = within(chip).getByRole("button")
      await user.click(clearButton)
    }
    expect(modelValue.EXPLANATION).toBeUndefined()
  })
})
