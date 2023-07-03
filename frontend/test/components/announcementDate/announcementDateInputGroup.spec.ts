import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import AnnouncementDateInputGroup from "@/components/announcementDate/AnnouncementDateInputGroup.vue"
import { Metadata } from "@/domain/Norm"

function renderComponent(options?: {
  ariaLabel?: string
  modelValue?: Metadata
}) {
  const user = userEvent.setup()
  const props = {
    ariaLabel: options?.ariaLabel ?? "aria-label",
    modelValue: options?.modelValue ?? {},
  }
  const utils = render(AnnouncementDateInputGroup, { props })
  return { user, props, ...utils }
}

describe("Announcement date/time/year fields", () => {
  it("defaults to the announcementDate selection", () => {
    renderComponent({})
    const dateRadio = screen.getByRole("radio", {
      name: "Wählen Sie ein Datum",
    })
    expect(dateRadio).toBeChecked()
  })

  it("Shows the year input and passes the year value", async () => {
    renderComponent({ modelValue: { YEAR: ["2013"] } })

    const announcementDateYearInputField = screen.getByRole("textbox", {
      name: "Jahresangabe",
    }) as HTMLInputElement

    expect(announcementDateYearInputField).toBeVisible()
    expect(announcementDateYearInputField).toHaveValue("2013")
  })

  it("selects the announcementDateYear selection", async () => {
    const user = userEvent.setup()
    renderComponent({})

    const dateRadio = screen.getByRole("radio", {
      name: "Wählen Sie ein Datum",
    })
    const yearRadio = screen.getByRole("radio", { name: "Wählen Sie ein Jahr" })

    expect(dateRadio).toBeChecked()

    await user.click(yearRadio)
    expect(yearRadio).toBeChecked()

    const announcementYearInputField = screen.getByRole("textbox", {
      name: "Jahresangabe",
    }) as HTMLInputElement

    expect(announcementYearInputField).toBeVisible()
  })

  it("Shows announcement date and time inputs and passes their values", () => {
    renderComponent({ modelValue: { DATE: ["01.01.2020"], TIME: ["10:11"] } })

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

  it("emits a model update when announcementDate data is updated", async () => {
    const { emitted } = renderComponent({ modelValue: { DATE: [] } })
    const user = userEvent.setup()

    const announcementDateInputField = screen.getByRole("textbox", {
      name: "Datum",
    }) as HTMLInputElement

    await user.type(announcementDateInputField, "01.01.2022")
    expect(emitted("update:modelValue")).toBeTruthy()
  })

  it("emits a model update when announcementYear is updated", async () => {
    const { emitted } = renderComponent({ modelValue: { YEAR: ["2010"] } })
    const user = userEvent.setup()

    const announcementYearInputField = screen.getByRole("textbox", {
      name: "Jahresangabe",
    }) as HTMLInputElement

    await user.clear(announcementYearInputField)
    await user.type(announcementYearInputField, "2022")
    expect(emitted("update:modelValue")).toBeTruthy()
  })
})
