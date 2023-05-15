import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import DigitalAnnouncementInputGroup from "@/components/DigitalAnnouncementInputGroup.vue"
import { Metadata, MetadatumType } from "@/domain/Norm"

function renderComponent(options?: { modelValue?: Metadata }) {
  const props = {
    modelValue: options?.modelValue ?? {},
  }
  return render(DigitalAnnouncementInputGroup, { props })
}

describe("DigitalAnnouncementInputGroup", () => {
  it("renders all digital announcement inputs", () => {
    renderComponent({
      modelValue: {
        [MetadatumType.ANNOUNCEMENT_MEDIUM]: ["test value"],
        [MetadatumType.DATE]: ["2023-01-01"],
        [MetadatumType.EDITION]: ["test value"],
        [MetadatumType.YEAR]: ["test value"],
        [MetadatumType.AREA_OF_PUBLICATION]: ["test value"],
        [MetadatumType.NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA]: [
          "test value",
        ],
        [MetadatumType.ADDITIONAL_INFO]: ["test value"],
        [MetadatumType.EXPLANATION]: ["test value"],
      },
    })

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

    expect(announcementMediumInput).toBeInTheDocument()
    expect(announcementMediumInput).toHaveValue("test value")

    expect(dateInput).toBeInTheDocument()
    expect(dateInput).toHaveValue("2023-01-01")

    expect(editionInput).toBeInTheDocument()
    expect(editionInput).toHaveValue("test value")

    expect(yearInput).toBeInTheDocument()
    expect(yearInput).toHaveValue("test value")

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
        [MetadatumType.ANNOUNCEMENT_MEDIUM]: ["foo"],
        [MetadatumType.DATE]: ["2023-01-01"],
        [MetadatumType.EDITION]: ["1"],
        [MetadatumType.YEAR]: ["2023"],
        [MetadatumType.AREA_OF_PUBLICATION]: ["bar"],
        [MetadatumType.NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA]: [
          "baz",
        ],
        [MetadatumType.ADDITIONAL_INFO]: ["foo bar"],
        [MetadatumType.EXPLANATION]: ["baz ban"],
      },
    })

    const announcementMediumInput = screen.queryByDisplayValue("foo")
    const dateInput = screen.getByLabelText("Verkündungsdatum", {
      selector: 'input[type="date"]',
    }) as HTMLInputElement
    const editionInput = screen.queryByDisplayValue("1")
    const yearInput = screen.queryByDisplayValue("2023")
    const areaOfPublicationInput = screen.queryByDisplayValue("bar")
    const numberOfPublicationInRespectiveAreaInput =
      screen.queryByDisplayValue("baz")
    const additionalInfoInputInput = screen.queryByDisplayValue("foo bar")
    const explanationInput = screen.queryByDisplayValue("baz ban")

    expect(announcementMediumInput).toBeInTheDocument()
    expect(dateInput).toBeInTheDocument()
    expect(dateInput).toHaveValue("2023-01-01")
    expect(editionInput).toBeInTheDocument()
    expect(yearInput).toBeInTheDocument()
    expect(areaOfPublicationInput).toBeInTheDocument()
    expect(numberOfPublicationInRespectiveAreaInput).toBeInTheDocument()
    expect(additionalInfoInputInput).toBeInTheDocument()
    expect(explanationInput).toBeInTheDocument()
  })

  it("emits update model value event when input value changes", async () => {
    const user = userEvent.setup()
    const modelValue = {}
    renderComponent({ modelValue })

    const input = screen.getAllByRole("textbox")
    await user.type(input[0], "foo")
    await user.type(input[1], "1")
    await user.type(input[2], "2023")
    await user.type(input[3], "foo bar")
    await user.type(input[4], "bar foo")
    await user.type(input[5], "foo baz")
    await user.type(input[6], "ban baz")

    expect(modelValue).toEqual({
      [MetadatumType.ANNOUNCEMENT_MEDIUM]: ["foo"],
      [MetadatumType.EDITION]: ["1"],
      [MetadatumType.YEAR]: ["2023"],
      [MetadatumType.AREA_OF_PUBLICATION]: ["foo bar"],
      [MetadatumType.NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA]: [
        "bar foo",
      ],
      [MetadatumType.ADDITIONAL_INFO]: ["foo baz"],
      [MetadatumType.EXPLANATION]: ["ban baz"],
    })
  })
})
