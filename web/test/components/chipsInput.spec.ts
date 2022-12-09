import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import ChipsInput from "@/components/ChipsInput.vue"

function renderComponent(options?: {
  ariaLabel?: string
  value?: string
  modelValue?: string
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
  const utils = render(ChipsInput, { props })
  return { screen, user, props, ...utils }
}

describe("ChipsInput", () => {
  it("shows a chips input element", () => {
    renderComponent()
    const input = screen.getByRole("textbox") as HTMLInputElement

    expect(input).toBeInTheDocument()
    expect(input?.type).toBe("text")
  })

  it("shows chips input with an aria label", () => {
    renderComponent({
      ariaLabel: "test-label",
    })
    const input = screen.queryByLabelText("test-label")

    expect(input).toBeInTheDocument()
  })

  it("allows to type text inside input", async () => {
    const { user } = renderComponent()
    const input: HTMLInputElement = screen.getByRole("textbox")
    expect(input).toHaveValue("")

    await user.type(input, "one")

    expect(input).toHaveValue("one")
  })

  it("press enter renders value in chip", async () => {
    const { user } = renderComponent()
    const input: HTMLInputElement = screen.getByRole("textbox")
    expect(input).toHaveValue("")

    await user.type(input, "one")
    await user.type(input, "{enter}")

    const chipList = screen.getAllByLabelText("chip")
    expect(chipList.length).toBe(1)

    expect(input).toHaveValue("")
  })

  it("press enter saves chip without whitespaces", async () => {
    const { user } = renderComponent()
    const input: HTMLInputElement = screen.getByRole("textbox")
    expect(input).toHaveValue("")

    await user.type(input, " one ")
    await user.type(input, "{enter}")

    const chipList = screen.getAllByLabelText("chip")

    expect(chipList.length).toBe(1)
    expect(chipList[0]).toHaveTextContent("one")

    expect(input).toHaveValue("")
  })

  it("adds multiple values in chips", async () => {
    const { user } = renderComponent()
    const input: HTMLInputElement = screen.getByRole("textbox")
    expect(input).toHaveValue("")

    await user.type(input, "one")
    await user.type(input, "{enter}")
    await user.type(input, "two")
    await user.type(input, "{enter}")
    screen.getAllByLabelText("chip")

    const chipList = screen.getAllByLabelText("chip")
    expect(chipList.length).toBe(2)
    expect(chipList[0]).toHaveTextContent("one")
    expect(chipList[1]).toHaveTextContent("two")

    expect(input).toHaveValue("")
  })

  it("deletes chip per click on delete icon", async () => {
    const { user } = renderComponent()
    const input: HTMLInputElement = screen.getByRole("textbox")
    expect(input).toHaveValue("")

    await user.type(input, "one")
    await user.type(input, "{enter}")
    await user.type(input, "two")
    await user.type(input, "{enter}")

    const chipList = screen.getAllByLabelText("chip")
    expect(chipList.length).toBe(2)
    expect(chipList[0]).toHaveTextContent("one")
    expect(chipList[1]).toHaveTextContent("two")

    const deleteButton = screen.getAllByLabelText("Löschen")[0] as HTMLElement
    await user.click(deleteButton)

    expect(screen.getAllByLabelText("chip").length).toBe(1)
    expect(chipList[0]).toHaveTextContent("two")
    expect(input).toHaveValue("")
  })

  it("deletes chip per backspace", async () => {
    const { user } = renderComponent()
    const input: HTMLInputElement = screen.getByRole("textbox")
    expect(input).toHaveValue("")

    await user.type(input, "one")
    await user.type(input, "{enter}")
    await user.type(input, "two")
    await user.type(input, "{enter}")

    const chipList = screen.getAllByLabelText("chip")
    expect(chipList.length).toBe(2)
    expect(chipList[0]).toHaveTextContent("one")
    expect(chipList[1]).toHaveTextContent("two")

    await user.type(input, "{backspace}")
    expect(chipList.length).toBe(1)
    expect(chipList[0]).toHaveTextContent("one")

    expect(input).toHaveValue("")
  })

  it("deletes written input first on backspace", async () => {
    const { user } = renderComponent()
    const input: HTMLInputElement = screen.getByRole("textbox")
    expect(input).toHaveValue("")

    await user.type(input, "one")
    await user.type(input, "{enter}")
    await user.type(input, "two")
    await user.type(input, "{enter}")
    await user.type(input, "three")

    const chipList = screen.getAllByLabelText("chip")
    expect(chipList.length).toBe(2)
    expect(chipList[0]).toHaveTextContent("one")
    expect(chipList[1]).toHaveTextContent("two")

    await user.type(input, "{backspace}")
    expect(chipList.length).toBe(2)

    expect(input).toHaveValue("thre")
  })

  it("sets previous chip active on arrow key 'left'", async () => {
    const { user } = renderComponent()
    const input: HTMLInputElement = screen.getByRole("textbox")
    expect(input).toHaveValue("")

    await user.type(input, "one")
    await user.type(input, "{enter}")
    await user.type(input, "two")
    await user.type(input, "{enter}")

    const chipList = screen.getAllByLabelText("chip")
    expect(chipList.length).toBe(2)
    await user.type(input, "{arrowleft}")
    expect(chipList[1]).toHaveFocus()
    await user.type(input, "{arrowleft}")
    expect(chipList[0]).toHaveFocus()
  })

  it("deletes active chip on press enter", async () => {
    const { user } = renderComponent()
    const input: HTMLInputElement = screen.getByRole("textbox")
    expect(input).toHaveValue("")

    await user.type(input, "one")
    await user.type(input, "{enter}")
    await user.type(input, "two")
    await user.type(input, "{enter}")

    const chipList = screen.getAllByLabelText("chip")
    expect(chipList.length).toBe(2)
    expect(chipList[0]).toHaveTextContent("one")
    expect(chipList[1]).toHaveTextContent("two")

    await user.type(input, "{arrowleft}")
    await user.type(input, "{arrowleft}")
    expect(chipList[0]).toHaveFocus()
  })

  it("chips and text: focus via arrow keys behaves as expected", async () => {
    const { user } = renderComponent()
    const input: HTMLInputElement = screen.getByRole("textbox")
    expect(input).toHaveValue("")

    await user.type(input, "one")
    await user.type(input, "{enter}")
    await user.type(input, "two")
    await user.type(input, "{enter}")
    await user.type(input, "x") // just text, not a confirmed chip
    expect(input).toHaveValue("x")

    const chipList = screen.getAllByLabelText("chip")
    expect(chipList.length).toBe(2)

    // now happens: 2x arrowleft, click on chip "two", 2x arrowleft, 3x arrowright

    await user.type(input, "{arrowleft}")
    await user.type(input, "{arrowleft}")
    expect(input).toHaveFocus() // ensure no chip is focused despite double left arrow

    const chipOne = chipList[0] as HTMLElement
    const chipTwo = chipList[1] as HTMLElement

    expect(input).toHaveValue("x")
    await user.click(chipTwo) // focusing away from the text-input clears the input
    expect(chipTwo).toHaveFocus()
    expect(input).toHaveValue("")

    await user.type(chipTwo, "{arrowleft}")
    expect(chipOne).toHaveFocus()

    await user.type(chipOne, "{arrowleft}")
    await user.type(chipOne, "{arrowleft}") // nothing happens further left
    expect(chipOne).toHaveFocus()

    await user.type(chipOne, "{arrowright}")
    expect(chipTwo).toHaveFocus()
    await user.type(chipTwo, "{arrowright}")
    expect(input).toHaveFocus()
    await user.type(input, "{arrowright}") // nothing happens further right
    expect(input).toHaveFocus()
    expect(input).toHaveValue("")
  })

  it("click middle chip and continue navigating by arrow keys", async () => {
    const { user } = renderComponent()
    const input: HTMLInputElement = screen.getByRole("textbox")
    expect(input).toHaveValue("")

    await user.type(input, "one")
    await user.type(input, "{enter}")
    await user.type(input, "two")
    await user.type(input, "{enter}")
    await user.type(input, "three")
    await user.type(input, "{enter}")

    screen.getAllByLabelText("chip")

    const chipList = screen.getAllByLabelText("chip")
    expect(chipList.length).toBe(3)

    // now happens: click on chip "two", arrowright, click on chip "two", arrowleft

    const chipTwo = chipList[1] as HTMLElement
    await user.click(chipTwo)
    expect(chipTwo).toHaveFocus()
    await user.type(chipTwo, "{arrowright}")
    expect(chipList[2]).toHaveFocus()
    await user.click(chipTwo)
    expect(chipTwo).toHaveFocus()
    await user.type(chipTwo, "{arrowleft}")
    expect(chipList[0]).toHaveFocus()
  })

  it("emits input event when user adds an input", async () => {
    const { emitted, user } = renderComponent()
    const input: HTMLInputElement = screen.getByRole("textbox")

    await user.type(input, "ab")

    expect(emitted().input).toHaveLength(2)
    expect(emitted().input).toEqual([[expect.any(Event)], [expect.any(Event)]])
  })

  it("emits model update event when user adds an input", async () => {
    const { emitted, user } = renderComponent()
    const input: HTMLInputElement = screen.getByRole("textbox")
    await user.type(input, "ab")
    await user.type(input, "{enter}")
    await userEvent.tab()

    expect(emitted()["update:modelValue"]).toEqual([[["ab"]]])
  })
})
