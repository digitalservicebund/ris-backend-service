import userEvent from "@testing-library/user-event"
import { render } from "@testing-library/vue"
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
  const renderResult = render(ChipsInput, { props })
  return { user, props, ...renderResult }
}

describe("ChipsInput", () => {
  it("shows a chips input element", () => {
    const { queryByRole } = renderComponent()
    const input: HTMLInputElement | null = queryByRole("textbox")

    expect(input).toBeInTheDocument()
    expect(input?.type).toBe("text")
  })

  it("shows chips input with an aria label", () => {
    const { queryByLabelText } = renderComponent({
      ariaLabel: "test-label",
    })
    const input = queryByLabelText("test-label")

    expect(input).toBeInTheDocument()
  })

  it("allows to type text inside input", async () => {
    const { getByRole, user } = renderComponent()
    const input: HTMLInputElement = getByRole("textbox")
    expect(input).toHaveValue("")

    await user.type(input, "one")

    expect(input).toHaveValue("one")
  })

  it("press enter renders value in chip", async () => {
    const { getByRole, user, container } = renderComponent()
    const input: HTMLInputElement = getByRole("textbox")
    expect(input).toHaveValue("")

    await user.type(input, "one")
    await user.type(input, "{enter}")

    expect(container.getElementsByClassName("chip").length).toBe(1)

    expect(input).toHaveValue("")
  })

  it("press enter saves chip without whitespaces", async () => {
    const { getByRole, user, container } = renderComponent()
    const input: HTMLInputElement = getByRole("textbox")
    expect(input).toHaveValue("")

    await user.type(input, " one ")
    await user.type(input, "{enter}")

    const chipList = container.getElementsByClassName("chip")
    expect(chipList.length).toBe(1)
    expect(chipList[0]).toHaveTextContent("one")

    expect(input).toHaveValue("")
  })

  it("adds multiple values in chips", async () => {
    const { getByRole, user, container } = renderComponent()
    const input: HTMLInputElement = getByRole("textbox")
    expect(input).toHaveValue("")

    await user.type(input, "one")
    await user.type(input, "{enter}")
    await user.type(input, "two")
    await user.type(input, "{enter}")

    const chipList = container.getElementsByClassName("chip")
    expect(chipList.length).toBe(2)
    expect(chipList[0]).toHaveTextContent("one")
    expect(chipList[1]).toHaveTextContent("two")

    expect(input).toHaveValue("")
  })

  it("deletes chip per click on delete icon", async () => {
    const { getByRole, user, container } = renderComponent()
    const input: HTMLInputElement = getByRole("textbox")
    expect(input).toHaveValue("")

    await user.type(input, "one")
    await user.type(input, "{enter}")
    await user.type(input, "two")
    await user.type(input, "{enter}")

    const chipList = container.getElementsByClassName("chip")
    expect(chipList.length).toBe(2)
    expect(chipList[0]).toHaveTextContent("one")
    expect(chipList[1]).toHaveTextContent("two")

    const deleteButton = container.getElementsByClassName(
      "material-icons"
    )[0] as HTMLElement
    await user.click(deleteButton)

    expect(chipList.length).toBe(1)
    expect(chipList[0]).toHaveTextContent("two")
    expect(input).toHaveValue("")
  })

  it("deletes chip per backspace", async () => {
    const { getByRole, user, container } = renderComponent()
    const input: HTMLInputElement = getByRole("textbox")
    expect(input).toHaveValue("")

    await user.type(input, "one")
    await user.type(input, "{enter}")
    await user.type(input, "two")
    await user.type(input, "{enter}")

    const chipList = container.getElementsByClassName("chip")
    expect(chipList.length).toBe(2)
    expect(chipList[0]).toHaveTextContent("one")
    expect(chipList[1]).toHaveTextContent("two")

    await user.type(input, "{backspace}")
    expect(chipList.length).toBe(1)
    expect(chipList[0]).toHaveTextContent("one")

    expect(input).toHaveValue("")
  })

  it("deletes written input first on backspace", async () => {
    const { getByRole, user, container } = renderComponent()
    const input: HTMLInputElement = getByRole("textbox")
    expect(input).toHaveValue("")

    await user.type(input, "one")
    await user.type(input, "{enter}")
    await user.type(input, "two")
    await user.type(input, "{enter}")
    await user.type(input, "three")

    const chipList = container.getElementsByClassName("chip")
    expect(chipList.length).toBe(2)
    expect(chipList[0]).toHaveTextContent("one")
    expect(chipList[1]).toHaveTextContent("two")

    await user.type(input, "{backspace}")
    expect(chipList.length).toBe(2)

    expect(input).toHaveValue("thre")
  })

  it("sets previous chip active on arrow key 'left'", async () => {
    const { getByRole, user, container } = renderComponent()
    const input: HTMLInputElement = getByRole("textbox")
    expect(input).toHaveValue("")

    await user.type(input, "one")
    await user.type(input, "{enter}")
    await user.type(input, "two")
    await user.type(input, "{enter}")

    const chipList = container.getElementsByClassName("chip")
    expect(chipList.length).toBe(2)
    await user.type(input, "{arrowleft}")
    expect(chipList[1]).toHaveFocus()
    await user.type(input, "{arrowleft}")
    expect(chipList[0]).toHaveFocus()
  })

  it("deletes active chip on press enter", async () => {
    const { getByRole, user, container } = renderComponent()
    const input: HTMLInputElement = getByRole("textbox")
    expect(input).toHaveValue("")

    await user.type(input, "one")
    await user.type(input, "{enter}")
    await user.type(input, "two")
    await user.type(input, "{enter}")

    const chipList = container.getElementsByClassName("chip")
    expect(chipList.length).toBe(2)
    expect(chipList[0]).toHaveTextContent("one")
    expect(chipList[1]).toHaveTextContent("two")

    await user.type(input, "{arrowleft}")
    await user.type(input, "{arrowleft}")
    expect(chipList[0]).toHaveFocus()
  })

  it("emits input event when user adds an input", async () => {
    const { emitted, getByRole, user } = renderComponent()
    const input: HTMLInputElement = getByRole("textbox")

    await user.type(input, "ab")

    expect(emitted().input).toHaveLength(2)
    expect(emitted().input).toEqual([[expect.any(Event)], [expect.any(Event)]])
  })

  it("emits model update event when user adds an input", async () => {
    const { emitted, user, getByRole } = renderComponent()
    const input: HTMLInputElement = getByRole("textbox")
    await user.type(input, "ab")
    await user.type(input, "{enter}")
    await userEvent.tab()

    expect(emitted()["update:modelValue"]).toEqual([[["ab"]]])
  })
})
