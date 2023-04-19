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

describe("PrintAnnouncementInputGroup", () => {
  it("renders all print announcement inputs", () => {
    renderComponent()

    const announcementGazetteInput =
      screen.queryAllByLabelText("Verkündungsblatt")
    const yearInput = screen.queryAllByLabelText("Jahr")
    const numberInput = screen.queryAllByLabelText("Nummer")
    const pageNumberInput = screen.queryAllByLabelText("Seitenzahl")

    expect(announcementGazetteInput[0]).toBeInTheDocument()
    expect(yearInput[0]).toBeInTheDocument()
    expect(numberInput[0]).toBeInTheDocument()
    expect(pageNumberInput[0]).toBeInTheDocument()
  })

  it("shows the correct model value entry in the associated input", () => {
    const modelValue = {
      ANNOUNCEMENT_GAZETTE: ["abc"],
      YEAR: ["2012"],
      NUMBER: ["123"],
      PAGE_NUMBER: ["2"],
    }
    renderComponent({ modelValue })

    const announcementGazetteInput = screen.queryByDisplayValue("abc")
    const yearInput = screen.queryByDisplayValue("2012")
    const numberInput = screen.queryByDisplayValue("123")
    const pageNumberInput = screen.queryByDisplayValue("2")

    expect(announcementGazetteInput).toBeInTheDocument()
    expect(yearInput).toBeInTheDocument()
    expect(numberInput).toBeInTheDocument()
    expect(pageNumberInput).toBeInTheDocument()
  })

  it("emits update model value event when input value changes", async () => {
    const { emitted, user } = renderComponent()

    const input = screen.getAllByRole("textbox")
    await user.type(input[0], "c")

    expect(emitted()["update:modelValue"]).toHaveLength(1)
    expect(emitted()["update:modelValue"]).toEqual([
      [{ ANNOUNCEMENT_GAZETTE: ["c"] }],
    ])
  })
})
