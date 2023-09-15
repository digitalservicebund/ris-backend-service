import userEvent from "@testing-library/user-event"
import { within, render, screen } from "@testing-library/vue"
import { createPinia, setActivePinia } from "pinia"
import EuAnnouncementInputGroup from "@/components/officialReference/EuAnnouncementInputGroup.vue"
import { Metadata } from "@/domain/norm"

type EuAnnouncementInputGroupProps = InstanceType<
  typeof EuAnnouncementInputGroup
>["$props"]

function renderComponent(props?: Partial<EuAnnouncementInputGroupProps>) {
  const effectiveProps = {
    modelValue: props?.modelValue ?? {},
    "onUpdate:modelValue": props?.["onUpdate:modelValue"] ?? vi.fn(),
  }

  const utils = render(EuAnnouncementInputGroup, { props: effectiveProps })
  // eslint-disable-next-line testing-library/await-async-events
  const user = userEvent.setup()
  return { user, ...utils }
}

function getControls() {
  const euGovernmentGazetteInput = screen.getByRole("textbox", {
    name: "Amtsblatt der EU",
  })

  const yearInput = screen.getByRole("textbox", {
    name: "Jahresangabe",
  })

  const seriesInput = screen.getByRole("textbox", {
    name: "Reihe",
  })

  const numberInput = screen.getByRole("textbox", {
    name: "Nummer des Amtsblatts",
  })

  const pageInput = screen.getByRole("textbox", {
    name: "Seitenzahl",
  })

  const additionalInfoInput = screen.getByRole("textbox", {
    name: "Zusatzangaben",
  })

  const additionalInfoChips = within(
    screen.getByTestId("chips-input_euAnnouncementInfo"),
  ).queryAllByTestId("chip")

  const additionalInfoChipValues = within(
    screen.getByTestId("chips-input_euAnnouncementInfo"),
  ).queryAllByTestId("chip-value")

  const explanationInput = screen.getByRole("textbox", {
    name: "Erläuterungen",
  })

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
  beforeEach(async () => {
    setActivePinia(createPinia())
  })

  it("renders all inputs", () => {
    renderComponent({
      modelValue: {
        EU_GOVERNMENT_GAZETTE: ["Amtsblatt der EU"],
        YEAR: ["4711"],
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
    expect(yearInput).toHaveValue("4711")

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
        YEAR: ["2023"],
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

    const yearInput = screen.queryByDisplayValue("2023")
    expect(yearInput).toBeInTheDocument()

    const seriesInput = screen.queryByDisplayValue("foo")
    expect(seriesInput).toBeInTheDocument()

    const numberInput = screen.queryByDisplayValue("1")
    expect(numberInput).toBeInTheDocument()

    const pageInput = screen.queryByDisplayValue("2")
    expect(pageInput).toBeInTheDocument()
  })

  it("emits update model value event when an input value changes", async () => {
    let modelValue: Metadata = {}
    const updateModelValue = vi.fn().mockImplementation((data: Metadata) => {
      modelValue = data
    })

    const { user, rerender } = renderComponent({
      modelValue,
      "onUpdate:modelValue": updateModelValue,
    })

    const {
      yearInput,
      seriesInput,
      numberInput,
      pageInput,
      additionalInfoInput,
      explanationInput,
    } = getControls()

    await user.type(yearInput, "2023")
    await rerender({ modelValue })
    await user.type(seriesInput, "0")
    await rerender({ modelValue })
    await user.type(numberInput, "1")
    await rerender({ modelValue })
    await user.type(pageInput, "2")
    await rerender({ modelValue })
    await user.type(additionalInfoInput, "additional info 1{enter}")
    await rerender({ modelValue })
    await user.type(additionalInfoInput, "additional info 2{enter}")
    await rerender({ modelValue })
    await user.type(explanationInput, "explanation 1{enter}")
    await rerender({ modelValue })
    await user.type(explanationInput, "explanation 2{enter}")

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
    let modelValue: Metadata = {
      EU_GOVERNMENT_GAZETTE: ["Amtsblatt der EU"],
      YEAR: ["2023"],
      SERIES: ["0"],
      NUMBER: ["1"],
      PAGE: ["2"],
      ADDITIONAL_INFO: ["foo bar"],
      EXPLANATION: ["bar foo"],
    }
    const updateModelValue = vi.fn().mockImplementation((data: Metadata) => {
      modelValue = data
    })

    const { user, rerender } = renderComponent({
      modelValue,
      "onUpdate:modelValue": updateModelValue,
    })

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
    await rerender({ modelValue })

    expect(modelValue.YEAR).toBeUndefined()

    expect(seriesInput).toHaveValue("0")
    await user.clear(seriesInput)
    await rerender({ modelValue })

    expect(modelValue.SERIES).toBeUndefined()

    expect(numberInput).toHaveValue("1")
    await user.clear(numberInput)
    await rerender({ modelValue })

    expect(modelValue.NUMBER).toBeUndefined()

    expect(pageInput).toHaveValue("2")
    await user.clear(pageInput)
    await rerender({ modelValue })

    expect(modelValue.PAGE).toBeUndefined()

    for (const chip of additionalInfoChips) {
      const clearButton = within(chip).getByRole("button")
      await user.click(clearButton)
      await rerender({ modelValue })
    }
    expect(modelValue.ADDITIONAL_INFO).toBeUndefined()

    for (const chip of explanationChips) {
      const clearButton = within(chip).getByRole("button")
      await user.click(clearButton)
      await rerender({ modelValue })
    }
    expect(modelValue.EXPLANATION).toBeUndefined()
  })
})
