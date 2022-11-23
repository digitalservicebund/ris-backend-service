import userEvent from "@testing-library/user-event"
import { render } from "@testing-library/vue"
import TextInput from "@/components/TextInput.vue"

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
  const renderResult = render(TextInput, { props })
  return { user, props, ...renderResult }
}

describe("TextInput", () => {
  it("shows an text input element", () => {
    const { queryByRole } = renderComponent()
    const input: HTMLInputElement | null = queryByRole("textbox")

    expect(input).toBeInTheDocument()
    expect(input?.type).toBe("text")
  })

  it("shows input with an aria label", () => {
    const { queryByLabelText } = renderComponent({
      ariaLabel: "test-label",
    })
    const input = queryByLabelText("test-label")

    expect(input).toBeInTheDocument()
  })

  it("shows input with a placeholder", () => {
    const { queryByPlaceholderText } = renderComponent({
      placeholder: "Test Placeholder",
    })
    const input = queryByPlaceholderText("Test Placeholder")

    expect(input).toBeInTheDocument()
  })

  it("allows to type text inside input", async () => {
    const { getByRole, user } = renderComponent({ value: "one" })
    const input: HTMLInputElement = getByRole("textbox")
    expect(input).toHaveValue("one")

    await user.type(input, " two")

    expect(input).toHaveValue("one two")
  })

  it("emits input events when user types into input", async () => {
    const { emitted, getByRole, user } = renderComponent()
    const input: HTMLInputElement = getByRole("textbox")

    await user.type(input, "ab")

    expect(emitted().input).toHaveLength(2)
    expect(emitted().input).toEqual([[expect.any(Event)], [expect.any(Event)]])
  })

  it("emits model update event when user types into input", async () => {
    const { emitted, user, getByRole } = renderComponent()
    const input: HTMLInputElement = getByRole("textbox")
    await user.type(input, "ab")
    await userEvent.tab()

    expect(emitted()["update:modelValue"]).toEqual([["ab"]])
  })
})
