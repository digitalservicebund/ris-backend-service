import { userEvent } from "@testing-library/user-event"
import { render, screen, within } from "@testing-library/vue"
import { createPinia, setActivePinia } from "pinia"
import PrintAnnouncementInputGroup from "@/components/officialReference/PrintAnnouncementInputGroup.vue"
import { Metadata } from "@/domain/norm"

type PrintAnnouncementInputGroupProps = InstanceType<
  typeof PrintAnnouncementInputGroup
>["$props"]

function renderComponent(props?: Partial<PrintAnnouncementInputGroupProps>) {
  const effectiveProps = {
    modelValue: props?.modelValue ?? {},
    "onUpdate:modelValue": props?.["onUpdate:modelValue"] ?? vi.fn(),
  }

  const utils = render(PrintAnnouncementInputGroup, { props: effectiveProps })
  // eslint-disable-next-line testing-library/await-async-events
  const user = userEvent.setup()
  return { user, ...utils }
}

function getControls() {
  const announcementGazetteInput = screen.getByRole("textbox", {
    name: "Verkündungsblatt",
  })

  const yearInput = screen.getByRole("textbox", {
    name: "Jahr",
  })

  const numberInput = screen.getByRole("textbox", {
    name: "Nummer",
  })

  const pageInput = screen.getByRole("textbox", {
    name: "Seitenzahl",
  })

  const additionalInfoInput = screen.getByRole("textbox", {
    name: "Zusatzangaben",
  })

  const additionalInfoChips = within(
    screen.getByTestId("chips-input_printAnnouncementInfo"),
  ).queryAllByTestId("chip")

  const additionalInfoChipValues = within(
    screen.getByTestId("chips-input_printAnnouncementInfo"),
  ).queryAllByTestId("chip-value")

  const explanationInput = screen.getByRole("textbox", {
    name: "Erläuterungen",
  })

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
  beforeEach(async () => {
    setActivePinia(createPinia())
  })

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
    let modelValue: Metadata = {}
    const updateModelValue = vi.fn().mockImplementation((data: Metadata) => {
      modelValue = data
    })

    const { user, rerender } = renderComponent({
      modelValue,
      "onUpdate:modelValue": updateModelValue,
    })

    const {
      announcementGazetteInput,
      yearInput,
      numberInput,
      pageInput,
      additionalInfoInput,
      explanationInput,
    } = getControls()

    await user.type(announcementGazetteInput, "foo")
    await rerender({ modelValue })
    await user.type(yearInput, "2023")
    await rerender({ modelValue })
    await user.type(numberInput, "ban")
    await rerender({ modelValue })
    await user.type(pageInput, "baz")
    await rerender({ modelValue })
    await user.type(additionalInfoInput, "additional info 1{enter}")
    await rerender({ modelValue })
    await user.type(additionalInfoInput, "additional info 2{enter}")
    await rerender({ modelValue })
    await user.type(explanationInput, "explanation 1{enter}")
    await rerender({ modelValue })
    await user.type(explanationInput, "explanation 2{enter}")
    await rerender({ modelValue })

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
    let modelValue: Metadata = {
      ANNOUNCEMENT_GAZETTE: ["abc"],
      YEAR: ["2012"],
      NUMBER: ["123"],
      PAGE: ["2"],
      ADDITIONAL_INFO: ["Info Text"],
      EXPLANATION: ["Explanation text"],
    }
    const updateModelValue = vi.fn().mockImplementation((data: Metadata) => {
      modelValue = data
    })

    const { user, rerender } = renderComponent({
      modelValue,
      "onUpdate:modelValue": updateModelValue,
    })

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
    await rerender({ modelValue })
    expect(modelValue.ANNOUNCEMENT_GAZETTE).toBeUndefined()

    expect(yearInput).toHaveValue("2012")
    await user.clear(yearInput)
    await rerender({ modelValue })
    expect(modelValue.YEAR).toBeUndefined()

    expect(numberInput).toHaveValue("123")
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
