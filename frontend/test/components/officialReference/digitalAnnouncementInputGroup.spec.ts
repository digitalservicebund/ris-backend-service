import userEvent from "@testing-library/user-event"
import { render, screen, within } from "@testing-library/vue"
import { createPinia, setActivePinia } from "pinia"
import DigitalAnnouncementInputGroup from "@/components/officialReference/DigitalAnnouncementInputGroup.vue"
import { Metadata } from "@/domain/norm"

type DigitalAnnouncementInputGroupProps = InstanceType<
  typeof DigitalAnnouncementInputGroup
>["$props"]

function renderComponent(props?: Partial<DigitalAnnouncementInputGroupProps>) {
  const effectiveProps = {
    modelValue: props?.modelValue ?? {},
    "onUpdate:modelValue": props?.["onUpdate:modelValue"] ?? vi.fn(),
  }

  const utils = render(DigitalAnnouncementInputGroup, { props: effectiveProps })
  // eslint-disable-next-line testing-library/await-async-events
  const user = userEvent.setup()
  return { user, ...utils }
}

function getControls() {
  const announcementMediumInput = screen.getByRole("textbox", {
    name: "Verkündungsmedium",
  })

  const dateInput = screen.getByRole("textbox", {
    name: "Verkündungsdatum",
  })

  const editionInput = screen.getByRole("textbox", {
    name: "Ausgabenummer",
  })

  const yearInput = screen.getByRole("textbox", {
    name: "Jahr",
  })

  const pageInput = screen.getByRole("textbox", {
    name: "Seitenzahl",
  })

  const areaOfPublicationInput = screen.getByRole("textbox", {
    name: "Bereich der Veröffentlichung",
  })

  const numberOfPublicationInRespectiveAreaInput = screen.getByRole("textbox", {
    name: "Nummer der Veröffentlichung im jeweiligen Bereich",
  })

  const additionalInfoInput = screen.getByRole("textbox", {
    name: "Zusatzangaben",
  })

  const additionalInfoChips = within(
    screen.getByTestId("chips-input_digitalAnnouncementInfo"),
  ).queryAllByTestId("chip")

  const additionalInfoChipValues = within(
    screen.getByTestId("chips-input_digitalAnnouncementInfo"),
  ).queryAllByTestId("chip-value")

  const explanationInput = screen.getByRole("textbox", {
    name: "Erläuterungen",
  })

  const explanationChips = within(
    screen.getByTestId("chips-input_digitalAnnouncementExplanations"),
  ).queryAllByTestId("chip")

  const explanationChipValues = within(
    screen.getByTestId("chips-input_digitalAnnouncementExplanations"),
  ).queryAllByTestId("chip-value")

  return {
    announcementMediumInput,
    dateInput,
    editionInput,
    yearInput,
    pageInput,
    areaOfPublicationInput,
    numberOfPublicationInRespectiveAreaInput,
    additionalInfoInput,
    additionalInfoChips,
    additionalInfoChipValues,
    explanationInput,
    explanationChips,
    explanationChipValues,
  }
}

