import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import DigitalAnnouncementInputGroup from "@/components/DigitalAnnouncementInputGroup.vue"
import { Metadata } from "@/domain/Norm"

function renderComponent(options?: { modelValue?: Metadata }) {
  const props = {
    modelValue: options?.modelValue ?? {},
  }
  return render(DigitalAnnouncementInputGroup, { props })
}

function getControls() {
  const announcementMediumInput = screen.queryByRole("textbox", {
    name: "Verkündungsmedium",
  }) as HTMLInputElement

  const dateInput = screen.getByLabelText("Verkündungsdatum", {
    selector: 'input[type="date"]',
  }) as HTMLInputElement

  const editionInput = screen.queryByRole("textbox", {
    name: "Ausgabenummer",
  }) as HTMLInputElement

  const yearInput = screen.queryByRole("textbox", {
    name: "Jahr",
  }) as HTMLInputElement

  const pageInput = screen.queryByRole("textbox", {
    name: "Seitenzahlen",
  }) as HTMLInputElement

  const areaOfPublicationInput = screen.queryByRole("textbox", {
    name: "Bereich der Veröffentlichung",
  }) as HTMLInputElement

  const numberOfPublicationInRespectiveAreaInput = screen.queryByRole(
    "textbox",
    {
      name: "Nummer der Veröffentlichung im jeweiligen Bereich",
    }
  ) as HTMLInputElement

  const additionalInfoInputInput = screen.queryByRole("textbox", {
    name: "Zusatzangaben",
  }) as HTMLInputElement

  const explanationInput = screen.queryByRole("textbox", {
    name: "Erläuterungen",
  }) as HTMLInputElement

  return {
    announcementMediumInput,
    dateInput,
    editionInput,
    yearInput,
    pageInput,
    areaOfPublicationInput,
    numberOfPublicationInRespectiveAreaInput,
    additionalInfoInputInput,
    explanationInput,
  }
}

describe("DigitalAnnouncementInputGroup", () => {
  it("renders all digital announcement inputs", () => {
    renderComponent({
      modelValue: {
        ANNOUNCEMENT_MEDIUM: ["test value"],
        DATE: ["2023-01-01"],
        EDITION: ["test value"],
        YEAR: ["test value"],
        PAGE: ["test value"],
        AREA_OF_PUBLICATION: ["test value"],
        NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA: ["test value"],
        ADDITIONAL_INFO: ["test value"],
        EXPLANATION: ["test value"],
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
      additionalInfoInputInput,
      explanationInput,
    } = getControls()

    expect(announcementMediumInput).toBeInTheDocument()
    expect(announcementMediumInput).toHaveValue("test value")

    expect(dateInput).toBeInTheDocument()
    expect(dateInput).toHaveValue("2023-01-01")

    expect(editionInput).toBeInTheDocument()
    expect(editionInput).toHaveValue("test value")

    expect(yearInput).toBeInTheDocument()
    expect(yearInput).toHaveValue("test value")

    expect(pageInput).toBeInTheDocument()
    expect(pageInput).toHaveValue("test value")

    expect(areaOfPublicationInput).toBeInTheDocument()
    expect(areaOfPublicationInput).toHaveValue("test value")

    expect(numberOfPublicationInRespectiveAreaInput).toBeInTheDocument()
    expect(numberOfPublicationInRespectiveAreaInput).toHaveValue("test value")

    expect(additionalInfoInputInput).toBeInTheDocument()
    expect(additionalInfoInputInput).toHaveValue("test value")

    expect(explanationInput).toBeInTheDocument()
    expect(explanationInput).toHaveValue("test value")
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

    const dateInput = screen.getByLabelText("Verkündungsdatum", {
      selector: 'input[type="date"]',
    }) as HTMLInputElement
    expect(dateInput).toBeInTheDocument()
    expect(dateInput).toHaveValue("2023-01-01")

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

    const additionalInfoInputInput = screen.queryByDisplayValue("foo bar")
    expect(additionalInfoInputInput).toBeInTheDocument()

    const explanationInput = screen.queryByDisplayValue("baz ban")
    expect(explanationInput).toBeInTheDocument()
  })

  it("emits update model value event when input value changes", async () => {
    const user = userEvent.setup()
    const modelValue = {}
    renderComponent({ modelValue })

    const {
      announcementMediumInput,
      dateInput,
      editionInput,
      yearInput,
      areaOfPublicationInput,
      pageInput,
      numberOfPublicationInRespectiveAreaInput,
      additionalInfoInputInput,
      explanationInput,
    } = getControls()

    await user.type(announcementMediumInput, "foo")
    await user.type(dateInput, "2023-04-05")
    await user.type(editionInput, "1")
    await user.type(yearInput, "2023")
    await user.type(areaOfPublicationInput, "baz baz")
    await user.type(pageInput, "foo bar")
    await user.type(numberOfPublicationInRespectiveAreaInput, "bar foo")
    await user.type(additionalInfoInputInput, "foo baz")
    await user.type(explanationInput, "ban baz")

    expect(modelValue).toEqual({
      ANNOUNCEMENT_MEDIUM: ["foo"],
      DATE: ["2023-04-04T22:00:00.000Z"],
      EDITION: ["1"],
      YEAR: ["2023"],
      AREA_OF_PUBLICATION: ["baz baz"],
      PAGE: ["foo bar"],
      NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA: ["bar foo"],
      ADDITIONAL_INFO: ["foo baz"],
      EXPLANATION: ["ban baz"],
    })
  })

  it("emits update model value event when input value is cleared", async () => {
    const user = userEvent.setup()
    const modelValue: Metadata = {
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

    renderComponent({ modelValue })

    const {
      announcementMediumInput,
      // dateInput,
      editionInput,
      yearInput,
      areaOfPublicationInput,
      pageInput,
      numberOfPublicationInRespectiveAreaInput,
      additionalInfoInputInput,
      explanationInput,
    } = getControls()

    expect(announcementMediumInput).toHaveValue("foo")
    await user.clear(announcementMediumInput)
    expect(modelValue.ANNOUNCEMENT_MEDIUM).toBe(undefined)

    // TODO: I couldn't get this to work - fix once we switched to the new date component
    // expect(dateInput).toHaveValue("2023-04-05")
    // await user.type(dateInput, "1992-12-10")
    // expect(modelValue.DATE).toBe(undefined)

    expect(editionInput).toHaveValue("1")
    await user.clear(editionInput)
    expect(modelValue.EDITION).toBe(undefined)

    expect(yearInput).toHaveValue("2023")
    await user.clear(yearInput)
    expect(modelValue.YEAR).toBe(undefined)

    expect(areaOfPublicationInput).toHaveValue("foo bar")
    await user.clear(areaOfPublicationInput)
    expect(modelValue.AREA_OF_PUBLICATION).toBe(undefined)

    expect(pageInput).toHaveValue("baz baz")
    await user.clear(pageInput)
    expect(modelValue.PAGE).toBe(undefined)

    expect(numberOfPublicationInRespectiveAreaInput).toHaveValue("bar foo")
    await user.clear(numberOfPublicationInRespectiveAreaInput)
    expect(modelValue.NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA).toBe(
      undefined
    )

    expect(additionalInfoInputInput).toHaveValue("foo baz")
    await user.clear(additionalInfoInputInput)
    expect(modelValue.ADDITIONAL_INFO).toBe(undefined)

    expect(explanationInput).toHaveValue("ban baz")
    await user.clear(explanationInput)
    expect(modelValue.EXPLANATION).toBe(undefined)
  })
})
