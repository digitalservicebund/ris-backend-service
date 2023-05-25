import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
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

  const explanationInput = screen.queryByRole("textbox", {
    name: "Erläuterungen",
  }) as HTMLInputElement

  return {
    euGovernmentGazetteInput,
    yearInput,
    seriesInput,
    numberInput,
    pageInput,
    additionalInfoInput,
    explanationInput,
  }
}

describe("EuGovernmentGazetteInputGroup", () => {
  it("renders all inputs", () => {
    renderComponent({
      modelValue: {
        EU_GOVERNMENT_GAZETTE: ["Amtsblatt der EU"],
        YEAR: ["test value"],
        SERIES: ["test value"],
        NUMBER: ["test value"],
        PAGE: ["test value"],
        ADDITIONAL_INFO: ["test value"],
        EXPLANATION: ["test value"],
      },
    })

    const {
      euGovernmentGazetteInput,
      yearInput,
      seriesInput,
      numberInput,
      pageInput,
      additionalInfoInput,
      explanationInput,
    } = getControls()

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

    const additionalInfoInput = screen.queryByDisplayValue("test info")
    expect(additionalInfoInput).toBeInTheDocument()

    const explanationInput = screen.queryByDisplayValue("test explanation")
    expect(explanationInput).toBeInTheDocument()
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
    await user.type(additionalInfoInput, "foo bar")
    await user.type(explanationInput, "bar foo")

    expect(modelValue).toEqual({
      YEAR: ["2023"],
      SERIES: ["0"],
      NUMBER: ["1"],
      PAGE: ["2"],
      ADDITIONAL_INFO: ["foo bar"],
      EXPLANATION: ["bar foo"],
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
      additionalInfoInput,
      explanationInput,
    } = getControls()

    expect(yearInput).toHaveValue("2023")
    await user.clear(yearInput)
    expect(modelValue.YEAR).toBe(undefined)

    expect(seriesInput).toHaveValue("0")
    await user.clear(seriesInput)
    expect(modelValue.SERIES).toBe(undefined)

    expect(numberInput).toHaveValue("1")
    await user.clear(numberInput)
    expect(modelValue.NUMBER).toBe(undefined)

    expect(pageInput).toHaveValue("2")
    await user.clear(pageInput)
    expect(modelValue.PAGE).toBe(undefined)

    expect(additionalInfoInput).toHaveValue("foo bar")
    await user.clear(additionalInfoInput)
    expect(modelValue.ADDITIONAL_INFO).toBe(undefined)

    expect(explanationInput).toHaveValue("bar foo")
    await user.clear(explanationInput)
    expect(modelValue.EXPLANATION).toBe(undefined)
  })
})
