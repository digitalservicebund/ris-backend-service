import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import PrintAnnouncementInputGroup from "@/components/PrintAnnouncementInputGroup.vue"
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

  const explanationInput = screen.queryByRole("textbox", {
    name: "Erläuterungen",
  }) as HTMLInputElement

  return {
    announcementGazetteInput,
    yearInput,
    numberInput,
    pageInput,
    additionalInfoInput,
    explanationInput,
  }
}

describe("PrintAnnouncementInputGroup", () => {
  it("renders all print announcement inputs", () => {
    renderComponent({
      modelValue: {
        ANNOUNCEMENT_GAZETTE: ["test value"],
        YEAR: ["test value"],
        NUMBER: ["test value"],
        PAGE: ["test value"],
        ADDITIONAL_INFO: ["test value"],
        EXPLANATION: ["test value"],
      },
    })

    const {
      announcementGazetteInput,
      yearInput,
      numberInput,
      pageInput,
      additionalInfoInput,
      explanationInput,
    } = getControls()

    expect(announcementGazetteInput).toBeInTheDocument()
    expect(announcementGazetteInput).toHaveValue("test value")

    expect(yearInput).toBeInTheDocument()
    expect(yearInput).toHaveValue("test value")

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

    const additionalInfoInput = screen.queryByDisplayValue("Info Text")
    expect(additionalInfoInput).toBeInTheDocument()

    const explanationInput = screen.queryByDisplayValue("Explanation text")
    expect(explanationInput).toBeInTheDocument()
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
    await user.type(additionalInfoInput, "foo bar")
    await user.type(explanationInput, "bar foo")

    expect(modelValue).toEqual({
      ANNOUNCEMENT_GAZETTE: ["foo"],
      YEAR: ["2023"],
      NUMBER: ["ban"],
      PAGE: ["baz"],
      ADDITIONAL_INFO: ["foo bar"],
      EXPLANATION: ["bar foo"],
    })
  })

  it("emits update model value event when an input value changes", async () => {
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
      additionalInfoInput,
      explanationInput,
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

    expect(additionalInfoInput).toHaveValue("Info Text")
    await user.clear(additionalInfoInput)
    expect(modelValue.ADDITIONAL_INFO).toBeUndefined()

    expect(explanationInput).toHaveValue("Explanation text")
    await user.clear(explanationInput)
    expect(modelValue.EXPLANATION).toBeUndefined()
  })
})
