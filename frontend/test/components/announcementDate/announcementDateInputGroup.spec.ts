import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import AnnouncementDateInputGroup from "@/components/announcementDate/AnnouncementDateInputGroup.vue"
import { Metadata } from "@/domain/Norm"

type AnnouncementDateInputGroupProps = InstanceType<
  typeof AnnouncementDateInputGroup
>["$props"]

function renderComponent(props?: Partial<AnnouncementDateInputGroupProps>) {
  let modelValue: Metadata = props?.modelValue ?? {}

  const effectiveProps: AnnouncementDateInputGroupProps = {
    modelValue,
    "onUpdate:modelValue": (val) => (modelValue = val),
    ...props,
  }

  return render(AnnouncementDateInputGroup, { props: effectiveProps })
}

describe("Announcement date/time/year fields", () => {
  it("defaults to the date selection", () => {
    renderComponent({})
    const dateRadio = screen.getByRole("radio", {
      name: "Wählen Sie ein Datum",
    })

    expect(dateRadio).toBeChecked()
  })

  it("shows the year input and passes the year value", async () => {
    renderComponent({ modelValue: { YEAR: ["2013"] } })

    const announcementDateYearInputField = screen.getByRole("textbox", {
      name: "Jahresangabe",
    }) as HTMLInputElement

    expect(announcementDateYearInputField).toBeVisible()
    expect(announcementDateYearInputField).toHaveValue("2013")
  })

  it("shows announcement date and time inputs and passes their values", () => {
    renderComponent({ modelValue: { DATE: ["2020-01-01"], TIME: ["10:11"] } })

    const announcementDateInputField = screen.getByRole("textbox", {
      name: "Datum",
    }) as HTMLInputElement

    const announcementDateTimeInputField = screen.getByLabelText(
      "Uhrzeit"
    ) as HTMLInputElement

    expect(announcementDateInputField).toBeVisible()
    expect(announcementDateInputField).toHaveValue("01.01.2020")

    expect(announcementDateTimeInputField).toBeVisible()
    expect(announcementDateTimeInputField).toHaveValue("10:11")
  })

  it("selects the date selection", async () => {
    let modelValue: Metadata = { YEAR: ["2013"] }
    const { rerender } = renderComponent({
      modelValue,
      "onUpdate:modelValue": (val) => (modelValue = val),
    })

    const user = userEvent.setup()

    const yearRadio = screen.getByRole("radio", { name: "Wählen Sie ein Jahr" })
    expect(yearRadio).toBeChecked()

    const dateRadio = screen.getByRole("radio", {
      name: "Wählen Sie ein Datum",
    })
    await user.click(dateRadio)
    expect(dateRadio).toBeChecked()

    await rerender({ modelValue })
    const announcementDateDateInputField = screen.getByRole("textbox", {
      name: "Datum",
    }) as HTMLInputElement
    expect(announcementDateDateInputField).toBeVisible()
  })

  it("selects the year selection", async () => {
    let modelValue: Metadata = {}
    const { rerender } = renderComponent({
      modelValue,
      "onUpdate:modelValue": (val) => (modelValue = val),
    })

    const user = userEvent.setup()

    const dateRadio = screen.getByRole("radio", {
      name: "Wählen Sie ein Datum",
    })
    expect(dateRadio).toBeChecked()

    const yearRadio = screen.getByRole("radio", { name: "Wählen Sie ein Jahr" })
    await user.click(yearRadio)
    expect(yearRadio).toBeChecked()

    await rerender({ modelValue })
    const announcementDateYearInputField = screen.getByRole("textbox", {
      name: "Jahresangabe",
    }) as HTMLInputElement
    expect(announcementDateYearInputField).toBeVisible()
  })

  it("emits a model update when date data is updated", async () => {
    const { emitted } = renderComponent({ modelValue: { DATE: [] } })
    const user = userEvent.setup()

    const announcementDateInputField = screen.getByRole("textbox", {
      name: "Datum",
    }) as HTMLInputElement

    await user.type(announcementDateInputField, "01.01.2022")
    expect(emitted("update:modelValue")).toBeTruthy()
  })

  it("emits a model update when year data is updated", async () => {
    const { emitted } = renderComponent({ modelValue: { YEAR: ["2010"] } })
    const user = userEvent.setup()

    const announcementYearInputField = screen.getByRole("textbox", {
      name: "Jahresangabe",
    }) as HTMLInputElement

    await user.clear(announcementYearInputField)
    await user.type(announcementYearInputField, "2022")
    expect(emitted("update:modelValue")).toBeTruthy()
  })

  it("clears the date and time when the type is set to year", async () => {
    let modelValue: Metadata = { DATE: ["01.01.2020"], TIME: ["10:11"] }
    renderComponent({
      modelValue,
      "onUpdate:modelValue": (val) => (modelValue = val),
    })

    const user = userEvent.setup()

    const yearRadio = screen.getByRole("radio", { name: "Wählen Sie ein Jahr" })
    await user.click(yearRadio)

    expect(modelValue).toEqual({ YEAR: [] })
  })

  it("clears the year when the type is set to date", async () => {
    let modelValue: Metadata = { YEAR: ["2020"] }
    renderComponent({
      modelValue,
      "onUpdate:modelValue": (val) => (modelValue = val),
    })

    const user = userEvent.setup()

    const dateRadio = screen.getByRole("radio", {
      name: "Wählen Sie ein Datum",
    })
    await user.click(dateRadio)

    expect(modelValue).toEqual({ DATE: [], TIME: [] })
  })

  it("doesn't automatically switch back to date when the year is cleared", async () => {
    let modelValue: Metadata = { YEAR: ["2020"] }
    renderComponent({
      modelValue,
      "onUpdate:modelValue": (val) => (modelValue = val),
    })

    const user = userEvent.setup()

    const announcementYearInputField = screen.getByRole("textbox", {
      name: "Jahresangabe",
    }) as HTMLInputElement

    await user.clear(announcementYearInputField)

    expect(modelValue).toEqual({ YEAR: [] })
    expect(announcementYearInputField).toBeVisible()
  })
})
