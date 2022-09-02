import userEvent from "@testing-library/user-event"
import { render } from "@testing-library/vue"
import { nextTick } from "vue"
import TextInput from "@/components/TextInput.vue"

function renderComponent(options?: {
  ariaLabel?: string
  value?: string
  modelValue?: string
  modeValue?: string
}) {
  const user = userEvent.setup()
  const props = {
    id: "identifier",
    value: options?.value,
    modelValue: options?.modelValue,
    ariaLabel: options?.ariaLabel ?? "aria-label",
  }
  const renderResult = render(TextInput, { props })
  return { user, props, ...renderResult }
}

describe("TextInput", () => {
  it("shows an text input element", () => {
    const { queryByRole } = renderComponent()
    const input: HTMLInputElement | null = queryByRole("textbox")

    expect(input).not.toBeNull()
    expect(input?.type).toBe("text")
  })

  it("shows input with an aria label", () => {
    const { queryByLabelText } = renderComponent({
      ariaLabel: "test-label",
    })
    const input = queryByLabelText("test-label")

    expect(input).not.toBeNull()
  })

  it("fills inital value into input by value property", () => {
    const { getByRole } = renderComponent({ value: "test value" })
    const input: HTMLInputElement = getByRole("textbox")

    expect(input.value).toBe("test value")
  })

  it("fills inital value into input by model value property", async () => {
    const { getByRole } = renderComponent({ modelValue: "test value" })
    const input: HTMLInputElement = getByRole("textbox")
    await nextTick()

    expect(input.value).toBe("test value")
  })

  it("allows to type text inside input", async () => {
    const { getByRole, user } = renderComponent({ value: "one" })
    const input: HTMLInputElement = getByRole("textbox")
    expect(input.value).toBe("one")

    await user.type(input, " two")

    expect(input.value).toBe("one two")
  })

  it("emits input events when user types into input", async () => {
    const { emitted, getByRole, user } = renderComponent()
    const input: HTMLInputElement = getByRole("textbox")

    await user.type(input, "ab")

    expect(emitted().input).toHaveLength(2)
    expect(emitted().input).toEqual([[expect.any(Event)], [expect.any(Event)]])
  })

  it("emits model update event when user types into input", async () => {
    const { emitted, getByRole, user } = renderComponent()
    const input: HTMLInputElement = getByRole("textbox")

    await user.type(input, "ab")

    expect(emitted()["update:modelValue"]).toEqual([["a"], ["ab"]])
  })

  it("updates input value based on injected properties", async () => {
    const { getByRole, rerender, props } = renderComponent({
      value: "old",
    })
    const input: HTMLInputElement = getByRole("textbox")
    expect(input.value).toBe("old")

    await rerender({ ...props, value: "new" })

    expect(input.value).toBe("new")
  })
})
