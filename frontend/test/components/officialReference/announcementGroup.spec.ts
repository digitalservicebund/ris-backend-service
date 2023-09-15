import userEvent from "@testing-library/user-event"
import { fireEvent, render, screen } from "@testing-library/vue"
import { createPinia, setActivePinia } from "pinia"
import AnnouncementGroup from "@/components/officialReference/AnnouncementGroup.vue"
import {
  MetadataSectionName,
  MetadataSections,
  MetadatumType,
} from "@/domain/norm"

type AnnouncementGroupProps = InstanceType<typeof AnnouncementGroup>["$props"]

function renderComponent(props?: Partial<AnnouncementGroupProps>) {
  const effectiveProps = {
    modelValue: props?.modelValue ?? {},
    "onUpdate:modelValue": props?.["onUpdate:modelValue"] ?? vi.fn(),
  }

  return render(AnnouncementGroup, { props: effectiveProps })
}

describe("AnnouncementInputGroup", () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
  })

  it("should render the component with 4 radio buttons each for different sections ", () => {
    renderComponent()

    const printRadio = screen.getByLabelText("Papierverkündungsblatt")
    const euRadio = screen.getByLabelText("Amtsblatt der EU")
    const digitalRadio = screen.getByLabelText(
      "Elektronisches Verkündungsblatt",
    )
    const otherRadio = screen.getByLabelText("Sonstige amtliche Fundstelle")

    expect(printRadio).toBeInTheDocument()
    expect(printRadio).toBeVisible()

    expect(euRadio).toBeInTheDocument()
    expect(euRadio).toBeVisible()

    expect(digitalRadio).toBeInTheDocument()
    expect(digitalRadio).toBeVisible()

    expect(otherRadio).toBeInTheDocument()
    expect(otherRadio).toBeVisible()
  })

  it("renders the correct child component when a radio button is selected ", async () => {
    const user = userEvent.setup()

    let modelValue: MetadataSections = {}
    const updateModelValue = vi
      .fn()
      .mockImplementation((data: MetadataSections) => {
        modelValue = data
      })
    const { rerender } = renderComponent({
      modelValue,
      "onUpdate:modelValue": updateModelValue,
    })

    const printRadio = screen.getByLabelText("Papierverkündungsblatt")
    const digitalRadio = screen.getByLabelText(
      "Elektronisches Verkündungsblatt",
    )
    const euRadio = screen.getByLabelText("Amtsblatt der EU")
    const otherRadio = screen.getByLabelText("Sonstige amtliche Fundstelle")

    expect(printRadio).toBeChecked()
    expect(digitalRadio).not.toBeChecked()
    expect(euRadio).not.toBeChecked()
    expect(otherRadio).not.toBeChecked()

    await user.click(digitalRadio)
    await rerender({ modelValue })
    expect(digitalRadio).toBeChecked()
    expect(printRadio).not.toBeChecked()
    expect(
      screen.getByRole("textbox", { name: "Verkündungsmedium" }),
    ).toBeInTheDocument()

    await user.click(euRadio)
    await rerender({ modelValue })
    expect(euRadio).toBeChecked()
    expect(digitalRadio).not.toBeChecked()
    expect(
      screen.getByRole("textbox", { name: "Amtsblatt der EU" }),
    ).toBeInTheDocument()

    await user.click(otherRadio)
    await rerender({ modelValue })
    expect(otherRadio).toBeChecked()
    expect(digitalRadio).not.toBeChecked()
    expect(
      screen.getByRole("textbox", { name: "Sonstige amtliche Fundstelle" }),
    ).toBeInTheDocument()
  })

  it("restores the original data after switching types", async () => {
    const user = userEvent.setup()

    let modelValue: MetadataSections = {
      PRINT_ANNOUNCEMENT: [
        {
          ANNOUNCEMENT_GAZETTE: ["foo"],
          NUMBER: ["1"],
          PAGE: ["2"],
          ADDITIONAL_INFO: ["foo bar"],
          EXPLANATION: ["baz ban"],
        },
      ],
    }

    const updateModelValue = vi
      .fn()
      .mockImplementation((data: MetadataSections) => {
        modelValue = data
      })

    const { rerender, emitted } = renderComponent({
      modelValue,
      "onUpdate:modelValue": updateModelValue,
    })

    const printRadio = screen.getByLabelText("Papierverkündungsblatt")
    const digitalRadio = screen.getByLabelText(
      "Elektronisches Verkündungsblatt",
    )

    await user.click(digitalRadio)
    await rerender({ modelValue })
    expect(emitted("update:modelValue")[0]).toEqual([
      { DIGITAL_ANNOUNCEMENT: [{}] },
    ])

    await user.click(printRadio)
    await rerender({ modelValue })
    expect(emitted("update:modelValue")[1]).toEqual([
      {
        PRINT_ANNOUNCEMENT: [
          {
            ANNOUNCEMENT_GAZETTE: ["foo"],
            NUMBER: ["1"],
            PAGE: ["2"],
            ADDITIONAL_INFO: ["foo bar"],
            EXPLANATION: ["baz ban"],
          },
        ],
      },
    ])
  })

  it("only allows one radio button to be selected at a time", async () => {
    renderComponent()

    const printRadio = screen.getByLabelText("Papierverkündungsblatt")
    const euRadio = screen.getByLabelText("Amtsblatt der EU")
    const digitalRadio = screen.getByLabelText(
      "Elektronisches Verkündungsblatt",
    )
    const otherRadio = screen.getByLabelText("Sonstige amtliche Fundstelle")

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

    const announcementGazetteInput = screen.getByRole("textbox", {
      name: "Verkündungsblatt",
    })

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

    const digitalRadio = screen.getByLabelText(
      "Elektronisches Verkündungsblatt",
    )
    expect(digitalRadio).toBeChecked()

    const digitalMedium = screen.getByRole("textbox", {
      name: "Verkündungsmedium",
    })
    expect(digitalMedium).toBeVisible()
    expect(digitalMedium).toHaveValue("test value")
  })

  it("should by default render the print announcement component if modelValue is empty", function () {
    renderComponent({ modelValue: {} })

    const printRadio = screen.getByLabelText("Papierverkündungsblatt")
    expect(printRadio).toBeChecked()

    const announcementGazetteInput = screen.getByRole("textbox", {
      name: "Verkündungsblatt",
    })
    expect(announcementGazetteInput).toBeInTheDocument()
  })
})
