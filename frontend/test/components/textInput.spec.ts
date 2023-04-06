import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import TextInput from "@/shared/components/input/TextInput.vue"

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
  const utils = render(TextInput, { props })
  return { user, props, ...utils }
}

describe("TextInput", () => {
  it("shows an text input element", () => {
    renderComponent()
    const input: HTMLInputElement | null = screen.queryByRole("textbox")

    expect(input).toBeInTheDocument()
    expect(input?.type).toBe("text")
  })

  it("shows input with an aria label", () => {
    renderComponent({
      ariaLabel: "test-label",
    })
    const input = screen.queryByLabelText("test-label")

    expect(input).toBeInTheDocument()
  })

  it("shows input with a placeholder", () => {
    renderComponent({
      placeholder: "Test Placeholder",
    })
    const input = screen.queryByPlaceholderText("Test Placeholder")

    expect(input).toBeInTheDocument()
  })

  it("allows to type text inside input", async () => {
    const { user } = renderComponent({ value: "one" })
    const input: HTMLInputElement = screen.getByRole("textbox")
    expect(input).toHaveValue("one")

    await user.type(input, " two")

    expect(input).toHaveValue("one two")
  })

  it("emits input events when user types into input", async () => {
    const { emitted, user } = renderComponent()
    const input: HTMLInputElement = screen.getByRole("textbox")

    await user.type(input, "ab")

    expect(emitted().input).toHaveLength(2)
    expect(emitted().input).toEqual([[expect.any(Event)], [expect.any(Event)]])
  })

  it("emits model update event when user types into input", async () => {
    const { emitted, user } = renderComponent()
    const input: HTMLInputElement = screen.getByRole("textbox")
    await user.type(input, "a")

    expect(emitted()["update:modelValue"]).toEqual([["a"]])
  })
})