describe("DigitalAnnouncementInputGroup", () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
  })

  it("renders all digital announcement inputs", () => {
    renderComponent({
      modelValue: {
        ANNOUNCEMENT_MEDIUM: ["test announcement medium"],
        DATE: ["2023-01-01"],
        EDITION: ["test edition"],
        YEAR: ["4711"],
        PAGE: ["test page"],
        AREA_OF_PUBLICATION: ["test area of publication"],
        NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA: [
          "test number of publication in the respective area",
        ],
        ADDITIONAL_INFO: ["test additional info 1", "test additional info 2"],
        EXPLANATION: ["test explanation 1", "test explanation 2"],
      },
    })

    const {
      announcementMediumInput,
      dateInput,
      editionInput,
      yearInput,
      areaOfPublicationInput,
      pageInput,
      numberOfPublicationInRespectiveAreaInput,
      additionalInfoInput,
      additionalInfoChipValues,
      explanationInput,
      explanationChipValues,
    } = getControls()

    expect(announcementMediumInput).toBeInTheDocument()
    expect(announcementMediumInput).toHaveValue("test announcement medium")

    expect(dateInput).toBeInTheDocument()
    expect(dateInput).toHaveValue("01.01.2023")

    expect(editionInput).toBeInTheDocument()
    expect(editionInput).toHaveValue("test edition")

    expect(yearInput).toBeInTheDocument()
    expect(yearInput).toHaveValue("4711")

    expect(pageInput).toBeInTheDocument()
    expect(pageInput).toHaveValue("test page")

    expect(areaOfPublicationInput).toBeInTheDocument()
    expect(areaOfPublicationInput).toHaveValue("test area of publication")

    expect(numberOfPublicationInRespectiveAreaInput).toBeInTheDocument()
    expect(numberOfPublicationInRespectiveAreaInput).toHaveValue(
      "test number of publication in the respective area",
    )

    expect(additionalInfoInput).toBeInTheDocument()
    expect(additionalInfoChipValues.map((chip) => chip.textContent)).toEqual([
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
        ANNOUNCEMENT_MEDIUM: ["foo"],
        DATE: ["2023-01-01"],
        EDITION: ["1"],
        YEAR: ["2023"],
        PAGE: ["25"],
        AREA_OF_PUBLICATION: ["bar"],
        NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA: ["baz"],
        ADDITIONAL_INFO: ["foo bar"],
        EXPLANATION: ["baz ban"],
      },
    })

    const announcementMediumInput = screen.queryByDisplayValue("foo")
    expect(announcementMediumInput).toBeInTheDocument()

    const dateInput = screen.queryByRole("textbox", {
      name: "Verkündungsdatum",
    }) as HTMLInputElement

    expect(dateInput).toBeInTheDocument()
    expect(dateInput).toHaveValue("01.01.2023")

    const editionInput = screen.queryByDisplayValue("1")
    expect(editionInput).toBeInTheDocument()

    const yearInput = screen.queryByDisplayValue("2023")
    expect(yearInput).toBeInTheDocument()

    const pageInput = screen.queryByDisplayValue("25")
    expect(pageInput).toBeInTheDocument()

    const areaOfPublicationInput = screen.queryByDisplayValue("bar")
    expect(areaOfPublicationInput).toBeInTheDocument()

    const numberOfPublicationInRespectiveAreaInput =
      screen.queryByDisplayValue("baz")
    expect(numberOfPublicationInRespectiveAreaInput).toBeInTheDocument()
  })

  it("emits update model value event when input value changes", async () => {
    let modelValue: Metadata = {}
    const updateModelValue = vi.fn().mockImplementation((data: Metadata) => {
      modelValue = data
    })

    const { user, rerender } = renderComponent({
      modelValue,
      "onUpdate:modelValue": updateModelValue,
    })

    const {
      announcementMediumInput,
      dateInput,
      editionInput,
      yearInput,
      areaOfPublicationInput,
      pageInput,
      numberOfPublicationInRespectiveAreaInput,
      additionalInfoInput,
      explanationInput,
    } = getControls()

    await user.type(announcementMediumInput, "foo")
    await rerender({ modelValue })
    await user.type(dateInput, "05.04.2023")
    await rerender({ modelValue })
    await user.type(editionInput, "1")
    await rerender({ modelValue })
    await user.type(yearInput, "2023")
    await rerender({ modelValue })
    await user.type(areaOfPublicationInput, "baz baz")
    await rerender({ modelValue })
    await user.type(pageInput, "foo bar")
    await rerender({ modelValue })
    await user.type(numberOfPublicationInRespectiveAreaInput, "bar foo")
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
      ANNOUNCEMENT_MEDIUM: ["foo"],
      DATE: ["2023-04-05T00:00:00.000Z"],
      EDITION: ["1"],
      YEAR: ["2023"],
      AREA_OF_PUBLICATION: ["baz baz"],
      PAGE: ["foo bar"],
      NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA: ["bar foo"],
      ADDITIONAL_INFO: ["additional info 1", "additional info 2"],
      EXPLANATION: ["explanation 1", "explanation 2"],
    })
  })

  it("emits update model value event when input value is cleared", async () => {
    let modelValue: Metadata = {
      ANNOUNCEMENT_MEDIUM: ["foo"],
      DATE: ["2023-04-05"],
      EDITION: ["1"],
      YEAR: ["2023"],
      AREA_OF_PUBLICATION: ["foo bar"],
      PAGE: ["baz baz"],
      NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA: ["bar foo"],
      ADDITIONAL_INFO: ["foo baz"],
      EXPLANATION: ["ban baz"],
    }
    const updateModelValue = vi.fn().mockImplementation((data: Metadata) => {
      modelValue = data
    })

    const { user, rerender } = renderComponent({
      modelValue,
      "onUpdate:modelValue": updateModelValue,
    })

    const {
      announcementMediumInput,
      dateInput,
      editionInput,
      yearInput,
      areaOfPublicationInput,
      pageInput,
      numberOfPublicationInRespectiveAreaInput,
      additionalInfoChips,
      explanationChips,
    } = getControls()

    expect(announcementMediumInput).toHaveValue("foo")
    await user.clear(announcementMediumInput)
    await rerender({ modelValue })
    expect(modelValue.ANNOUNCEMENT_MEDIUM).toBeUndefined()

    expect(dateInput).toHaveValue("05.04.2023")
    await userEvent.clear(dateInput)
    await rerender({ modelValue })
    expect(modelValue.DATE).toBeUndefined()

    expect(editionInput).toHaveValue("1")
    await user.clear(editionInput)
    await rerender({ modelValue })
    expect(modelValue.EDITION).toBeUndefined()

    expect(yearInput).toHaveValue("2023")
    await user.clear(yearInput)
    await rerender({ modelValue })
    expect(modelValue.YEAR).toBeUndefined()

    expect(areaOfPublicationInput).toHaveValue("foo bar")
    await user.clear(areaOfPublicationInput)
    await rerender({ modelValue })
    expect(modelValue.AREA_OF_PUBLICATION).toBeUndefined()

    expect(pageInput).toHaveValue("baz baz")
    await user.clear(pageInput)
    await rerender({ modelValue })
    expect(modelValue.PAGE).toBeUndefined()

    expect(numberOfPublicationInRespectiveAreaInput).toHaveValue("bar foo")
    await user.clear(numberOfPublicationInRespectiveAreaInput)
    await rerender({ modelValue })
    expect(modelValue.NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA).toBe(
      undefined,
    )

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
