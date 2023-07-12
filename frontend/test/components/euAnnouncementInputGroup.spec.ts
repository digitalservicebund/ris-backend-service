import userEvent from "@testing-library/user-event"
import { within, render, screen } from "@testing-library/vue"
import EuAnnouncementInputGroup from "@/components/EuAnnouncementInputGroup.vue"
import { Metadata } from "@/domain/Norm"

function renderComponent(options?: { modelValue?: Metadata }) {
  const props = {
    modelValue: options?.modelValue ?? {},
  }
  const utils = render(EuAnnouncementInputGroup, { props })
  const user = userEvent.setup()
  return { user, ...utils }
}

function getControls() {
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

  const additionalInfoChips = within(
    screen.getByTestId("chips-input_euAnnouncementInfo"),
  ).queryAllByTestId("chip")

  const additionalInfoChipValues = within(
    screen.getByTestId("chips-input_euAnnouncementInfo"),
  ).queryAllByTestId("chip-value")

  const explanationInput = screen.queryByRole("textbox", {
    name: "Erläuterungen",
  }) as HTMLInputElement

  const explanationChips = within(
    screen.getByTestId("chips-input_euAnnouncementExplanations"),
  ).queryAllByTestId("chip")

  const explanationChipValues = within(
    screen.getByTestId("chips-input_euAnnouncementExplanations"),
  ).queryAllByTestId("chip-value")

  return {
    euGovernmentGazetteInput,
    yearInput,
    seriesInput,
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

describe("EuGovernmentGazetteInputGroup", () => {
  it("renders all inputs", () => {
    renderComponent({
      modelValue: {
        EU_GOVERNMENT_GAZETTE: ["Amtsblatt der EU"],
        YEAR: ["test year"],
        SERIES: ["test series"],
        NUMBER: ["test number"],
        PAGE: ["test page"],
        ADDITIONAL_INFO: ["test additional info 1", "test additional info 2"],
        EXPLANATION: ["test explanation 1", "test explanation 2"],
      },
    })

    const {
      euGovernmentGazetteInput,
      yearInput,
      seriesInput,
      numberInput,
      pageInput,
      additionalInfoInput,
      additionalInfoChipValues,
      explanationInput,
      explanationChipValues,
    } = getControls()

    expect(euGovernmentGazetteInput).toBeInTheDocument()
    expect(euGovernmentGazetteInput).toHaveValue("Amtsblatt der EU")

    expect(yearInput).toBeInTheDocument()
    expect(yearInput).toHaveValue("test year")

    expect(seriesInput).toBeInTheDocument()
    expect(seriesInput).toHaveValue("test series")

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
    expect(explanationChipValues.map((value) => value.textContent)).toEqual([
      "test explanation 1",
      "test explanation 2",
    ])
  })

  it("shows the correct model value entry in the associated input", () => {
    renderComponent({
      modelValue: {
        EU_GOVERNMENT_GAZETTE: ["Amtsblatt der EU"],
        YEAR: ["01-01-2023"],
        SERIES: ["foo"],
        NUMBER: ["1"],
        PAGE: ["2"],
        ADDITIONAL_INFO: ["test info"],
        EXPLANATION: ["test explanation"],
      },
    })

    const euGovernmentGazetteInput =
      screen.queryByDisplayValue("Amtsblatt der EU")
    expect(euGovernmentGazetteInput).toBeInTheDocument()

    const yearInput = screen.queryByDisplayValue("01-01-2023")
    expect(yearInput).toBeInTheDocument()

    const seriesInput = screen.queryByDisplayValue("foo")
    expect(seriesInput).toBeInTheDocument()

    const numberInput = screen.queryByDisplayValue("1")
    expect(numberInput).toBeInTheDocument()

    const pageInput = screen.queryByDisplayValue("2")
    expect(pageInput).toBeInTheDocument()
  })

  it("emits update model value event when an input value changes", async () => {
    const user = userEvent.setup()
    const modelValue = {}
    renderComponent({ modelValue })

    const {
      yearInput,
      seriesInput,
      numberInput,
      pageInput,
      additionalInfoInput,
      explanationInput,
    } = getControls()

    await user.type(yearInput, "2023")
    await user.type(seriesInput, "0")
    await user.type(numberInput, "1")
    await user.type(pageInput, "2")
    await user.type(
      additionalInfoInput,
      "additional info 1{enter}additional info 2{enter}",
    )
    await user.type(
      explanationInput,
      "explanation 1{enter}explanation 2{enter}",
    )

    expect(modelValue).toEqual({
      YEAR: ["2023"],
      SERIES: ["0"],
      NUMBER: ["1"],
      PAGE: ["2"],
      ADDITIONAL_INFO: ["additional info 1", "additional info 2"],
      EXPLANATION: ["explanation 1", "explanation 2"],
    })
  })

  it("emits update model value event when an input value is cleared", async () => {
    const user = userEvent.setup()
    const modelValue: Metadata = {
      EU_GOVERNMENT_GAZETTE: ["Amtsblatt der EU"],
      YEAR: ["2023"],
      SERIES: ["0"],
      NUMBER: ["1"],
      PAGE: ["2"],
      ADDITIONAL_INFO: ["foo bar"],
      EXPLANATION: ["bar foo"],
    }
    renderComponent({ modelValue })

    const {
      yearInput,
      seriesInput,
      numberInput,
      pageInput,
      additionalInfoChips,
      explanationChips,
    } = getControls()

    expect(yearInput).toHaveValue("2023")
    await user.clear(yearInput)
    expect(modelValue.YEAR).toBeUndefined()

    expect(seriesInput).toHaveValue("0")
    await user.clear(seriesInput)
    expect(modelValue.SERIES).toBeUndefined()

    expect(numberInput).toHaveValue("1")
    await user.clear(numberInput)
    expect(modelValue.NUMBER).toBeUndefined()

    expect(pageInput).toHaveValue("2")
    await user.clear(pageInput)
    expect(modelValue.PAGE).toBeUndefined()

    for (const chip of additionalInfoChips) {
      const clearButton = within(chip).getByLabelText("Löschen")
      await user.click(clearButton)
    }
    expect(modelValue.ADDITIONAL_INFO).toStrictEqual([])

    for (const chip of explanationChips) {
      const clearButton = within(chip).getByLabelText("Löschen")
      await user.click(clearButton)
    }
    expect(modelValue.EXPLANATION).toStrictEqual([])
  })
})
