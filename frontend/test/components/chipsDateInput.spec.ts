import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import dayjs from "dayjs"
import timezone from "dayjs/plugin/timezone"
import utc from "dayjs/plugin/utc"
import ChipsDateInput from "@/components/ChipsDateInput.vue"

dayjs.extend(utc)
dayjs.extend(timezone)

function renderComponent(options?: {
  ariaLabel?: string
  value?: string
  modelValue?: string[]
  placeholder?: string
}) {
  const user = userEvent.setup()
  const props = {
    id: "identifier",
    value: options?.value,
    modelValue: options?.modelValue,
    ariaLabel: options?.ariaLabel ?? "aria-label",
    placeholder: options?.placeholder,
  }
  const utils = render(ChipsDateInput, { props })
  return { screen, user, props, ...utils }
}

describe("ChipsInput", () => {
  beforeEach(() => {
    dayjs.tz.setDefault("Europe/Berlin")
  })

  afterEach(() => {
    dayjs.tz.setDefault()
  })

  it("shows a chips input element with type of date", () => {
    renderComponent({
      ariaLabel: "test-label",
    })
    const input = screen.getByLabelText("test-label") as HTMLInputElement

    expect(input).toBeInTheDocument()
    expect(input?.type).toBe("date")
  })

  it("shows chips date input with an aria label", () => {
    renderComponent({
      ariaLabel: "test-label",
    })
    const input = screen.getByLabelText("test-label")

    expect(input).toBeInTheDocument()
  })

  it("press enter renders formatted date value in chip", async () => {
    const { user } = renderComponent({
      ariaLabel: "test-label",
    })
    const input = screen.getByLabelText("test-label")
    await userEvent.clear(input)
    expect(input).toHaveValue("")

    await user.type(input, "2022-02-03")
    await user.type(input, "{enter}")

    const chipList = screen.getAllByLabelText("chip")
    expect(chipList.length).toBe(1)
    expect(chipList[0]).toHaveTextContent("03.02.2022")

    expect(input).toHaveValue("")
  })

  it("press enter does not render chip if date string empty", async () => {
    const { user } = renderComponent()
    const input = screen.getByLabelText("aria-label")
    expect(input).toHaveValue("")

    await user.type(input, "{enter}")

    const chipList = screen.queryAllByLabelText("chip")

    expect(chipList.length).toBe(0)

    expect(input).toHaveValue("")
  })

  it("adds multiple date values in chips", async () => {
    const { user } = renderComponent()
    const input = screen.getByLabelText("aria-label")
    expect(input).toHaveValue("")

    await user.type(input, "2022-02-03")
    await user.type(input, "{enter}")
    await user.type(input, "2022-01-03")
    await user.type(input, "{enter}")

    const chipList = screen.getAllByLabelText("chip")
    expect(chipList.length).toBe(2)
    expect(chipList[0]).toHaveTextContent("03.02.2022")
    expect(chipList[1]).toHaveTextContent("03.01.2022")

    expect(input).toHaveValue("")
  })

  it("does not render chips with letters", async () => {
    const { user } = renderComponent()
    const input = screen.getByLabelText("aria-label")
    expect(input).toHaveValue("")

    await user.type(input, "test")
    await user.type(input, "{enter}")

    expect(screen.queryByLabelText("chip")).not.toBeInTheDocument()
    expect(input).toHaveValue("")
  })

  it("emits model update event when user adds an input", async () => {
    const { emitted, user } = renderComponent()
    const input = screen.getByLabelText("aria-label")
    await user.type(input, "2022-02-03")
    await user.type(input, "{enter}")
    await userEvent.tab()

    expect(emitted()["update:modelValue"]).toEqual([
      [["2022-02-03T00:00:00.000Z"]],
    ])
  })

  it("resets date input on backspace delete", async () => {
    const { user } = renderComponent()
    const input = screen.getByLabelText("aria-label") as HTMLInputElement
    await user.type(input, "2022-02-03")
    expect(input).toHaveValue("2022-02-03")
    await user.type(input, "{backspace}")
    expect(input).toHaveValue("")
  })
})
