import userEvent from "@testing-library/user-event"
import { render, screen, fireEvent } from "@testing-library/vue"
import AnnouncementGroup from "@/components/AnnouncementGroup.vue"
import {
  MetadataSectionName,
  MetadataSections,
  MetadatumType,
} from "@/domain/Norm"

function renderComponent(options?: { modelValue?: MetadataSections }) {
  const props = {
    modelValue: options?.modelValue ?? {},
  }

  return render(AnnouncementGroup, { props })
}

describe("AnnouncementInputGroup", () => {
  it("should render the component with 4 radio buttons each for different sections ", () => {
    renderComponent()
    const printRadio = screen.queryByLabelText(
      "Papierverkündungsblatt"
    ) as HTMLInputElement
    const euRadio = screen.queryByLabelText(
      "Amtsblatt der EU"
    ) as HTMLInputElement
    const digitalRadio = screen.queryByLabelText(
      "Elektronisches Verkündungsblatt"
    ) as HTMLInputElement
    const otherRadio = screen.queryByLabelText(
      "Sonstige amtliche Fundstelle"
    ) as HTMLInputElement

    expect(printRadio).toBeInTheDocument()
    expect(printRadio).toBeVisible()

    expect(euRadio).toBeInTheDocument()
    expect(euRadio).toBeVisible()

    expect(digitalRadio).toBeInTheDocument()
    expect(digitalRadio).toBeVisible()

    expect(otherRadio).toBeInTheDocument()
    expect(otherRadio).toBeVisible()

    const radioLabels = screen
      .getAllByRole("radio")
      .map((radio) => radio.labels[0].textContent)
    expect([...new Set(radioLabels)]).toHaveLength(radioLabels.length)
  })

  it("renders the correct child component when a radio button is selected ", async () => {
    renderComponent()

    const printRadio = screen.queryByLabelText(
      "Papierverkündungsblatt"
    ) as HTMLInputElement
    const digitalRadio = screen.queryByLabelText(
      "Elektronisches Verkündungsblatt"
    ) as HTMLInputElement
    const euRadio = screen.queryByLabelText(
      "Amtsblatt der EU"
    ) as HTMLInputElement
    const otherRadio = screen.queryByLabelText(
      "Sonstige amtliche Fundstelle"
    ) as HTMLInputElement

    expect(printRadio).toBeChecked()
    expect(digitalRadio).not.toBeChecked()
    expect(euRadio).not.toBeChecked()
    expect(otherRadio).not.toBeChecked()

    await fireEvent.click(digitalRadio)
    expect(digitalRadio).toBeChecked()
    expect(printRadio).not.toBeChecked()
    expect(
      screen.getByRole("textbox", {
        name: "Verkündungsmedium",
      }) as HTMLInputElement
    ).toBeInTheDocument()

    await fireEvent.click(euRadio)
    expect(euRadio).toBeChecked()
    expect(digitalRadio).not.toBeChecked()
    expect(
      screen.getByRole("textbox", {
        name: "Amtsblatt der EU",
      }) as HTMLInputElement
    ).toBeInTheDocument()
  })

  it("clears the child section data when a different radio button is selected ", async () => {
    renderComponent()

    const printRadio = screen.queryByLabelText(
      "Papierverkündungsblatt"
    ) as HTMLInputElement
    const digitalRadio = screen.queryByLabelText(
      "Elektronisches Verkündungsblatt"
    ) as HTMLInputElement

    const announcementGazetteInput = screen.queryByRole("textbox", {
      name: "Verkündungsblatt",
    }) as HTMLInputElement

    await userEvent.type(announcementGazetteInput, "foo-bar")
    await userEvent.tab()

    expect(announcementGazetteInput).toHaveValue("foo-bar")

    await fireEvent.click(digitalRadio)

    const announcementMediumInput = screen.queryByRole("textbox", {
      name: "Verkündungsmedium",
    }) as HTMLInputElement

    await userEvent.type(announcementMediumInput, "bar-foo")
    await userEvent.tab()

    expect(announcementMediumInput).toHaveValue("bar-foo")

    await fireEvent.click(printRadio)

    const announcementGazetteInputNew = screen.queryByRole("textbox", {
      name: "Verkündungsblatt",
    }) as HTMLInputElement

    expect(announcementGazetteInputNew).not.toHaveValue()
  })

  it("only allows one radio button to be selected at a time", async () => {
    renderComponent()

    const printRadio = screen.queryByLabelText(
      "Papierverkündungsblatt"
    ) as HTMLInputElement
    const euRadio = screen.queryByLabelText(
      "Amtsblatt der EU"
    ) as HTMLInputElement
    const digitalRadio = screen.queryByLabelText(
      "Elektronisches Verkündungsblatt"
    ) as HTMLInputElement
    const otherRadio = screen.queryByLabelText(
      "Sonstige amtliche Fundstelle"
    ) as HTMLInputElement

    expect(printRadio).toBeChecked()
    expect(euRadio).not.toBeChecked()
    expect(digitalRadio).not.toBeChecked()
    expect(otherRadio).not.toBeChecked()

    await fireEvent.click(euRadio)

    // Verify that euRadio is now checked and the others are not
    expect(printRadio).not.toBeChecked()
    expect(euRadio).toBeChecked()
    expect(digitalRadio).not.toBeChecked()
    expect(otherRadio).not.toBeChecked()

    // Click on the Digital radio button
    await fireEvent.click(digitalRadio)

    // Verify that digitalRadio is now checked and the others are not
    expect(printRadio).not.toBeChecked()
    expect(euRadio).not.toBeChecked()
    expect(digitalRadio).toBeChecked()
    expect(otherRadio).not.toBeChecked()

    // Click on the Other radio button
    await fireEvent.click(otherRadio)

    // Verify that otherRadio is now checked and the others are not
    expect(printRadio).not.toBeChecked()
    expect(euRadio).not.toBeChecked()
    expect(digitalRadio).not.toBeChecked()
    expect(otherRadio).toBeChecked()
  })

  it("emits update:modelValue event when child section data is updated ", async () => {
    const { emitted } = renderComponent()

    const announcementGazetteInput = screen.queryByRole("textbox", {
      name: "Verkündungsblatt",
    }) as HTMLInputElement

    await userEvent.type(announcementGazetteInput, "A")

    expect(emitted("update:modelValue")).toHaveLength(1)
    expect(emitted("update:modelValue")[0]).toStrictEqual([
      {
        [MetadataSectionName.PRINT_ANNOUNCEMENT]: [
          {
            [MetadatumType.ANNOUNCEMENT_GAZETTE]: ["A"],
          },
        ],
      },
    ])
  })

  it("initialises with the correct child section based on the modelvalue prop", function () {
    renderComponent({
      modelValue: {
        [MetadataSectionName.DIGITAL_ANNOUNCEMENT]: [
          {
            [MetadatumType.ANNOUNCEMENT_MEDIUM]: ["test value"],
          },
        ],
      },
    })

    const digitalRadio = screen.queryByLabelText(
      "Elektronisches Verkündungsblatt"
    ) as HTMLInputElement
    expect(digitalRadio).toBeChecked()

    const digitalMedium = screen.queryByRole("textbox", {
      name: "Verkündungsmedium",
    }) as HTMLInputElement
    expect(digitalMedium).toBeVisible()
    expect(digitalMedium).toHaveValue("test value")
  })

  it("should by default render the print announcement component if modelValue is empty", function () {
    renderComponent({ modelValue: {} })

    const printRadio = screen.queryByLabelText(
      "Papierverkündungsblatt"
    ) as HTMLInputElement
    expect(printRadio).toBeChecked()

    const announcementGazetteInput = screen.queryByRole("textbox", {
      name: "Verkündungsblatt",
    }) as HTMLInputElement
    expect(announcementGazetteInput).toBeInTheDocument()
  })
})
