import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createPinia, setActivePinia } from "pinia"
import AnnouncementDateInputGroup from "@/components/announcementDate/AnnouncementDateInputGroup.vue"
import { Metadata } from "@/domain/norm"

type AnnouncementDateInputGroupProps = InstanceType<
  typeof AnnouncementDateInputGroup
>["$props"]

function renderComponent(props?: Partial<AnnouncementDateInputGroupProps>) {
  const effectiveProps = {
    modelValue: props?.modelValue ?? {},
    "onUpdate:modelValue": props?.["onUpdate:modelValue"],
  }
  return render(AnnouncementDateInputGroup, { props: effectiveProps })
}

describe("Announcement date/time/year fields", () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
  })
  it("defaults to the date selection", () => {
    renderComponent()

    const dateRadio = screen.getByRole("radio", { name: "Datum" })
    expect(dateRadio).toBeChecked()
  })

  it("shows the year input and passes the year value", async () => {
    renderComponent({ modelValue: { YEAR: ["2013"] } })

    const yearInputField = screen.getByRole("textbox", { name: "Jahresangabe" })
    expect(yearInputField).toBeVisible()
    expect(yearInputField).toHaveValue("2013")
  })

  it("shows announcement date and time inputs and passes their values", () => {
    renderComponent({ modelValue: { DATE: ["2020-01-01"], TIME: ["10:11"] } })

    const dateInputField = screen.getByRole("textbox", { name: "Datum" })
    expect(dateInputField).toBeVisible()
    expect(dateInputField).toHaveValue("01.01.2020")

    const timeInputField = screen.getByLabelText("Uhrzeit")
    expect(timeInputField).toBeVisible()
    expect(timeInputField).toHaveValue("10:11")
  })

  it("selects the date selection", async () => {
    const user = userEvent.setup()
    let modelValue: Metadata = { YEAR: ["2013"] }

    const onUpdateModelValue = vi.fn().mockImplementation((value) => {
      modelValue = value
    })

    const { rerender } = renderComponent({
      modelValue,
      "onUpdate:modelValue": onUpdateModelValue,
    })

    const dateRadio = screen.getByRole("radio", { name: "Datum" })
    await user.click(dateRadio)
    await rerender({ modelValue })
    const dateInputField = screen.getByRole("textbox", { name: "Datum" })
    expect(dateInputField).toBeVisible()
  })

  it("selects the year selection", async () => {
    const user = userEvent.setup()
    let modelValue: Metadata = {}
    const onUpdateModelValue = vi.fn().mockImplementation((value) => {
      modelValue = value
    })

    const { rerender } = renderComponent({
      modelValue,
      "onUpdate:modelValue": onUpdateModelValue,
    })

    const yearRadio = screen.getByRole("radio", { name: "Jahresangabe" })
    await user.click(yearRadio)
    await rerender({ modelValue })
    const yearInputField = screen.getByRole("textbox", { name: "Jahresangabe" })
    expect(yearInputField).toBeVisible()
  })

  it("emits a model update when date data is updated", async () => {
    const user = userEvent.setup()
    const { emitted } = renderComponent({ modelValue: { DATE: [] } })

    const dateInputField = screen.getByRole("textbox", { name: "Datum" })
    await user.type(dateInputField, "01.01.2022")
    expect(emitted("update:modelValue")).toBeTruthy()
  })

  it("emits a model update when year data is updated", async () => {
    const user = userEvent.setup()
    const { emitted } = renderComponent({ modelValue: { YEAR: ["2010"] } })

    const yearInputField = screen.getByRole("textbox", { name: "Jahresangabe" })
    await user.clear(yearInputField)
    await user.type(yearInputField, "2022")
    expect(emitted("update:modelValue")).toBeTruthy()
  })

  it("restores the original data after switching types", async () => {
    const user = userEvent.setup()
    let modelValue: Metadata = {
      YEAR: ["2020"],
    }
    const onUpdateModelValue = vi.fn().mockImplementation((value) => {
      modelValue = value
    })

    const { rerender } = renderComponent({
      modelValue,
      "onUpdate:modelValue": onUpdateModelValue,
    })

    const dateRadio = screen.getByRole("radio", { name: "Datum" })
    await user.click(dateRadio)
    await rerender({ modelValue })
    const dateInputField = screen.getByRole("textbox", { name: "Datum" })
    await user.type(dateInputField, "01.01.2022")
    expect(dateInputField).toHaveValue("01.01.2022")

    const yearRadio = screen.getByRole("radio", { name: "Jahresangabe" })
    await user.click(yearRadio)
    await rerender({ modelValue })
    const yearInputField = screen.getByRole("textbox", { name: "Jahresangabe" })
    expect(yearInputField).toHaveValue("2020")
  })

  it("doesn't revert to date when year is cleared", async () => {
    const user = userEvent.setup()
    let modelValue: Metadata = { YEAR: ["2020"] }
    const onUpdateModelValue = vi.fn().mockImplementation((value) => {
      modelValue = value
    })
    renderComponent({
      modelValue,
      "onUpdate:modelValue": onUpdateModelValue,
    })

    const yearInputField = screen.getByRole("textbox", { name: "Jahresangabe" })
    await user.clear(yearInputField)
    expect(yearInputField).toBeVisible()
  })
})
